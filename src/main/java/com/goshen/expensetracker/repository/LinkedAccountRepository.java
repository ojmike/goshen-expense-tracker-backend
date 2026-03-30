package com.goshen.expensetracker.repository;

import com.goshen.expensetracker.model.entity.LinkedAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LinkedAccountRepository extends JpaRepository<LinkedAccount, Long> {

    List<LinkedAccount> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<LinkedAccount> findByIdAndUserId(Long id, Long userId);

    Optional<LinkedAccount> findByItemId(String itemId);
}
