package com.goshen.expensetracker.repository;

import com.goshen.expensetracker.model.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

    @Query("SELECT t FROM BankTransaction t LEFT JOIN FETCH t.category " +
            "WHERE t.user.id = :userId AND t.transactionDate >= :startDate AND t.transactionDate < :endDate " +
            "ORDER BY t.transactionDate DESC, t.createdAt DESC")
    List<BankTransaction> findByUserIdAndMonth(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    Optional<BankTransaction> findByIdAndUserId(Long id, Long userId);

    boolean existsByPlaidTransactionId(String plaidTransactionId);

    void deleteByLinkedAccountId(Long linkedAccountId);
}
