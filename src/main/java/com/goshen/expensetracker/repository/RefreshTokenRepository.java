package com.goshen.expensetracker.repository;

import com.goshen.expensetracker.model.entity.RefreshToken;
import com.goshen.expensetracker.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    void deleteByToken(String token);
}
