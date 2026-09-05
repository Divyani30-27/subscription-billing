package com.busy.subscription_billing;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.busy.subscription_billing.model.Invoice;
import com.busy.subscription_billing.model.Subscription;
import com.busy.subscription_billing.model.CreditNote;
import com.busy.subscription_billing.repository.InvoiceRepository;
import com.busy.subscription_billing.repository.SubscriptionRepository;
import com.busy.subscription_billing.repository.CreditNoteRepository;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.nio.charset.StandardCharsets;

import java.util.List;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CreditNoteRepository creditNoteRepository;

    public InvoiceController(
        InvoiceRepository invoiceRepository,
        SubscriptionRepository subscriptionRepository,
        CreditNoteRepository creditNoteRepository) {

    this.invoiceRepository = invoiceRepository;
    this.subscriptionRepository = subscriptionRepository;
    this.creditNoteRepository = creditNoteRepository;
}

    @GetMapping
public Map<String, Object> getAll(
        HttpSession session,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Boolean overdue,
        @RequestParam(required = false) Long ownerId,
        @RequestParam(defaultValue = "dueDate") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

    checkLoggedIn(session);

    String role = (String) session.getAttribute("role");
    Long userId = (Long) session.getAttribute("userId");

    List<Invoice> invoices = invoiceRepository.findAll();

    List<Invoice> filtered = invoices.stream()
            .filter(invoice -> {
                Subscription subscription = subscriptionRepository
                        .findById(invoice.getSubscriptionId())
                        .orElse(null);

                if (subscription == null) {
                    return false;
                }

                if ("ACCOUNT_MANAGER".equals(role)) {
                    boolean owner = userId.equals(subscription.getOwnerId());

                    boolean collaborator =
                            subscription.getCollaboratorIds() != null
                            && subscription.getCollaboratorIds().contains(userId);

                    if (!owner && !collaborator) {
                        return false;
                    }
                }

                if (ownerId != null
                        && !ownerId.equals(subscription.getOwnerId())) {
                    return false;
                }

                if (search != null && !search.trim().isEmpty()) {
                    String text = search.toLowerCase();

                    boolean matches =
                            subscription.getCustomerName() != null
                            && subscription.getCustomerName()
                                    .toLowerCase()
                                    .contains(text);

                    boolean emailMatches =
                            subscription.getBillingEmail() != null
                            && subscription.getBillingEmail()
                                    .toLowerCase()
                                    .contains(text);

                    if (!matches && !emailMatches) {
                        return false;
                    }
                }

                if (status != null
                        && !status.equalsIgnoreCase(invoice.getStatus())) {
                    return false;
                }

                boolean isOverdue =
                        "ISSUED".equals(invoice.getStatus())
                        && invoice.getDueDate() != null
                        && invoice.getDueDate().isBefore(LocalDate.now());

                if (overdue != null && overdue != isOverdue) {
                    return false;
                }

                return true;
            })
            .collect(Collectors.toList());

    Comparator<Invoice> comparator;

    if ("amount".equalsIgnoreCase(sortBy)) {
        comparator = Comparator.comparing(
                Invoice::getAmount,
                Comparator.nullsLast(Comparator.naturalOrder()));
    } else if ("status".equalsIgnoreCase(sortBy)) {
        comparator = Comparator.comparing(
                Invoice::getStatus,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
    } else {
        comparator = Comparator.comparing(
                Invoice::getDueDate,
                Comparator.nullsLast(Comparator.naturalOrder()));
    }

    if ("desc".equalsIgnoreCase(sortDir)) {
        comparator = comparator.reversed();
    }

    filtered.sort(comparator);

    int total = filtered.size();

    int from = Math.min(page * size, total);
    int to = Math.min(from + size, total);

    List<Invoice> result = filtered.subList(from, to);

    Map<String, Object> response = new HashMap<>();
    response.put("invoices", result);
    response.put("total", total);
    response.put("page", page);
    response.put("size", size);

    return response;
}

    @PostMapping
    public Invoice create(
            @RequestBody Invoice invoice,
            HttpSession session) {

        checkLoggedIn(session);

        Subscription subscription = subscriptionRepository
                .findById(invoice.getSubscriptionId())
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        checkSubscriptionAccess(subscription, session);

        return invoiceRepository.save(invoice);
    }

    @GetMapping("/{id}")
    public Invoice getById(@PathVariable Long id) {
        return invoiceRepository.findById(id).orElseThrow();
    }

    @PutMapping("/{id}")
public Invoice update(
        @PathVariable Long id,
        @RequestBody Invoice updated,
        HttpSession session) {

    checkLoggedIn(session);

    Invoice invoice = findInvoice(id);

    Subscription subscription = subscriptionRepository
            .findById(invoice.getSubscriptionId())
            .orElseThrow(() -> new RuntimeException("Subscription not found"));

    checkSubscriptionAccess(subscription, session);

    if ("PAID".equals(invoice.getStatus())) {
        throw new RuntimeException("Paid invoice cannot be edited");
    }

    if ("DRAFT".equals(invoice.getStatus())) {
        invoice.setBillingPeriodStart(updated.getBillingPeriodStart());
        invoice.setBillingPeriodEnd(updated.getBillingPeriodEnd());
        invoice.setAmount(updated.getAmount());
    }

    invoice.setDueDate(updated.getDueDate());

    return invoiceRepository.save(invoice);
}

    @PostMapping("/{id}/issue")
    public Invoice issue(
            @PathVariable Long id,
            HttpSession session) {

        checkAdmin(session);

        Invoice invoice = findInvoice(id);

        if (!"DRAFT".equals(invoice.getStatus())) {
            throw new RuntimeException(
                    "Only Draft invoices can be issued");
        }

        invoice.setStatus("ISSUED");
        return invoiceRepository.save(invoice);
    }

    @PostMapping("/{id}/pay")
    public Invoice pay(
            @PathVariable Long id,
            HttpSession session) {

        checkAdmin(session);

        Invoice invoice = findInvoice(id);

        if (!"ISSUED".equals(invoice.getStatus())) {
            throw new RuntimeException(
                    "Only Issued invoices can be marked as Paid");
        }

        invoice.setStatus("PAID");
        return invoiceRepository.save(invoice);
    }

    @PostMapping("/bulk-generate")
public List<Map<String, Object>> bulkGenerate(HttpSession session) {

    checkAdmin(session);

    LocalDate today = LocalDate.now();
    LocalDate periodStart = today.withDayOfMonth(1);
    LocalDate periodEnd = today.with(
            TemporalAdjusters.lastDayOfMonth());

    List<Map<String, Object>> report = new ArrayList<>();

    List<Subscription> subscriptions = subscriptionRepository.findAll()
            .stream()
            .filter(s -> !s.isArchived())
            .collect(java.util.stream.Collectors.toList());

    for (Subscription subscription : subscriptions) {

        Map<String, Object> result = new HashMap<>();
        result.put("subscriptionId", subscription.getId());

        try {
            boolean alreadyExists =
                    invoiceRepository
                            .findBySubscriptionIdAndBillingPeriodStartAndBillingPeriodEnd(
                                    subscription.getId(),
                                    periodStart,
                                    periodEnd)
                            .isPresent();

            if (alreadyExists) {
                result.put("status", "skipped");
                result.put("reason", "Invoice already exists");
            } else {

                Invoice invoice = new Invoice();
                invoice.setSubscriptionId(subscription.getId());
                invoice.setBillingPeriodStart(periodStart);
                invoice.setBillingPeriodEnd(periodEnd);
                invoice.setAmount(subscription.getPrice());
                invoice.setDueDate(periodEnd);
                invoice.setStatus("DRAFT");

                invoiceRepository.save(invoice);

                result.put("status", "generated");
                result.put("invoiceId", invoice.getId());
            }

        } catch (Exception e) {
            result.put("status", "failed");
            result.put("reason", e.getMessage());
        }

        report.add(result);
    }

    return report;
}

  @GetMapping("/receivables/export")
public ResponseEntity<byte[]> exportReceivables(HttpSession session) {

    checkLoggedIn(session);

    String role = (String) session.getAttribute("role");
    Long userId = (Long) session.getAttribute("userId");

    StringBuilder csv = new StringBuilder();

    csv.append("Invoice ID,Subscription ID,Customer Name,Amount,Due Date,Status\n");

    for (Invoice invoice : invoiceRepository.findAll()) {

        if (!"ISSUED".equals(invoice.getStatus())) {
            continue;
        }

        Subscription subscription = subscriptionRepository
                .findById(invoice.getSubscriptionId())
                .orElse(null);

        if (subscription == null) {
            continue;
        }

        if ("ACCOUNT_MANAGER".equals(role)) {

            boolean owner = userId.equals(subscription.getOwnerId());

            boolean collaborator =
                    subscription.getCollaboratorIds() != null
                    && subscription.getCollaboratorIds().contains(userId);

            if (!owner && !collaborator) {
                continue;
            }
        }

        csv.append(invoice.getId()).append(",");
        csv.append(subscription.getId()).append(",");
        csv.append("\"")
                .append(subscription.getCustomerName())
                .append("\"").append(",");
        csv.append(invoice.getAmount()).append(",");
        csv.append(invoice.getDueDate()).append(",");
        csv.append(invoice.getStatus()).append("\n");
    }

    byte[] data = csv.toString().getBytes(StandardCharsets.UTF_8);

    return ResponseEntity.ok()
            .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=receivables.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(data);
}

    private Invoice findInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    private void checkLoggedIn(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            throw new RuntimeException("Please login first");
        }
    }

    private void checkAdmin(HttpSession session) {
    checkLoggedIn(session);

    if (!"BILLING_ADMIN".equals(session.getAttribute("role"))) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only Billing Admin can perform this action");
    }
}

  private void checkSubscriptionAccess(
        Subscription subscription,
        HttpSession session) {

    String role = (String) session.getAttribute("role");

    if ("BILLING_ADMIN".equals(role)) {
        return;
    }

    Long userId = (Long) session.getAttribute("userId");

    boolean isOwner = userId.equals(subscription.getOwnerId());

    boolean isCollaborator =
            subscription.getCollaboratorIds() != null
            && subscription.getCollaboratorIds().contains(userId);

    if (!"ACCOUNT_MANAGER".equals(role)
            || (!isOwner && !isCollaborator)) {

        throw new RuntimeException(
                "You do not have access to this subscription");
    }
}

    @PostMapping("/{id}/void")
