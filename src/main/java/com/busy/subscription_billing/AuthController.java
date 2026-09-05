package com.busy.subscription_billing;

import com.busy.subscription_billing.model.User;
import com.busy.subscription_billing.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository repository;

    public AuthController(UserRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestBody Map<String, String> request,
            HttpSession session) {

        String email = request.get("email");
        String password = request.get("password");

        User user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid email or password");
        }

        session.setAttribute("userId", user.getId());
        session.setAttribute("role", user.getRole());

        return Map.of(
                "message", "Login successful",
                "userId", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole()
        );
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        session.invalidate();
        return Map.of("message", "Logout successful");
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {

        if (session.getAttribute("userId") == null) {
            return Map.of("loggedIn", false);
        }

        return Map.of(
                "loggedIn", true,
                "userId", session.getAttribute("userId"),
                "role", session.getAttribute("role")
        );
    }
}