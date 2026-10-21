package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.request.PersonalTransactionRequest;
import com.dev.expensemanagement.dto.response.PagedResponse;
import com.dev.expensemanagement.dto.response.PersonalDashboardResponse;
import com.dev.expensemanagement.dto.response.PersonalTransactionResponse;
import com.dev.expensemanagement.entity.PersonalCategory;
import com.dev.expensemanagement.entity.PersonalTransaction;
import com.dev.expensemanagement.entity.PersonalTransactionType;
import com.dev.expensemanagement.entity.PersonalWallet;
import com.dev.expensemanagement.entity.User;
import com.dev.expensemanagement.repository.PersonalCategoryRepository;
import com.dev.expensemanagement.repository.PersonalTransactionRepository;
import com.dev.expensemanagement.repository.PersonalWalletRepository;
import com.dev.expensemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PersonalTransactionService {

    private final PersonalTransactionRepository transactionRepository;
    private final PersonalWalletRepository walletRepository;
    private final PersonalCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public PersonalTransactionResponse createTransaction(
            Long userId,
            PersonalTransactionRequest request
    ) {
        User user = findUser(userId);
        PersonalWallet wallet = lockWallet(request.getWalletId(), userId);
        PersonalCategory category = requireMatchingCategory(request.getCategoryId(), request.getType());

        PersonalTransaction transaction = PersonalTransaction.builder()
                .user(user)
                .wallet(wallet)
                .category(category)
                .title(request.getTitle().trim())
                .amount(request.getAmount())
                .type(request.getType())
                .note(trimToNull(request.getNote()))
                .receiptImageUrl(trimToNull(request.getReceiptImageUrl()))
                .transactionTime(request.getTransactionTime())
                .build();

        adjustBalance(wallet, request.getType(), request.getAmount(), 1);
        walletRepository.save(wallet);
        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public PersonalTransactionResponse getTransaction(Long userId, Long transactionId) {
        return toResponse(findOwnedTransaction(userId, transactionId));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PersonalTransactionResponse> getTransactions(
            Long userId,
            String month,
            PersonalTransactionType type,
            Long categoryId,
            String query,
            int page,
            int size
    ) {
        YearMonth selectedMonth = parseMonth(month);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Order.desc("transactionTime"), Sort.Order.desc("createdAt"))
        );
        Page<PersonalTransaction> result = transactionRepository.search(
                userId,
                selectedMonth.atDay(1).atStartOfDay(),
                selectedMonth.plusMonths(1).atDay(1).atStartOfDay(),
                type,
                categoryId,
                query == null ? "" : query.trim(),
                pageable
        );

        return PagedResponse.<PersonalTransactionResponse>builder()
                .content(result.getContent().stream().map(this::toResponse).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    @Transactional
    public PersonalTransactionResponse updateTransaction(
            Long userId,
            Long transactionId,
            PersonalTransactionRequest request
    ) {
        PersonalTransaction transaction = findOwnedTransactionForUpdate(userId, transactionId);
        Map<Long, PersonalWallet> lockedWallets = lockWallets(
                userId,
                transaction.getWallet().getId(),
                request.getWalletId()
        );
        PersonalWallet oldWallet = lockedWallets.get(transaction.getWallet().getId());
        PersonalWallet newWallet = lockedWallets.get(request.getWalletId());
        PersonalCategory category = requireMatchingCategory(request.getCategoryId(), request.getType());

        adjustBalance(oldWallet, transaction.getType(), transaction.getAmount(), -1);
        adjustBalance(newWallet, request.getType(), request.getAmount(), 1);

        transaction.setWallet(newWallet);
        transaction.setCategory(category);
        transaction.setTitle(request.getTitle().trim());
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setNote(trimToNull(request.getNote()));
        transaction.setReceiptImageUrl(trimToNull(request.getReceiptImageUrl()));
        transaction.setTransactionTime(request.getTransactionTime());

        walletRepository.saveAll(lockedWallets.values());
        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        PersonalTransaction transaction = findOwnedTransactionForUpdate(userId, transactionId);
        PersonalWallet wallet = lockWallet(transaction.getWallet().getId(), userId);
        adjustBalance(wallet, transaction.getType(), transaction.getAmount(), -1);
        walletRepository.save(wallet);
        transactionRepository.delete(transaction);
    }

    @Transactional(readOnly = true)
    public PersonalDashboardResponse getDashboard(Long userId, String month) {
        YearMonth selectedMonth = parseMonth(month);
        LocalDateTime fromTime = selectedMonth.atDay(1).atStartOfDay();
        LocalDateTime toTime = selectedMonth.plusMonths(1).atDay(1).atStartOfDay();
        List<PersonalTransactionResponse> recent = transactionRepository
                .findTop5ByUser_IdAndTransactionTimeGreaterThanEqualAndTransactionTimeLessThanOrderByTransactionTimeDescCreatedAtDesc(
                        userId,
                        fromTime,
                        toTime
                ).stream()
                .map(this::toResponse)
                .toList();

        return PersonalDashboardResponse.builder()
                .month(selectedMonth.toString())
                .balance(walletRepository.sumBalanceByUserId(userId))
                .income(transactionRepository.sumAmount(
                        userId,
                        PersonalTransactionType.INCOME,
                        fromTime,
                        toTime
                ))
                .expense(transactionRepository.sumAmount(
                        userId,
                        PersonalTransactionType.EXPENSE,
                        fromTime,
                        toTime
                ))
                .recentTransactions(recent)
                .build();
    }

    private Map<Long, PersonalWallet> lockWallets(Long userId, Long firstId, Long secondId) {
        Map<Long, PersonalWallet> wallets = new LinkedHashMap<>();
        Stream.of(firstId, secondId)
                .distinct()
                .sorted()
                .forEach(id -> wallets.put(id, lockWallet(id, userId)));
        return wallets;
    }

    private PersonalWallet lockWallet(Long walletId, Long userId) {
        return walletRepository.findOwnedForUpdate(walletId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Ví không tồn tại hoặc không thuộc tài khoản"));
    }

    private PersonalCategory requireMatchingCategory(Long categoryId, PersonalTransactionType type) {
        PersonalCategory category = categoryRepository.findByIdAndIsActiveTrue(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));
        if (category.getType() != type) {
            throw new IllegalArgumentException("Danh mục không phù hợp với loại giao dịch");
        }
        return category;
    }

    private PersonalTransaction findOwnedTransaction(Long userId, Long transactionId) {
        return transactionRepository.findByIdAndUser_Id(transactionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Giao dịch không tồn tại"));
    }

    private PersonalTransaction findOwnedTransactionForUpdate(Long userId, Long transactionId) {
        return transactionRepository.findOwnedForUpdate(transactionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Giao dịch không tồn tại"));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
    }

    private void adjustBalance(
            PersonalWallet wallet,
            PersonalTransactionType type,
            BigDecimal amount,
            int direction
    ) {
        BigDecimal signedAmount = type == PersonalTransactionType.INCOME ? amount : amount.negate();
        wallet.setBalance(wallet.getBalance().add(signedAmount.multiply(BigDecimal.valueOf(direction))));
    }

    private YearMonth parseMonth(String value) {
        if (value == null || value.isBlank()) return YearMonth.now();
        try {
            return YearMonth.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Tháng phải có định dạng yyyy-MM");
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private PersonalTransactionResponse toResponse(PersonalTransaction transaction) {
        PersonalCategory category = transaction.getCategory();
        PersonalWallet wallet = transaction.getWallet();
        return PersonalTransactionResponse.builder()
                .transactionId(transaction.getId())
                .title(transaction.getTitle())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .categoryIconName(category.getIconName())
                .categoryColorCode(category.getColorCode())
                .walletId(wallet.getId())
                .walletName(wallet.getName())
                .note(transaction.getNote())
                .receiptImageUrl(transaction.getReceiptImageUrl())
                .transactionTime(transaction.getTransactionTime())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
