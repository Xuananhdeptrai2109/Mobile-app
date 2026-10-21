package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.request.CreateWalletRequest;
import com.dev.expensemanagement.dto.request.UpdateWalletRequest;
import com.dev.expensemanagement.dto.response.WalletResponse;
import com.dev.expensemanagement.entity.PersonalWallet;
import com.dev.expensemanagement.entity.User;
import com.dev.expensemanagement.repository.PersonalTransactionRepository;
import com.dev.expensemanagement.repository.PersonalWalletRepository;
import com.dev.expensemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonalWalletService {

    private static final String DEFAULT_WALLET_NAME = "Ví chính";

    private final PersonalWalletRepository walletRepository;
    private final PersonalTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<WalletResponse> getWallets(Long userId) {
        return walletRepository.findByUser_IdOrderByIsDefaultDescCreatedAtAsc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public WalletResponse createWallet(Long userId, CreateWalletRequest request) {
        User user = findUser(userId);
        String name = request.getName().trim();
        requireUniqueName(userId, name, null);

        boolean makeDefault = Boolean.TRUE.equals(request.getMakeDefault())
                || walletRepository.findByUser_IdAndIsDefaultTrue(userId).isEmpty();
        if (makeDefault) unsetCurrentDefault(userId, null);

        PersonalWallet wallet = PersonalWallet.builder()
                .user(user)
                .name(name)
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .isDefault(makeDefault)
                .build();
        return toResponse(walletRepository.save(wallet));
    }

    @Transactional
    public WalletResponse updateWallet(Long userId, Long walletId, UpdateWalletRequest request) {
        PersonalWallet wallet = walletRepository.findOwnedForUpdate(walletId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Ví không tồn tại"));

        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isBlank()) throw new IllegalArgumentException("Tên ví không được trống");
            requireUniqueName(userId, name, walletId);
            wallet.setName(name);
        }

        if (Boolean.TRUE.equals(request.getMakeDefault()) && !Boolean.TRUE.equals(wallet.getIsDefault())) {
            unsetCurrentDefault(userId, walletId);
            wallet.setIsDefault(true);
        } else if (Boolean.FALSE.equals(request.getMakeDefault()) && Boolean.TRUE.equals(wallet.getIsDefault())) {
            throw new IllegalArgumentException("Không thể bỏ trạng thái mặc định nếu chưa chọn ví mặc định khác");
        }

        return toResponse(walletRepository.save(wallet));
    }

    @Transactional
    public void deleteWallet(Long userId, Long walletId) {
        PersonalWallet wallet = walletRepository.findOwnedForUpdate(walletId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Ví không tồn tại"));
        if (Boolean.TRUE.equals(wallet.getIsDefault())) {
            throw new IllegalArgumentException("Không thể xóa ví mặc định");
        }
        if (transactionRepository.existsByWallet_Id(walletId)) {
            throw new IllegalArgumentException("Không thể xóa ví đã có giao dịch");
        }
        walletRepository.delete(wallet);
    }

    @Transactional
    public PersonalWallet createDefaultWallet(User user) {
        return walletRepository.findByUser_IdAndIsDefaultTrue(user.getId())
                .orElseGet(() -> walletRepository.save(PersonalWallet.builder()
                        .user(user)
                        .name(DEFAULT_WALLET_NAME)
                        .balance(BigDecimal.ZERO)
                        .currency("VND")
                        .isDefault(true)
                        .build()));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
    }

    private void unsetCurrentDefault(Long userId, Long exceptWalletId) {
        walletRepository.findByUser_IdAndIsDefaultTrue(userId).ifPresent(current -> {
            if (exceptWalletId == null || !current.getId().equals(exceptWalletId)) {
                current.setIsDefault(false);
                walletRepository.save(current);
            }
        });
    }

    private void requireUniqueName(Long userId, String name, Long currentWalletId) {
        walletRepository.findByUser_IdOrderByIsDefaultDescCreatedAtAsc(userId).stream()
                .filter(wallet -> currentWalletId == null || !wallet.getId().equals(currentWalletId))
                .filter(wallet -> wallet.getName().equalsIgnoreCase(name))
                .findFirst()
                .ifPresent(wallet -> {
                    throw new IllegalArgumentException("Tên ví đã tồn tại");
                });
    }

    private WalletResponse toResponse(PersonalWallet wallet) {
        return WalletResponse.builder()
                .walletId(wallet.getId())
                .name(wallet.getName())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .isDefault(Boolean.TRUE.equals(wallet.getIsDefault()))
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}
