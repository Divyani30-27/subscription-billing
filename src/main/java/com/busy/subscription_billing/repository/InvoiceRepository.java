package com.busy.subscription_billing.repository;

import com.busy.subscription_billing.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findBySubscriptionIdAndBillingPeriodStartAndBillingPeriodEnd(
            Long subscriptionId,
            LocalDate billingPeriodStart,
            LocalDate billingPeriodEnd);
}