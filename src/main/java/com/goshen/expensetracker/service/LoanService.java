package com.goshen.expensetracker.service;

import com.goshen.expensetracker.exception.ResourceNotFoundException;
import com.goshen.expensetracker.model.dto.*;
import com.goshen.expensetracker.model.entity.Loan;
import com.goshen.expensetracker.model.entity.LoanPayment;
import com.goshen.expensetracker.model.entity.User;
import com.goshen.expensetracker.repository.LoanPaymentRepository;
import com.goshen.expensetracker.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanPaymentRepository loanPaymentRepository;

    @Transactional(readOnly = true)
    public List<LoanResponse> getAllLoans(User user) {
        List<Loan> loans = loanRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return loans.stream().map(this::toResponse).toList();
    }

    public LoanResponse createLoan(LoanRequest request, User user) {
        if (request.originalAmount() == null || request.originalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Loan original amount must be positive");
        }
        Loan loan = new Loan();
        loan.setUser(user);
        loan.setName(request.name());
        loan.setOriginalAmount(request.originalAmount());
        loan = loanRepository.save(loan);
        return toResponse(loan);
    }

    @Transactional(readOnly = true)
    public LoanDetailResponse getLoanDetail(Long id, User user) {
        Loan loan = findLoanByUser(id, user);
        BigDecimal totalPaid = Objects.requireNonNullElse(
                loanPaymentRepository.sumPaymentsByLoanId(loan.getId()), BigDecimal.ZERO);
        BigDecimal remainingBalance = loan.getOriginalAmount().subtract(totalPaid);

        List<LoanPayment> payments = loanPaymentRepository.findByLoanIdOrderByPaymentDateAscCreatedAtAsc(loan.getId());
        List<LoanPaymentResponse> paymentResponses = buildPaymentResponses(loan.getOriginalAmount(), payments);

        return new LoanDetailResponse(
                loan.getId(),
                loan.getName(),
                loan.getOriginalAmount(),
                remainingBalance,
                totalPaid,
                loan.getCreatedAt(),
                paymentResponses
        );
    }

    public LoanPaymentResponse recordPayment(Long loanId, LoanPaymentRequest request, User user) {
        Loan loan = findLoanByUser(loanId, user);
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        BigDecimal totalPaid = Objects.requireNonNullElse(
                loanPaymentRepository.sumPaymentsByLoanId(loan.getId()), BigDecimal.ZERO);
        BigDecimal remainingBalance = loan.getOriginalAmount().subtract(totalPaid);

        if (request.amount().compareTo(remainingBalance) > 0) {
            throw new IllegalArgumentException(
                    "Payment of $" + request.amount() + " exceeds remaining balance of $" + remainingBalance
            );
        }

        LoanPayment payment = new LoanPayment();
        payment.setLoan(loan);
        payment.setAmount(request.amount());
        payment.setPaymentDate(request.paymentDate());
        payment.setNote(request.note());
        payment = loanPaymentRepository.save(payment);

        BigDecimal balanceAfterPayment = remainingBalance.subtract(request.amount());

        return new LoanPaymentResponse(
                payment.getId(),
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getNote(),
                balanceAfterPayment,
                payment.getCreatedAt()
        );
    }

    public void deleteLoan(Long id, User user) {
        Loan loan = findLoanByUser(id, user);
        loanRepository.delete(loan);
    }

    public List<LoanPaymentResponse> copyPaymentsFromPreviousMonth(User user, int year, int month) {
        LocalDate targetMonth = LocalDate.of(year, month, 1);
        LocalDate prevStart = targetMonth.minusMonths(1);
        LocalDate prevEnd = targetMonth;

        List<LoanPayment> previousPayments = loanPaymentRepository.findByUserIdAndMonth(user.getId(), prevStart, prevEnd);

        if (previousPayments.isEmpty()) {
            throw new ResourceNotFoundException("No loan payments found in the previous month to copy");
        }

        List<LoanPaymentResponse> responses = new ArrayList<>();
        for (LoanPayment prev : previousPayments) {
            Loan loan = prev.getLoan();
            BigDecimal totalPaid = loanPaymentRepository.sumPaymentsByLoanId(loan.getId());
            BigDecimal remainingBalance = loan.getOriginalAmount().subtract(totalPaid);

            if (prev.getAmount().compareTo(remainingBalance) > 0) {
                continue;
            }

            LoanPayment copy = new LoanPayment();
            copy.setLoan(loan);
            copy.setAmount(prev.getAmount());
            int day = Math.min(prev.getPaymentDate().getDayOfMonth(), targetMonth.lengthOfMonth());
            copy.setPaymentDate(targetMonth.withDayOfMonth(day));
            copy.setNote(prev.getNote());
            copy = loanPaymentRepository.save(copy);

            BigDecimal balanceAfter = remainingBalance.subtract(copy.getAmount());
            responses.add(new LoanPaymentResponse(
                    copy.getId(),
                    copy.getAmount(),
                    copy.getPaymentDate(),
                    copy.getNote(),
                    balanceAfter,
                    copy.getCreatedAt()
            ));
        }

        return responses;
    }

    public void deletePayment(Long loanId, Long paymentId, User user) {
        Loan loan = findLoanByUser(loanId, user);
        LoanPayment payment = loanPaymentRepository.findByIdAndLoanId(paymentId, loan.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        loanPaymentRepository.delete(payment);
    }

    private Loan findLoanByUser(Long id, User user) {
        return loanRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
    }

    private LoanResponse toResponse(Loan loan) {
        BigDecimal totalPaid = Objects.requireNonNullElse(
                loanPaymentRepository.sumPaymentsByLoanId(loan.getId()), BigDecimal.ZERO);
        BigDecimal remainingBalance = loan.getOriginalAmount().subtract(totalPaid);
        return new LoanResponse(
                loan.getId(),
                loan.getName(),
                loan.getOriginalAmount(),
                remainingBalance,
                totalPaid,
                loan.getCreatedAt()
        );
    }

    private List<LoanPaymentResponse> buildPaymentResponses(BigDecimal originalAmount, List<LoanPayment> payments) {
        List<LoanPaymentResponse> responses = new ArrayList<>();
        BigDecimal runningBalance = originalAmount;
        for (LoanPayment payment : payments) {
            runningBalance = runningBalance.subtract(payment.getAmount());
            responses.add(new LoanPaymentResponse(
                    payment.getId(),
                    payment.getAmount(),
                    payment.getPaymentDate(),
                    payment.getNote(),
                    runningBalance,
                    payment.getCreatedAt()
            ));
        }
        return responses;
    }
}
