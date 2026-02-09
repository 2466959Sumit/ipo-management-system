package com.ipomanagement.ipo_management_system.controller;

import com.ipomanagement.ipo_management_system.domain.enums.Role;
import com.ipomanagement.ipo_management_system.service.auth.UserService;
import com.ipomanagement.ipo_management_system.web.form.RegisterForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("form") RegisterForm form,
                                 BindingResult br,
                                 Model model) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            br.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }

        if (br.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerInvestor(form.getUsername(), form.getEmail(), form.getPassword());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/register";
        }

        return "redirect:/login?registered";
    }

    @GetMapping("/post-login")
    public String postLogin(Authentication authentication) {
        // Decide landing page based on role
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.ADMIN.name()));
        boolean isIssuer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.ISSUER.name()));
        boolean isInvestor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.INVESTOR.name()));

        if (isAdmin) return "redirect:/admin/dashboard";
        if (isIssuer) return "redirect:/issuer/dashboard";
        if (isInvestor) return "redirect:/investor/dashboard";

        return "redirect:/ipos";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}