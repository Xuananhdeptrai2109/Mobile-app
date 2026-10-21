package com.dev.expensemanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "group_expense_splits",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_expense_id", "group_member_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "split_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_expense_id", nullable = false)
    private GroupExpense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_member_id", nullable = false)
    private GroupMember member;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal splitAmount;

    @Column(name = "is_paid", nullable = false)
    private boolean isPaid;
}
