package com.balu.bankflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Loan {

    // id - primary key, auto increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // loanType - enum (PERSONAL, HOME, VEHICLE, EDUCATION)
    //            not null
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanType loanType;

    // loanAmount - BigDecimal, precision=15, scale=2, not null
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal loanAmount;

    // tenure - Integer (months), not null
    @Column(nullable = false)
    private Integer tenure;

    // interestRate - BigDecimal, precision=5, scale=2, not null
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    // emiAmount - BigDecimal, precision=15, scale=2
    //             calculated automatically
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal emiAmount;

    // status - enum (PENDING, APPROVED, REJECTED, CLOSED)
    //          default PENDING
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LoanStatus status = LoanStatus.PENDING;

    // remarks - nullable (admin comments on approval/rejection)
    private String remarks;

    // user - ManyToOne, FetchType.LAZY
    //        JoinColumn name = "user_id", not null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // createdAt - auto set on create
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // updatedAt - auto set on update
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum LoanType {
        PERSONAL,
        HOME,
        VEHICLE,
        EDUCATION
    }

    public enum LoanStatus {
        PENDING,
        APPROVED,
        REJECTED,
        CLOSED
    }
}
