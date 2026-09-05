package com.busy.subscription_billing;

import com.busy.subscription_billing.model.User;
import com.busy.subscription_billing.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository repository) {
        return args -> {

            if (repository.findByEmail("admin@example.com").isEmpty()) {
                User admin = new User();
                admin.setName("Billing Admin");
                admin.setEmail("admin@example.com");
                admin.setPassword("admin123");
                admin.setRole("BILLING_ADMIN");
                repository.save(admin);
            }

            if (repository.findByEmail("manager@example.com").isEmpty()) {
                User manager = new User();
                manager.setName("Account Manager");
                manager.setEmail("manager@example.com");
                manager.setPassword("manager123");
                manager.setRole("ACCOUNT_MANAGER");
                repository.save(manager);
            }
        };
    }
}