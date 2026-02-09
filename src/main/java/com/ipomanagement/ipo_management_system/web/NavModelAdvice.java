package com.ipomanagement.ipo_management_system.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavModelAdvice {

    @ModelAttribute("nav")
    public NavLinks nav(Authentication auth) {
        NavLinks n = new NavLinks();

        if (auth == null || !auth.isAuthenticated()) {
            n.setAuthenticated(false);
            return n;
        }

        n.setAuthenticated(true);
        n.setUsername(auth.getName());

        n.setAdmin(hasRole(auth, "ROLE_ADMIN"));
        n.setIssuer(hasRole(auth, "ROLE_ISSUER"));
        n.setInvestor(hasRole(auth, "ROLE_INVESTOR"));

        return n;
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }

    public static class NavLinks {
        private boolean authenticated;
        private String username;
        private boolean admin;
        private boolean issuer;
        private boolean investor;

        public boolean isAuthenticated() { return authenticated; }
        public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public boolean isAdmin() { return admin; }
        public void setAdmin(boolean admin) { this.admin = admin; }

        public boolean isIssuer() { return issuer; }
        public void setIssuer(boolean issuer) { this.issuer = issuer; }

        public boolean isInvestor() { return investor; }
        public void setInvestor(boolean investor) { this.investor = investor; }
    }
}