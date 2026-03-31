package com.goshen.expensetracker.service;

import com.goshen.expensetracker.exception.ResourceNotFoundException;
import com.goshen.expensetracker.model.dto.IncomeOverviewResponse;
import com.goshen.expensetracker.model.dto.IncomeSourceRequest;
import com.goshen.expensetracker.model.dto.IncomeSourceResponse;
import com.goshen.expensetracker.model.entity.IncomeSource;
import com.goshen.expensetracker.model.entity.PayFrequency;
import com.goshen.expensetracker.model.entity.User;
import com.goshen.expensetracker.repository.IncomeSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IncomeSourceService {

    private static final BigDecimal FIFTY_TWO = new BigDecimal("52");
    private static final BigDecimal TWENTY_SIX = new BigDecimal("26");
    private static final BigDecimal TWELVE = new BigDecimal("12");

    private final IncomeSourceRepository incomeSourceRepository;

    @Transactional(readOnly = true)
    public IncomeOverviewResponse getOverview(User user) {
        List<IncomeSource> sources = incomeSourceRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<IncomeSourceResponse> responses = sources.stream()
                .map(this::toResponse)
                .toList();

        BigDecimal totalMonthly = responses.stream()
                .map(IncomeSourceResponse::monthlyEquivalent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new IncomeOverviewResponse(totalMonthly, responses.size(), responses);
    }

    public IncomeSourceResponse create(IncomeSourceRequest request, User user) {
        IncomeSource source = new IncomeSource();
        source.setUser(user);
        source.setName(request.name());
        source.setAmount(request.amount());
        source.setFrequency(request.frequency());
        source.setNextPayDate(request.nextPayDate());
        source.setSecondPayDay(request.secondPayDay());

        return toResponse(incomeSourceRepository.save(source));
    }

    public IncomeSourceResponse update(Long id, IncomeSourceRequest request, User user) {
        IncomeSource source = incomeSourceRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Income source not found"));

        source.setName(request.name());
        source.setAmount(request.amount());
        source.setFrequency(request.frequency());
        source.setNextPayDate(request.nextPayDate());
        source.setSecondPayDay(request.secondPayDay());

        return toResponse(incomeSourceRepository.save(source));
    }

    public void delete(Long id, User user) {
        IncomeSource source = incomeSourceRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Income source not found"));

        incomeSourceRepository.delete(source);
    }

    public BigDecimal calculateMonthlyEquivalent(BigDecimal amount, PayFrequency frequency) {
        return switch (frequency) {
            case WEEKLY -> amount.multiply(FIFTY_TWO).divide(TWELVE, 4, RoundingMode.HALF_UP);
            case BIWEEKLY -> amount.multiply(TWENTY_SIX).divide(TWELVE, 4, RoundingMode.HALF_UP);
            case MONTHLY -> amount;
        };
    }

    private IncomeSourceResponse toResponse(IncomeSource source) {
        return new IncomeSourceResponse(
                source.getId(),
                source.getName(),
                source.getAmount(),
                source.getFrequency().name(),
                source.getNextPayDate(),
                source.getSecondPayDay(),
                calculateMonthlyEquivalent(source.getAmount(), source.getFrequency()),
                source.getCreatedAt()
        );
    }
}
