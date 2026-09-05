package com.busy.subscription_billing;

import com.busy.subscription_billing.model.Subscription;
import com.busy.subscription_billing.repository.SubscriptionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionRepository repository;

    public SubscriptionController(SubscriptionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Subscription> getAll(HttpSession session) {
        checkLoggedIn(session);

        String role = (String) session.getAttribute("role");

        if ("BILLING_ADMIN".equals(role)) {
            return repository.findAll();
        }

        Long userId = (Long) session.getAttribute("userId");

        return repository.findAll()
                .stream()
                .filter(s -> userId.equals(s.getOwnerId()))
                .collect(Collectors.toList());
    }

    @PostMapping
    public Subscription create(
            @RequestBody Subscription subscription,
            HttpSession session) {

        checkLoggedIn(session);

        String role = (String) session.getAttribute("role");
        Long userId = (Long) session.getAttribute("userId");

        if ("ACCOUNT_MANAGER".equals(role)) {
            subscription.setOwnerId(userId);
        }

        return repository.save(subscription);
    }

    @PutMapping("/{id}")
    public Subscription update(
            @PathVariable Long id,
            @RequestBody Subscription subscription,
            HttpSession session) {

        checkLoggedIn(session);

        Subscription existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        String role = (String) session.getAttribute("role");
        Long userId = (Long) session.getAttribute("userId");

        if ("ACCOUNT_MANAGER".equals(role)
        && !userId.equals(existing.getOwnerId())
        && !existing.getCollaboratorIds().contains(userId)) {
    throw new RuntimeException(
            "You do not have access to this subscription");
}

        subscription.setId(id);
        subscription.setOwnerId(existing.getOwnerId());

        return repository.save(subscription);
    }

    @PutMapping("/{id}/archive")
    public Subscription archive(
            @PathVariable Long id,
            HttpSession session) {

        checkAdmin(session);

        Subscription s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        s.setArchived(true);

        return repository.save(s);
    }

    @PutMapping("/{id}/restore")
    public Subscription restore(
            @PathVariable Long id,
            HttpSession session) {

        checkAdmin(session);

        Subscription s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        s.setArchived(false);

        return repository.save(s);
    }
      
    @PostMapping("/{id}/collaborators/{userId}")
public Subscription addCollaborator(
        @PathVariable Long id,
        @PathVariable Long userId,
        HttpSession session) {

    checkAdmin(session);

    Subscription subscription = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Subscription not found"));

    if (!subscription.getCollaboratorIds().contains(userId)
            && !userId.equals(subscription.getOwnerId())) {
        subscription.getCollaboratorIds().add(userId);
    }

    return repository.save(subscription);
}

@DeleteMapping("/{id}/collaborators/{userId}")
public Subscription removeCollaborator(
        @PathVariable Long id,
        @PathVariable Long userId,
        HttpSession session) {

    checkAdmin(session);

    Subscription subscription = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Subscription not found"));

    subscription.getCollaboratorIds().remove(userId);

    return repository.save(subscription);
}  


    private void checkLoggedIn(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            throw new RuntimeException("Please login first");
        }
    }

    private void checkAdmin(HttpSession session) {
        checkLoggedIn(session);

        if (!"BILLING_ADMIN".equals(session.getAttribute("role"))) {
            throw new RuntimeException(
                    "Only Billing Admin can perform this action");
        }
    }
}