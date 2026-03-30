package com.goshen.expensetracker.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "linked_accounts")
@Getter
@Setter
@NoArgsConstructor
public class LinkedAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "access_token", nullable = false, length = 500)
    private String accessToken;

    @Column(name = "item_id", nullable = false, unique = true, length = 200)
    private String itemId;

    @Column(name = "institution_name", length = 200)
    private String institutionName;

    @Column(name = "account_name", length = 200)
    private String accountName;

    @Column(name = "account_mask", length = 10)
    private String accountMask;

    @Column(name = "cursor")
    private String cursor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
