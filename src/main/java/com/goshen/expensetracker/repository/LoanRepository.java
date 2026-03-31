package com.goshen.expensetracker.repository;

import com.goshen.expensetracker.model.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Loan> findByIdAndUserId(Long id, Long userId);
}
