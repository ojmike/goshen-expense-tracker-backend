package com.goshen.expensetracker.repository;

import com.goshen.expensetracker.model.entity.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {

    List<LoanPayment> findByLoanIdOrderByPaymentDateAscCreatedAtAsc(Long loanId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM LoanPayment p WHERE p.loan.id = :loanId")
    BigDecimal sumPaymentsByLoanId(@Param("loanId") Long loanId);

    Optional<LoanPayment> findByIdAndLoanId(Long id, Long loanId);
}
