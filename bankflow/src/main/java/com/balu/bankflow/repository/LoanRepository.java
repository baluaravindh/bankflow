package com.balu.bankflow.repository;

import com.balu.bankflow.entity.Loan;
import com.balu.bankflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    // findByUser - return all loans belonging to a given user
    //              used for "my loans" listing on customer side
    List<Loan> findByUser(User user);

    // findByUserAndStatus - return loans for a user filtered by status
    //                        e.g. customer viewing only their APPROVED loans
    List<Loan> findByUserAndStatus(User user, Loan.LoanStatus status);

    // findByStatus - return all loans with a given status
    //                used for admin approval queue (e.g. all PENDING loans)
    List<Loan> findByStatus(Loan.LoanStatus status);
}