public Invoice voidInvoice(
        @PathVariable Long id,
        @RequestBody String reason,
        HttpSession session) {

    checkAdmin(session);

    if (reason == null || reason.trim().isEmpty()) {
        throw new RuntimeException("Void reason is required");
    }

    Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Invoice not found"));

    if ("PAID".equals(invoice.getStatus())) {
        throw new RuntimeException("Paid invoice cannot be voided");
    }

    invoice.setStatus("VOID");
    return invoiceRepository.save(invoice);
}


@PostMapping("/{id}/credit-note")
public CreditNote createCreditNote(
        @PathVariable Long id,
        @RequestBody CreditNote creditNote,
        HttpSession session) {

    checkAdmin(session);

    Invoice invoice = findInvoice(id);

    if (!"PAID".equals(invoice.getStatus())) {
        throw new RuntimeException(
                "Credit note can be created only for paid invoices");
    }

    if (creditNote.getReason() == null ||
            creditNote.getReason().isBlank()) {
        throw new RuntimeException("Credit note reason is required");
    }

    if (creditNote.getAmount() == null ||
            creditNote.getAmount().signum() <= 0) {
        throw new RuntimeException("Credit note amount must be positive");
    }

    if (creditNote.getAmount().compareTo(invoice.getAmount()) > 0) {
        throw new RuntimeException(
                "Credit note amount cannot exceed invoice amount");
    }

    creditNote.setInvoiceId(invoice.getId());

    return creditNoteRepository.save(creditNote);
}
}