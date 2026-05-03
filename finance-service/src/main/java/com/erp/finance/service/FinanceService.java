package com.erp.finance.service;

import com.erp.finance.entity.*;
import com.erp.finance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinanceService {

    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final InvoiceRepository invoiceRepository;

    // ============================================================
    // Chart of Accounts
    // ============================================================

    @Transactional
    public Account createAccount(Account account) {
        if (accountRepository.findByAccountNumber(account.getAccountNumber()).isPresent()) {
            throw new IllegalArgumentException("Account number already exists: " + account.getAccountNumber());
        }

        // Auto-determine normal balance from account type
        if (account.getNormalBalance() == null) {
            account.setNormalBalance(switch (account.getAccountType()) {
                case ASSET, EXPENSE -> Account.NormalBalance.DEBIT;
                case LIABILITY, EQUITY, REVENUE -> Account.NormalBalance.CREDIT;
            });
        }

        account.setBalanceCents(0L);
        account.setIsActive(true);
        return accountRepository.save(account);
    }

    @Cacheable(value = "accounts", key = "#id")
    public Account getAccount(String id) {
        return accountRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
    }

    public List<Account> listAccounts(Account.AccountType type, boolean activeOnly, String search) {
        if (search != null && !search.isEmpty()) {
            return accountRepository.searchAccounts(search);
        }
        if (type != null) {
            return accountRepository.findByAccountTypeAndIsActiveTrue(type);
        }
        return activeOnly ? accountRepository.findByIsActiveTrue() : accountRepository.findAll();
    }

    // ============================================================
    // Journal Entries - Double Entry Bookkeeping
    // ============================================================

    @Transactional
    public JournalEntry createJournalEntry(JournalEntry entry, List<JournalLineRequest> lineRequests) {
        // Generate entry number
        Integer seq = journalEntryRepository.getNextEntrySequence();
        entry.setEntryNumber(String.format("JE-%06d", seq));
        entry.setStatus(JournalEntry.JournalStatus.DRAFT);

        // Validate and create lines
        long totalDebit = 0;
        long totalCredit = 0;
        int lineNum = 1;

        for (JournalLineRequest req : lineRequests) {
            Account account = accountRepository.findById(req.accountId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found: " + req.accountId()));

            if (req.debitCents() > 0 && req.creditCents() > 0) {
                throw new IllegalArgumentException("Line cannot have both debit and credit amounts");
            }
            if (req.debitCents() == 0 && req.creditCents() == 0) {
                throw new IllegalArgumentException("Line must have either debit or credit amount");
            }

            JournalLine line = JournalLine.builder()
                    .lineNumber(lineNum++)
                    .account(account)
                    .debitAmountCents(req.debitCents())
                    .creditAmountCents(req.creditCents())
                    .description(req.description())
                    .departmentId(req.departmentId())
                    .costCenter(req.costCenter())
                    .currency(entry.getCurrency())
                    .build();

            entry.addLine(line);
            totalDebit += req.debitCents();
            totalCredit += req.creditCents();
        }

        // FUNDAMENTAL ACCOUNTING RULE: debits must equal credits
        if (totalDebit != totalCredit) {
            throw new IllegalArgumentException(
                    String.format("Journal entry is unbalanced: debits=%d, credits=%d", totalDebit, totalCredit));
        }

        entry.setTotalDebitCents(totalDebit);
        entry.setTotalCreditCents(totalCredit);

        // Determine fiscal period
        LocalDate entryDate = entry.getEntryDate().atZone(ZoneOffset.UTC).toLocalDate();
        entry.setFiscalPeriod(entryDate.getYear() + "-" + String.format("%02d", entryDate.getMonthValue()));

        log.info("Created journal entry {} with {} lines, total debit/credit: {}",
                entry.getEntryNumber(), lineRequests.size(), totalDebit);

        return journalEntryRepository.save(entry);
    }

    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public JournalEntry postJournalEntry(String entryId, String postedBy) {
        JournalEntry entry = journalEntryRepository.findById(UUID.fromString(entryId))
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found"));

        if (entry.getStatus() != JournalEntry.JournalStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT entries can be posted. Current: " + entry.getStatus());
        }

        // Update account balances
        for (JournalLine line : entry.getLines()) {
            Account account = line.getAccount();
            long balanceChange;

            // Apply debit/credit based on normal balance
            if (account.getNormalBalance() == Account.NormalBalance.DEBIT) {
                balanceChange = line.getDebitAmountCents() - line.getCreditAmountCents();
            } else {
                balanceChange = line.getCreditAmountCents() - line.getDebitAmountCents();
            }

            account.setBalanceCents(account.getBalanceCents() + balanceChange);
            accountRepository.save(account);

            log.debug("Account {} balance updated by {} cents (new balance: {})",
                    account.getAccountNumber(), balanceChange, account.getBalanceCents());
        }

        entry.setStatus(JournalEntry.JournalStatus.POSTED);
        entry.setUpdatedBy(postedBy);

        log.info("Posted journal entry {}", entry.getEntryNumber());
        return journalEntryRepository.save(entry);
    }

    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public JournalEntry reverseJournalEntry(String entryId, String reason, String reversedBy) {
        JournalEntry original = journalEntryRepository.findById(UUID.fromString(entryId))
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found"));

        if (original.getStatus() != JournalEntry.JournalStatus.POSTED) {
            throw new IllegalStateException("Only POSTED entries can be reversed");
        }

        // Create reversal entry (swap debits and credits)
        JournalEntry reversal = new JournalEntry();
        reversal.setEntryDate(Instant.now());
        reversal.setDescription("REVERSAL of " + original.getEntryNumber() + ": " + reason);
        reversal.setSourceType("REVERSAL");
        reversal.setSourceId(original.getId().toString());
        reversal.setCurrency(original.getCurrency());
        reversal.setCreatedBy(reversedBy);

        List<JournalLineRequest> reversalLines = new ArrayList<>();
        for (JournalLine origLine : original.getLines()) {
            // Swap debit and credit
            reversalLines.add(new JournalLineRequest(
                    origLine.getAccount().getId(),
                    origLine.getCreditAmountCents(), // was credit, now debit
                    origLine.getDebitAmountCents(),   // was debit, now credit
                    "Reversal: " + origLine.getDescription(),
                    origLine.getDepartmentId(),
                    origLine.getCostCenter()
            ));
        }

        JournalEntry savedReversal = createJournalEntry(reversal, reversalLines);
        postJournalEntry(savedReversal.getId().toString(), reversedBy);

        original.setStatus(JournalEntry.JournalStatus.REVERSED);
        original.setUpdatedBy(reversedBy);
        journalEntryRepository.save(original);

        log.info("Reversed journal entry {} with reversal {}", original.getEntryNumber(), savedReversal.getEntryNumber());
        return savedReversal;
    }

    public Page<JournalEntry> listJournalEntries(JournalEntry.JournalStatus status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "entryDate"));
        if (status != null) {
            return journalEntryRepository.findByStatus(status, pageRequest);
        }
        return journalEntryRepository.findAll(pageRequest);
    }

    // ============================================================
    // Invoice Management
    // ============================================================

    @Transactional
    public Invoice createInvoice(Invoice invoice, List<InvoiceLineRequest> lineRequests) {
        Integer seq = invoiceRepository.getNextInvoiceSequence();
        String prefix = switch (invoice.getInvoiceType()) {
            case SALES -> "INV";
            case PURCHASE -> "BILL";
            case CREDIT_NOTE -> "CN";
            case DEBIT_NOTE -> "DN";
        };
        invoice.setInvoiceNumber(String.format("%s-%06d", prefix, seq));

        long subtotal = 0;
        long totalTax = 0;
        int lineNum = 1;

        for (InvoiceLineRequest req : lineRequests) {
            long lineTotal = (long) req.quantity() * req.unitPriceCents();
            long taxAmount = BigDecimal.valueOf(lineTotal).multiply(req.taxRate()).longValue();

            InvoiceLine line = InvoiceLine.builder()
                    .lineNumber(lineNum++)
                    .description(req.description())
                    .productId(req.productId())
                    .quantity(req.quantity())
                    .unitPriceCents(req.unitPriceCents())
                    .taxRate(req.taxRate())
                    .taxAmountCents(taxAmount)
                    .lineTotalCents(lineTotal)
                    .currency(invoice.getCurrency())
                    .build();

            line.setInvoice(invoice);
            invoice.getLines().add(line);
            subtotal += lineTotal;
            totalTax += taxAmount;
        }

        invoice.setSubtotalCents(subtotal);
        invoice.setTaxAmountCents(totalTax);
        invoice.setTotalAmountCents(subtotal + totalTax - invoice.getDiscountAmountCents());
        invoice.setAmountPaidCents(0L);
        invoice.setAmountDueCents(invoice.getTotalAmountCents());

        // Calculate due date from payment terms
        if (invoice.getDueDate() == null && invoice.getPaymentTerms() != null) {
            int days = switch (invoice.getPaymentTerms()) {
                case "NET15" -> 15;
                case "NET30" -> 30;
                case "NET60" -> 60;
                case "NET90" -> 90;
                case "COD" -> 0;
                default -> 30;
            };
            invoice.setDueDate(invoice.getInvoiceDate().plus(days, ChronoUnit.DAYS));
        }

        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        Invoice saved = invoiceRepository.save(invoice);

        // Auto-create journal entry for the invoice
        createInvoiceJournalEntry(saved);

        log.info("Created invoice {} for customer {}, total: {}",
                saved.getInvoiceNumber(), saved.getCustomerName(), saved.getTotalAmountCents());
        return saved;
    }

    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public Invoice recordPayment(String invoiceId, long amountCents, String paymentMethod,
                                  String reference, Instant paymentDate, String recordedBy) {
        Invoice invoice = invoiceRepository.findById(UUID.fromString(invoiceId))
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID ||
            invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Cannot record payment for " + invoice.getStatus() + " invoice");
        }

        if (amountCents <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        if (amountCents > invoice.getAmountDueCents()) {
            throw new IllegalArgumentException("Payment exceeds amount due: " +
                    amountCents + " > " + invoice.getAmountDueCents());
        }

        invoice.setAmountPaidCents(invoice.getAmountPaidCents() + amountCents);
        invoice.setAmountDueCents(invoice.getTotalAmountCents() - invoice.getAmountPaidCents());

        if (invoice.getAmountDueCents() <= 0) {
            invoice.setStatus(Invoice.InvoiceStatus.PAID);
            invoice.setPaidDate(paymentDate);
        } else {
            invoice.setStatus(Invoice.InvoiceStatus.PARTIALLY_PAID);
        }

        // Create payment journal entry (Debit: Bank/Cash, Credit: AR)
        JournalEntry paymentJE = new JournalEntry();
        paymentJE.setEntryDate(paymentDate);
        paymentJE.setDescription("Payment for " + invoice.getInvoiceNumber() + " via " + paymentMethod);
        paymentJE.setSourceType("PAYMENT");
        paymentJE.setSourceId(invoiceId);
        paymentJE.setCurrency(invoice.getCurrency());
        paymentJE.setCreatedBy(recordedBy);

        Account bankAccount = accountRepository.findByAccountNumber("1010")
                .orElseThrow(() -> new RuntimeException("Bank account not found"));
        Account arAccount = accountRepository.findByAccountNumber("1200")
                .orElseThrow(() -> new RuntimeException("AR account not found"));

        List<JournalLineRequest> paymentLines = List.of(
                new JournalLineRequest(bankAccount.getId(), amountCents, 0L, "Payment received", null, null),
                new JournalLineRequest(arAccount.getId(), 0L, amountCents, "AR reduction", null, null)
        );

        JournalEntry savedJE = createJournalEntry(paymentJE, paymentLines);
        postJournalEntry(savedJE.getId().toString(), recordedBy);

        log.info("Recorded payment of {} for invoice {}, remaining: {}",
                amountCents, invoice.getInvoiceNumber(), invoice.getAmountDueCents());

        return invoiceRepository.save(invoice);
    }

    // ============================================================
    // Financial Reports
    // ============================================================

    /**
     * Trial Balance - lists all accounts with their debit/credit balances.
     * Debits should equal credits for a balanced set of books.
     */
    public TrialBalanceReport getTrialBalance() {
        List<Account> accounts = accountRepository.findByIsActiveTrue();
        long totalDebit = 0;
        long totalCredit = 0;

        List<TrialBalanceReport.Line> lines = new ArrayList<>();
        for (Account account : accounts) {
            if (account.getBalanceCents() == 0) continue;

            long debit = 0, credit = 0;
            if (account.getNormalBalance() == Account.NormalBalance.DEBIT) {
                if (account.getBalanceCents() >= 0) {
                    debit = account.getBalanceCents();
                } else {
                    credit = Math.abs(account.getBalanceCents());
                }
            } else {
                if (account.getBalanceCents() >= 0) {
                    credit = account.getBalanceCents();
                } else {
                    debit = Math.abs(account.getBalanceCents());
                }
            }

            lines.add(new TrialBalanceReport.Line(
                    account.getAccountNumber(), account.getName(),
                    account.getAccountType(), debit, credit));
            totalDebit += debit;
            totalCredit += credit;
        }

        lines.sort(Comparator.comparing(TrialBalanceReport.Line::accountNumber));
        return new TrialBalanceReport(lines, totalDebit, totalCredit, totalDebit == totalCredit);
    }

    /**
     * Profit & Loss Statement (Income Statement)
     */
    public ProfitLossReport getProfitAndLoss() {
        List<Account> accounts = accountRepository.findIncomeStatementAccounts();

        Map<String, List<Account>> revenueBySubType = accounts.stream()
                .filter(a -> a.getAccountType() == Account.AccountType.REVENUE)
                .collect(Collectors.groupingBy(a -> a.getSubType() != null ? a.getSubType() : "OTHER"));

        Map<String, List<Account>> expenseBySubType = accounts.stream()
                .filter(a -> a.getAccountType() == Account.AccountType.EXPENSE)
                .collect(Collectors.groupingBy(a -> a.getSubType() != null ? a.getSubType() : "OTHER"));

        long totalRevenue = accounts.stream()
                .filter(a -> a.getAccountType() == Account.AccountType.REVENUE)
                .mapToLong(Account::getBalanceCents).sum();

        long totalExpenses = accounts.stream()
                .filter(a -> a.getAccountType() == Account.AccountType.EXPENSE)
                .mapToLong(Account::getBalanceCents).sum();

        return new ProfitLossReport(
                buildSections(revenueBySubType),
                buildSections(expenseBySubType),
                totalRevenue, totalExpenses, totalRevenue - totalExpenses);
    }

    /**
     * Balance Sheet - Assets = Liabilities + Equity
     */
    public BalanceSheetReport getBalanceSheet() {
        List<Account> accounts = accountRepository.findBalanceSheetAccounts();

        Map<String, List<Account>> assetsBySubType = accounts.stream()
                .filter(a -> a.getAccountType() == Account.AccountType.ASSET)
                .collect(Collectors.groupingBy(a -> a.getSubType() != null ? a.getSubType() : "OTHER"));

        Map<String, List<Account>> liabilitiesBySubType = accounts.stream()
                .filter(a -> a.getAccountType() == Account.AccountType.LIABILITY)
                .collect(Collectors.groupingBy(a -> a.getSubType() != null ? a.getSubType() : "OTHER"));

        Map<String, List<Account>> equityBySubType = accounts.stream()
                .filter(a -> a.getAccountType() == Account.AccountType.EQUITY)
                .collect(Collectors.groupingBy(a -> a.getSubType() != null ? a.getSubType() : "OTHER"));

        long totalAssets = accounts.stream()
                .filter(a -> a.getAccountType() == Account.AccountType.ASSET)
                .mapToLong(Account::getBalanceCents).sum();
        long totalLiabilities = accounts.stream()
                .filter(a -> a.getAccountType() == Account.AccountType.LIABILITY)
                .mapToLong(Account::getBalanceCents).sum();
        long totalEquity = accounts.stream()
                .filter(a -> a.getAccountType() == Account.AccountType.EQUITY)
                .mapToLong(Account::getBalanceCents).sum();

        return new BalanceSheetReport(
                buildSections(assetsBySubType),
                buildSections(liabilitiesBySubType),
                buildSections(equityBySubType),
                totalAssets, totalLiabilities, totalEquity,
                totalAssets == (totalLiabilities + totalEquity),
                Instant.now());
    }

    /**
     * Accounts Receivable Aging
     */
    public ARAgingReport getAccountsReceivableAging() {
        List<Invoice> overdue = invoiceRepository.findOverdueInvoices(Instant.now());
        Instant now = Instant.now();

        Map<String, List<Invoice>> buckets = new LinkedHashMap<>();
        buckets.put("Current", new ArrayList<>());
        buckets.put("1-30", new ArrayList<>());
        buckets.put("31-60", new ArrayList<>());
        buckets.put("61-90", new ArrayList<>());
        buckets.put("90+", new ArrayList<>());

        List<Invoice> allOutstanding = invoiceRepository.findAll().stream()
                .filter(i -> i.getAmountDueCents() > 0 &&
                        i.getStatus() != Invoice.InvoiceStatus.CANCELLED &&
                        i.getStatus() != Invoice.InvoiceStatus.VOID)
                .toList();

        for (Invoice inv : allOutstanding) {
            long daysOverdue = ChronoUnit.DAYS.between(inv.getDueDate(), now);
            if (daysOverdue <= 0) {
                buckets.get("Current").add(inv);
            } else if (daysOverdue <= 30) {
                buckets.get("1-30").add(inv);
            } else if (daysOverdue <= 60) {
                buckets.get("31-60").add(inv);
            } else if (daysOverdue <= 90) {
                buckets.get("61-90").add(inv);
            } else {
                buckets.get("90+").add(inv);
            }
        }

        long totalOutstanding = allOutstanding.stream().mapToLong(Invoice::getAmountDueCents).sum();
        List<ARAgingReport.Bucket> result = buckets.entrySet().stream()
                .map(e -> new ARAgingReport.Bucket(
                        e.getKey(),
                        e.getValue().stream().mapToLong(Invoice::getAmountDueCents).sum(),
                        e.getValue().size()))
                .toList();

        return new ARAgingReport(result, totalOutstanding);
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private void createInvoiceJournalEntry(Invoice invoice) {
        if (invoice.getInvoiceType() != Invoice.InvoiceType.SALES) return;

        JournalEntry je = new JournalEntry();
        je.setEntryDate(invoice.getInvoiceDate());
        je.setDescription("Sales Invoice " + invoice.getInvoiceNumber());
        je.setSourceType("SALES_INVOICE");
        je.setSourceId(invoice.getId().toString());
        je.setCurrency(invoice.getCurrency());
        je.setCreatedBy(invoice.getCreatedBy());

        Account arAccount = accountRepository.findByAccountNumber("1200")
                .orElseThrow(() -> new RuntimeException("AR account not found"));
        Account revenueAccount = accountRepository.findByAccountNumber("4000")
                .orElseThrow(() -> new RuntimeException("Revenue account not found"));
        Account taxAccount = accountRepository.findByAccountNumber("2200")
                .orElseThrow(() -> new RuntimeException("Tax Payable account not found"));

        List<JournalLineRequest> lines = new ArrayList<>();

        // Debit: Accounts Receivable (full amount)
        lines.add(new JournalLineRequest(
                arAccount.getId(), invoice.getTotalAmountCents(), 0L,
                "AR - " + invoice.getCustomerName(), null, null));

        // Credit: Revenue (subtotal)
        lines.add(new JournalLineRequest(
                revenueAccount.getId(), 0L, invoice.getSubtotalCents(),
                "Revenue", null, null));

        // Credit: Tax Payable (tax amount)
        if (invoice.getTaxAmountCents() > 0) {
            lines.add(new JournalLineRequest(
                    taxAccount.getId(), 0L, invoice.getTaxAmountCents(),
                    "Tax Payable", null, null));
        }

        JournalEntry savedJE = createJournalEntry(je, lines);
        postJournalEntry(savedJE.getId().toString(), invoice.getCreatedBy());

        invoice.setJournalEntryId(savedJE.getId());
    }

    private List<ReportSection> buildSections(Map<String, List<Account>> grouped) {
        return grouped.entrySet().stream()
                .map(e -> {
                    List<ReportSection.Line> lines = e.getValue().stream()
                            .map(a -> new ReportSection.Line(a.getAccountNumber(), a.getName(), a.getBalanceCents()))
                            .toList();
                    long total = e.getValue().stream().mapToLong(Account::getBalanceCents).sum();
                    return new ReportSection(e.getKey(), lines, total);
                })
                .toList();
    }

    // ============================================================
    // Request / Report Records
    // ============================================================

    public record JournalLineRequest(UUID accountId, long debitCents, long creditCents,
                                     String description, String departmentId, String costCenter) {}

    public record InvoiceLineRequest(String description, UUID productId, int quantity,
                                     long unitPriceCents, BigDecimal taxRate, UUID accountId) {}

    public record ReportSection(String name, List<Line> lines, long sectionTotal) {
        public record Line(String accountNumber, String accountName, long amount) {}
    }

    public record TrialBalanceReport(List<Line> lines, long totalDebit, long totalCredit, boolean balanced) {
        public record Line(String accountNumber, String accountName, Account.AccountType type, long debit, long credit) {}
    }

    public record ProfitLossReport(List<ReportSection> revenueSections, List<ReportSection> expenseSections,
                                   long totalRevenue, long totalExpenses, long netIncome) {}

    public record BalanceSheetReport(List<ReportSection> assets, List<ReportSection> liabilities,
                                     List<ReportSection> equity, long totalAssets, long totalLiabilities,
                                     long totalEquity, boolean balanced, Instant asOfDate) {}

    public record ARAgingReport(List<Bucket> buckets, long totalOutstanding) {
        public record Bucket(String label, long amount, int invoiceCount) {}
    }
}
