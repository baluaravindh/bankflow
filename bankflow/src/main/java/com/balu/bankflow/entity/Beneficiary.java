package com.balu.bankflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiaries")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Beneficiary {

    // id - primary key, auto increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // beneficiaryName - not null
    @Column(nullable = false)
    private String beneficiaryName;

    @Column(nullable = false)
    // accountNumber - not null
    private String accountNumber;

    // bankName - not null
    @Column(nullable = false)
    private String bankName;

    // ifscCode - not null
    @Column(nullable = false)
    private String ifscCode;

    // user - ManyToOne, FetchType.LAZY
    //        JoinColumn name = "user_id"
    //        not null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // createdAt - auto set on create
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
