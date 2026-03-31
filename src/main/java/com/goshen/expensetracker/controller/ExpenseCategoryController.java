package com.goshen.expensetracker.controller;

import com.goshen.expensetracker.model.dto.CategoryRequest;
import com.goshen.expensetracker.model.dto.CategoryResponse;
import com.goshen.expensetracker.model.entity.User;
import com.goshen.expensetracker.service.ExpenseCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class ExpenseCategoryController {

    private final ExpenseCategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(categoryService.getAll(user));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CategoryRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(request, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(categoryService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        categoryService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
