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
import java.util.ArrayList;
import java.util.List;

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
        BigDecimal totalPaid = loanPaymentRepository.sumPaymentsByLoanId(loan.getId());
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
        BigDecimal totalPaid = loanPaymentRepository.sumPaymentsByLoanId(loan.getId());
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
        BigDecimal totalPaid = loanPaymentRepository.sumPaymentsByLoanId(loan.getId());
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
