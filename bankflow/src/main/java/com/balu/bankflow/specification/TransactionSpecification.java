package com.balu.bankflow.specification;

import com.balu.bankflow.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TransactionSpecification {

    public static Specification<Transaction> hasAccountId(Long accountId) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.or(criteriaBuilder.equal(root.get("fromAccount").get("id"), accountId),
                        criteriaBuilder.or(criteriaBuilder.equal(root.get("toAccount").get("id"), accountId))));
    }

    public static Specification<Transaction> hasTransactionType(Transaction.TransactionType transactionType) {
        return (root, query, criteriaBuilder) -> {
            if (transactionType == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("transactionType"), transactionType);
        };
    }

    public static Specification<Transaction> hasStatus(Transaction.TransactionStatus transactionStatus) {
        return (root, query, criteriaBuilder) -> {
            if (transactionStatus == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("status"), transactionStatus);
        };
    }

    public static Specification<Transaction> createdBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, criteriaBuilder) -> {
            if (start != null && end != null) {
                return criteriaBuilder.between(root.get("createdAt"), start, end);
            }
            if (start != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), start);
            }
            if (end != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), end);
            }
            return null;
        };
    }
}
