package com.goshen.expensetracker.service;

import com.goshen.expensetracker.exception.ResourceNotFoundException;
import com.goshen.expensetracker.model.dto.LinkedAccountResponse;
import com.goshen.expensetracker.model.entity.BankTransaction;
import com.goshen.expensetracker.model.entity.ExpenseCategory;
import com.goshen.expensetracker.model.entity.LinkedAccount;
import com.goshen.expensetracker.model.entity.User;
import com.goshen.expensetracker.repository.BankTransactionRepository;
import com.goshen.expensetracker.repository.ExpenseCategoryRepository;
import com.goshen.expensetracker.repository.LinkedAccountRepository;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlaidService {

    private final PlaidApi plaidApi;
    private final LinkedAccountRepository linkedAccountRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;

    public String createLinkToken(User user) throws IOException {
        LinkTokenCreateRequestUser requestUser = new LinkTokenCreateRequestUser()
                .clientUserId(user.getId().toString());

        LinkTokenCreateRequest request = new LinkTokenCreateRequest()
                .user(requestUser)
                .clientName("Goshen Expense Tracker")
                .products(Arrays.asList(Products.TRANSACTIONS))
                .countryCodes(Arrays.asList(CountryCode.US))
                .language("en");

        Response<LinkTokenCreateResponse> response = plaidApi.linkTokenCreate(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            return response.body().getLinkToken();
        }
        throw new RuntimeException("Failed to create Plaid link token");
    }

    public LinkedAccountResponse exchangeToken(String publicToken, String institutionName, User user) throws IOException {
        ItemPublicTokenExchangeRequest request = new ItemPublicTokenExchangeRequest()
                .publicToken(publicToken);

        Response<ItemPublicTokenExchangeResponse> response = plaidApi.itemPublicTokenExchange(request).execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new RuntimeException("Failed to exchange Plaid public token");
        }

        String accessToken = response.body().getAccessToken();
        String itemId = response.body().getItemId();

        // Fetch account details
        AccountsGetRequest accountsRequest = new AccountsGetRequest().accessToken(accessToken);
        Response<AccountsGetResponse> accountsResponse = plaidApi.accountsGet(accountsRequest).execute();

        String accountName = null;
        String accountMask = null;
        if (accountsResponse.isSuccessful() && accountsResponse.body() != null) {
            List<AccountBase> accounts = accountsResponse.body().getAccounts();
            if (!accounts.isEmpty()) {
                AccountBase first = accounts.get(0);
                accountName = first.getName();
                accountMask = first.getMask();
            }
        }

        LinkedAccount account = new LinkedAccount();
        account.setUser(user);
        account.setAccessToken(accessToken);
        account.setItemId(itemId);
        account.setInstitutionName(institutionName);
        account.setAccountName(accountName);
        account.setAccountMask(accountMask);
        account = linkedAccountRepository.save(account);

        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public List<LinkedAccountResponse> getLinkedAccounts(User user) {
        return linkedAccountRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void unlinkAccount(Long id, User user) throws IOException {
        LinkedAccount account = linkedAccountRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Linked account not found"));

        // Remove item from Plaid — abort local deletion if this fails
        try {
            ItemRemoveRequest request = new ItemRemoveRequest().accessToken(account.getAccessToken());
            Response<ItemRemoveResponse> plaidResponse = plaidApi.itemRemove(request).execute();
            if (!plaidResponse.isSuccessful()) {
                throw new IllegalStateException("Failed to remove Plaid item: non-successful response");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to remove Plaid item " + account.getItemId() + ": " + e.getMessage(), e);
        }

        bankTransactionRepository.deleteByLinkedAccountId(account.getId());
        linkedAccountRepository.delete(account);
    }

    public int syncTransactions(Long accountId, User user) throws IOException {
        LinkedAccount account = linkedAccountRepository.findByIdAndUserId(accountId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Linked account not found"));

        // Build category lookup for auto-categorization
        Map<String, ExpenseCategory> categoryMap = expenseCategoryRepository
                .findByUserIdOrderByNameAsc(user.getId())
                .stream()
                .collect(Collectors.toMap(
                        c -> c.getName().toLowerCase(),
                        c -> c,
                        (a, b) -> a
                ));

        int synced = 0;
        boolean hasMore = true;
        String cursor = account.getCursor();

        while (hasMore) {
            TransactionsSyncRequest request = new TransactionsSyncRequest()
                    .accessToken(account.getAccessToken());
            if (cursor != null) {
                request.cursor(cursor);
            }

            Response<TransactionsSyncResponse> response = plaidApi.transactionsSync(request).execute();
            if (!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException("Failed to sync transactions from Plaid");
            }

            TransactionsSyncResponse body = response.body();

            // Process added transactions
            for (Transaction txn : body.getAdded()) {
                if (bankTransactionRepository.existsByPlaidTransactionId(txn.getTransactionId())) {
                    continue;
                }

                // Plaid amounts: positive = debit (money spent), negative = credit
                BigDecimal amount = BigDecimal.valueOf(txn.getAmount());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue; // Skip credits/income
                }

                BankTransaction bt = new BankTransaction();
                bt.setUser(user);
                bt.setLinkedAccount(account);
                bt.setPlaidTransactionId(txn.getTransactionId());
                bt.setName(txn.getName());
                bt.setAmount(amount);
                bt.setTransactionDate(txn.getDate());

                // Plaid category
                List<String> categories = txn.getCategory();
                String plaidCategory = categories != null ? String.join(" > ", categories) : null;
                bt.setPlaidCategory(plaidCategory);

                // Auto-categorize
                bt.setCategory(autoCategorize(txn.getName(), plaidCategory, categoryMap));

                bankTransactionRepository.save(bt);
                synced++;
            }

            // Process modified transactions
            for (Transaction txn : body.getModified()) {
                bankTransactionRepository.findByPlaidTransactionId(txn.getTransactionId())
                        .ifPresent(bt -> {
                            BigDecimal amount = BigDecimal.valueOf(txn.getAmount());
                            bt.setName(txn.getName());
                            bt.setAmount(amount);
                            bt.setTransactionDate(txn.getDate());
                            List<String> categories = txn.getCategory();
                            String plaidCategory = categories != null ? String.join(" > ", categories) : null;
                            bt.setPlaidCategory(plaidCategory);
                            bt.setCategory(autoCategorize(txn.getName(), plaidCategory, categoryMap));
                            bankTransactionRepository.save(bt);
                        });
            }

            // Process removed transactions
            for (RemovedTransaction removed : body.getRemoved()) {
                bankTransactionRepository.findByPlaidTransactionId(removed.getTransactionId())
                        .ifPresent(bankTransactionRepository::delete);
            }

            cursor = body.getNextCursor();
            hasMore = body.getHasMore();
        }

        account.setCursor(cursor);
        linkedAccountRepository.save(account);

        return synced;
    }

    private ExpenseCategory autoCategorize(String name, String plaidCategory, Map<String, ExpenseCategory> categoryMap) {
        String lower = name.toLowerCase();

        // Simple keyword matching against user's categories
        for (Map.Entry<String, ExpenseCategory> entry : categoryMap.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Match on Plaid category keywords
        if (plaidCategory != null) {
            String plaidLower = plaidCategory.toLowerCase();
            if (plaidLower.contains("food") || plaidLower.contains("restaurant")) {
                return categoryMap.get("food");
            }
            if (plaidLower.contains("gas") || plaidLower.contains("fuel")) {
                return categoryMap.get("gas");
            }
            if (plaidLower.contains("rent") || plaidLower.contains("mortgage")) {
                return categoryMap.get("rent");
            }
            if (plaidLower.contains("electric") || plaidLower.contains("utility")) {
                return categoryMap.get("electricity");
            }
        }

        return null; // Uncategorized
    }

    private LinkedAccountResponse toResponse(LinkedAccount account) {
        return new LinkedAccountResponse(
                account.getId(),
                account.getInstitutionName(),
                account.getAccountName(),
                account.getAccountMask(),
                account.getCreatedAt()
        );
    }
}
