package com.balu.bankflow.repository;

import com.balu.bankflow.entity.Beneficiary;
import com.balu.bankflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    // Find all beneficiaries by user
    List<Beneficiary> findByUser_Id(Long id);

    // Find by id and user (for ownership check)
    Optional<Beneficiary> findByIdAndUser(Long id, User user);

    // Check if accountNumber already exists for this user
    //  (same user shouldn't add same account twice)
    boolean existsByAccountNumberAndUser(String accountNumber, User user);

}
