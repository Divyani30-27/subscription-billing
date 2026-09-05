package com.busy.subscription_billing.repository;

import com.busy.subscription_billing.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByOwnerId(Long ownerId);
}