package com.busy.subscription_billing;

import com.busy.subscription_billing.model.Invoice;
import com.busy.subscription_billing.model.Subscription;
import com.busy.subscription_billing.repository.SubscriptionRepository;
import com.busy.subscription_billing.repository.InvoiceRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DashboardController {

    private final SubscriptionRepository subscriptionRepository;

    private final InvoiceRepository invoiceRepository;

    public DashboardController(
        InvoiceRepository invoiceRepository,
        SubscriptionRepository subscriptionRepository) {

    this.invoiceRepository = invoiceRepository;
    this.subscriptionRepository = subscriptionRepository;
}

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(HttpSession session) {

        if (session.getAttribute("userId") == null) {
            throw new RuntimeException("Please login first");
        }

        List<Invoice> invoices = invoiceRepository.findAll();

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        BigDecimal collected = BigDecimal.ZERO;
        BigDecimal outstanding = BigDecimal.ZERO;
        BigDecimal overdueAmount = BigDecimal.ZERO;

        int issuedThisMonth = 0;
        int overdueCount = 0;

        Map<String, Integer> statusBreakdown = new HashMap<>();

        for (Invoice invoice : invoices) {

            String status = invoice.getStatus();

            statusBreakdown.put(
                    status,
                    statusBreakdown.getOrDefault(status, 0) + 1
            );

            if ("ISSUED".equals(status)) {

                if (invoice.getBillingPeriodStart() != null
                        && !invoice.getBillingPeriodStart().isBefore(monthStart)) {
                    issuedThisMonth++;
                }

                if (invoice.getAmount() != null) {
                    outstanding = outstanding.add(invoice.getAmount());
                }

                if (invoice.getDueDate() != null
                        && invoice.getDueDate().isBefore(today)) {

                    overdueCount++;

                    if (invoice.getAmount() != null) {
                        overdueAmount =
                                overdueAmount.add(invoice.getAmount());
                    }
                }
            }

            if ("PAID".equals(status)
                    && invoice.getAmount() != null) {

                collected = collected.add(invoice.getAmount());
            }
        }
        
        Map<String, Integer> planBreakdown = new HashMap<>();

for (Subscription subscription : subscriptionRepository.findAll()) {

    String plan = subscription.getPlanName();

    if (plan != null) {
        planBreakdown.put(
                plan,
                planBreakdown.getOrDefault(plan, 0) + 1
        );
    }
}
        Map<String, Object> result = new HashMap<>();

        result.put("issuedThisMonth", issuedThisMonth);
        result.put("collected", collected);
        result.put("outstandingReceivables", outstanding);
        result.put("overdueAmount", overdueAmount);
        result.put("overdueCount", overdueCount);
        result.put("statusBreakdown", statusBreakdown);
        result.put("planBreakdown", planBreakdown);

        return result;
    }
}