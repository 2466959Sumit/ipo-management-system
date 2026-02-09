package com.ipomanagement.ipo_management_system.controller;

import com.ipomanagement.ipo_management_system.domain.entity.AllotmentRun;
import com.ipomanagement.ipo_management_system.domain.entity.Ipo;
import com.ipomanagement.ipo_management_system.domain.entity.User;
import com.ipomanagement.ipo_management_system.domain.enums.IpoStatus;
import com.ipomanagement.ipo_management_system.repository.AllotmentRunRepository;
import com.ipomanagement.ipo_management_system.repository.IpoRepository;
import com.ipomanagement.ipo_management_system.service.admin.AllotmentService;
import com.ipomanagement.ipo_management_system.service.admin.ComplianceService;
import com.ipomanagement.ipo_management_system.service.audit.AuditService;
import com.ipomanagement.ipo_management_system.service.auth.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.ipomanagement.ipo_management_system.web.form.AdminCreateUserForm;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final IpoRepository ipoRepository;
    private final AllotmentService allotmentService;
    private final AllotmentRunRepository allotmentRunRepository;

    private final ComplianceService complianceService;
    private final AuditService auditService;

    public AdminController(UserService userService,
                           IpoRepository ipoRepository,
                           AllotmentService allotmentService,
                           AllotmentRunRepository allotmentRunRepository,
                           ComplianceService complianceService,
                           AuditService auditService) {
        this.userService = userService;
        this.ipoRepository = ipoRepository;
        this.allotmentService = allotmentService;
        this.allotmentRunRepository = allotmentRunRepository;
        this.complianceService = complianceService;
        this.auditService = auditService;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    // ===== Allotment screens =====

    @GetMapping("/allotment")
    public String allotmentHome(Model model) {
        List<Ipo> open = ipoRepository.findByStatusWithCompany(IpoStatus.OPEN);
        List<Ipo> closed = ipoRepository.findByStatusWithCompany(IpoStatus.CLOSED);
        List<Ipo> allotted = ipoRepository.findByStatusWithCompany(IpoStatus.ALLOTTED);

        model.addAttribute("openIpos", open);
        model.addAttribute("closedIpos", closed);
        model.addAttribute("allottedIpos", allotted);

        return "admin/allotment";
    }

    @PostMapping("/ipos/{ipoId}/close")
    public String closeIpo(@PathVariable Long ipoId) {
        User admin = currentUser();
        allotmentService.closeIpo(ipoId, admin.getUserId());
        return "redirect:/admin/allotment";
    }

    @PostMapping("/ipos/{ipoId}/allot")
    public String runAllotment(@PathVariable Long ipoId, Model model) {
        User admin = currentUser();
        try {
            allotmentService.runAllotment(ipoId, admin.getUserId());
            return "redirect:/admin/ipos/" + ipoId + "/allotment-result";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return allotmentHome(model);
        }
    }

    @GetMapping("/ipos/{ipoId}/allotment-result")
    public String allotmentResult(@PathVariable Long ipoId, Model model) {
        Ipo ipo = ipoRepository.findByIdWithCompany(ipoId).orElseThrow();
        AllotmentRun run = allotmentRunRepository.findByIpo(ipo).orElse(null);

        model.addAttribute("ipo", ipo);
        model.addAttribute("run", run);

        return "admin/allotment-result";
    }

    // ===== Compliance screens =====

    @GetMapping("/compliance")
    public String complianceHome(@RequestParam(required = false) Long ipoId, Model model) {
        model.addAttribute("ipos", complianceService.listAllIposForCompliance());

        if (ipoId != null) {
            Map<String, Object> summary = complianceService.computeSummary(ipoId);
            model.addAttribute("summary", summary);
            model.addAttribute("reports", complianceService.reportsForIpo(ipoId));
            model.addAttribute("selectedIpoId", ipoId);
        }

        return "admin/compliance";
    }

    @PostMapping("/ipos/{ipoId}/compliance-report")
    public String generateCompliance(@PathVariable Long ipoId) {
        User admin = currentUser();
        complianceService.generateReport(ipoId, admin.getUserId());
        return "redirect:/admin/compliance?ipoId=" + ipoId;
    }

    // ===== Audit trail =====

    @GetMapping("/audit")
    public String audit(Model model) {
        model.addAttribute("audits", auditService.latest200());
        return "admin/audit";
    }

    private User currentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        return userService.getByUsername(username);
    }

    // Add these methods inside the existing AdminController class

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        AdminCreateUserForm form = new AdminCreateUserForm();
        form.setRole(com.ipomanagement.ipo_management_system.domain.enums.Role.ISSUER);
        model.addAttribute("form", form);
        return "admin/user-form";
    }

    @PostMapping("/users")
    public String createUser(@Valid @ModelAttribute("form") AdminCreateUserForm form,
                             BindingResult br,
                             Model model) {
        if (br.hasErrors()) {
            return "admin/user-form";
        }

        User admin = currentUser();

        try {
            userService.createUserByAdmin(form.getUsername(), form.getEmail(), form.getPassword(), form.getRole(), admin.getUserId());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/user-form";
        }

        return "redirect:/admin/dashboard";
    }

}