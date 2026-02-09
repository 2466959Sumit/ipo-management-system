package com.ipomanagement.ipo_management_system.controller;

import com.ipomanagement.ipo_management_system.domain.entity.Allotment;
import com.ipomanagement.ipo_management_system.domain.entity.Ipo;
import com.ipomanagement.ipo_management_system.domain.entity.Subscription;
import com.ipomanagement.ipo_management_system.domain.entity.User;
import com.ipomanagement.ipo_management_system.domain.enums.InvestorCategory;
import com.ipomanagement.ipo_management_system.repository.AllotmentRepository;
import com.ipomanagement.ipo_management_system.service.auth.UserService;
import com.ipomanagement.ipo_management_system.service.subscription.SubscriptionService;
import com.ipomanagement.ipo_management_system.web.form.SubscribeForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SubscriptionController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final AllotmentRepository allotmentRepository;

    public SubscriptionController(UserService userService,
                                  SubscriptionService subscriptionService,
                                  AllotmentRepository allotmentRepository) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.allotmentRepository = allotmentRepository;
    }

    @GetMapping("/investor/dashboard")
    public String dashboard() {
        return "investor/dashboard";
    }

    @GetMapping("/investor/subscriptions")
    public String mySubscriptions(Model model) {
        User investor = currentUser();
        List<Subscription> subs = subscriptionService.mySubscriptions(investor);

        Map<Long, Allotment> allotmentsBySubId = new HashMap<>();
        for (Subscription s : subs) {
            allotmentRepository.findBySubscription(s).ifPresent(a -> allotmentsBySubId.put(s.getSubscriptionId(), a));
        }

        model.addAttribute("subs", subs);
        model.addAttribute("allotmentsBySubId", allotmentsBySubId);
        return "investor/subscriptions";
    }

    @GetMapping("/ipos/{ipoId}/subscribe")
    public String subscribeForm(@PathVariable Long ipoId, Model model) {
        Ipo ipo = subscriptionService.getIpoOrThrow(ipoId);

        SubscribeForm form = new SubscribeForm();
        form.setInvestorCategory(InvestorCategory.RETAIL);
        form.setPaymentSuccess(true);

        model.addAttribute("ipo", ipo);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("form", form);

        return "subscription/subscribe";
    }

    @PostMapping("/ipos/{ipoId}/subscribe")
    public String subscribeSubmit(@PathVariable Long ipoId,
                                  @Valid @ModelAttribute("form") SubscribeForm form,
                                  BindingResult br,
                                  Model model) {
        Ipo ipo = subscriptionService.getIpoOrThrow(ipoId);
        model.addAttribute("ipo", ipo);
        model.addAttribute("today", LocalDate.now());

        if (br.hasErrors()) {
            return "subscription/subscribe";
        }

        User investor = currentUser();

        try {
            Subscription saved = subscriptionService.subscribe(investor, ipoId, form);
            if (saved.getStatus().name().equals("FAILED")) {
                model.addAttribute("errorMessage", "Payment failed (simulation). Subscription saved as FAILED.");
                return "subscription/subscribe";
            }
            return "redirect:/investor/subscriptions";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "subscription/subscribe";
        }
    }

    @PostMapping("/investor/subscriptions/{subscriptionId}/cancel")
    public String cancel(@PathVariable Long subscriptionId, Model model) {
        User investor = currentUser();
        try {
            subscriptionService.cancel(investor, subscriptionId);
        } catch (Exception ex) {
            model.addAttribute("cancelError", ex.getMessage());
            return mySubscriptions(model);
        }
        return "redirect:/investor/subscriptions";
    }

    private User currentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        return userService.getByUsername(username);
    }
}