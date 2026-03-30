package com.goshen.expensetracker.repository;

import com.goshen.expensetracker.model.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT e FROM Expense e JOIN FETCH e.category WHERE e.user.id = :userId " +
            "AND e.expenseDate >= :startDate AND e.expenseDate < :endDate " +
            "ORDER BY e.expenseDate DESC, e.createdAt DESC")
    List<Expense> findByUserIdAndMonth(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    boolean existsByCategoryId(Long categoryId);
}
