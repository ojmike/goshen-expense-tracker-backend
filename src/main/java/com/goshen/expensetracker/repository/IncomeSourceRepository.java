package com.goshen.expensetracker.repository;

import com.goshen.expensetracker.model.entity.IncomeSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncomeSourceRepository extends JpaRepository<IncomeSource, Long> {

    List<IncomeSource> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<IncomeSource> findByIdAndUserId(Long id, Long userId);
}
