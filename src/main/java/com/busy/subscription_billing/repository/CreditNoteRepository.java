package com.busy.subscription_billing.repository;

import com.busy.subscription_billing.model.CreditNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditNoteRepository extends JpaRepository<CreditNote, Long> {
}