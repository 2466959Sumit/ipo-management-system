package com.ipomanagement.ipo_management_system.controller;

import com.ipomanagement.ipo_management_system.domain.entity.Company;
import com.ipomanagement.ipo_management_system.domain.entity.Ipo;
import com.ipomanagement.ipo_management_system.domain.entity.User;
import com.ipomanagement.ipo_management_system.service.auth.UserService;
import com.ipomanagement.ipo_management_system.service.company.CompanyService;
import com.ipomanagement.ipo_management_system.service.ipo.IpoService;
import com.ipomanagement.ipo_management_system.web.form.CompanyForm;
import com.ipomanagement.ipo_management_system.web.form.IpoForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/issuer")
public class IssuerController {

    private final UserService userService;
    private final CompanyService companyService;
    private final IpoService ipoService;

    public IssuerController(UserService userService, CompanyService companyService, IpoService ipoService) {
        this.userService = userService;
        this.companyService = companyService;
        this.ipoService = ipoService;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "issuer/dashboard";
    }

    // ===== Company Profile =====

    @GetMapping("/company")
    public String companyForm(@RequestParam(required = false) String saved, Model model) {
        User issuer = userService.getByUsername(org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName());

        Company c = companyService.getCompanyForIssuer(issuer);

        CompanyForm form = new CompanyForm();
        if (c != null) {
            form.setCompanyName(c.getCompanyName());
            form.setRegistrationNumber(c.getRegistrationNumber());
            form.setAddress(c.getAddress());
            form.setContactEmail(c.getContactEmail());
            form.setContactPhone(c.getContactPhone());
        }

        model.addAttribute("form", form);
        model.addAttribute("saved", saved != null);
        return "issuer/company-form";
    }

    @PostMapping("/company")
    public String companySubmit(@Valid @ModelAttribute("form") CompanyForm form,
                                BindingResult br,
                                Model model) {
        if (br.hasErrors()) {
            return "issuer/company-form";
        }

        User issuer = userService.getByUsername(org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName());

        try {
            companyService.createOrUpdateCompany(issuer, form);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "issuer/company-form";
        }

        return "redirect:/issuer/company?saved=1";
    }

    // ===== Issuer IPO list =====

    @GetMapping("/ipos")
    public String issuerIpoList(Model model) {
        User issuer = userService.getByUsername(org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName());

        Company company = companyService.getCompanyForIssuer(issuer);
        if (company == null) {
            model.addAttribute("noCompany", true);
            return "issuer/ipo-list";
        }

        model.addAttribute("noCompany", false);
        model.addAttribute("ipos", ipoService.listForIssuerCompany(company));
        return "issuer/ipo-list";
    }

    // ===== Create IPO =====

    @GetMapping("/ipos/new")
    public String newIpoForm(Model model) {
        model.addAttribute("form", new IpoForm());
        model.addAttribute("mode", "create");
        return "ipo/form";
    }

    @PostMapping("/ipos")
    public String createIpo(@Valid @ModelAttribute("form") IpoForm form,
                            BindingResult br,
                            Model model) {
        if (br.hasErrors()) {
            model.addAttribute("mode", "create");
            return "ipo/form";
        }

        User issuer = userService.getByUsername(org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName());

        try {
            Company company = ipoService.requireIssuerCompany(issuer);
            Ipo saved = ipoService.createIpo(issuer, company, form);
            return "redirect:/ipos/" + saved.getIpoId();
        } catch (IllegalArgumentException ex) {
            // ✅ validation/business rule error (e.g., totalShares < lotSize)
            model.addAttribute("mode", "create");
            model.addAttribute("errorMessage", ex.getMessage());
            return "ipo/form";
        } catch (Exception ex) {
            // ✅ unexpected error (keep message generic so UX is clean)
            model.addAttribute("mode", "create");
            model.addAttribute("errorMessage", "Unable to create IPO. Please try again.");
            return "ipo/form";
        }
    }

    // ===== Edit IPO =====

    @GetMapping("/ipos/{ipoId}/edit")
    public String editIpoForm(@PathVariable Long ipoId, Model model) {
        User issuer = userService.getByUsername(org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName());

        Company company = ipoService.requireIssuerCompany(issuer);
        Ipo ipo = ipoService.getOwnedIpoOrThrow(ipoId, company);

        IpoForm form = new IpoForm();
        form.setIssuePrice(ipo.getIssuePrice());
        form.setLotSize(ipo.getLotSize());
        form.setTotalSharesOffered(ipo.getTotalSharesOffered());
        form.setOpenDate(ipo.getOpenDate());
        form.setCloseDate(ipo.getCloseDate());
        form.setRetailQuotaPct(ipo.getRetailQuotaPct());
        form.setNiiQuotaPct(ipo.getNiiQuotaPct());
        form.setQibQuotaPct(ipo.getQibQuotaPct());
        form.setRetailMaxApplicationAmount(ipo.getRetailMaxApplicationAmount());

        model.addAttribute("form", form);
        model.addAttribute("mode", "edit");
        model.addAttribute("ipoId", ipoId);
        return "ipo/form";
    }

    @PostMapping("/ipos/{ipoId}")
    public String updateIpo(@PathVariable Long ipoId,
                            @Valid @ModelAttribute("form") IpoForm form,
                            BindingResult br,
                            Model model) {
        if (br.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("ipoId", ipoId);
            return "ipo/form";
        }

        User issuer = userService.getByUsername(org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName());

        try {
            Company company = ipoService.requireIssuerCompany(issuer);
            Ipo existing = ipoService.getOwnedIpoOrThrow(ipoId, company);
            Ipo saved = ipoService.updateIpo(issuer, existing, form);
            return "redirect:/ipos/" + saved.getIpoId();
        } catch (IllegalArgumentException ex) {
            // validation/business rule error (e.g., totalShares < lotSize)
            model.addAttribute("mode", "edit");
            model.addAttribute("ipoId", ipoId);
            model.addAttribute("errorMessage", ex.getMessage());
            return "ipo/form";
        } catch (Exception ex) {
            model.addAttribute("mode", "edit");
            model.addAttribute("ipoId", ipoId);
            model.addAttribute("errorMessage", "Unable to update IPO. Please try again.");
            return "ipo/form";
        }
    }
}