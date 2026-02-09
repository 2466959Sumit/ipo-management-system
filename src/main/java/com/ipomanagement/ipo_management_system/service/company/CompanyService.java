package com.ipomanagement.ipo_management_system.service.company;

import com.ipomanagement.ipo_management_system.domain.entity.Company;
import com.ipomanagement.ipo_management_system.domain.entity.User;
import com.ipomanagement.ipo_management_system.repository.CompanyRepository;
import com.ipomanagement.ipo_management_system.service.audit.AuditService;
import com.ipomanagement.ipo_management_system.web.form.CompanyForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final AuditService auditService;

    public CompanyService(CompanyRepository companyRepository, AuditService auditService) {
        this.companyRepository = companyRepository;
        this.auditService = auditService;
    }

    public Company getCompanyForIssuer(User issuer) {
        return companyRepository.findByIssuerUser(issuer).orElse(null);
    }

    @Transactional
    public Company createOrUpdateCompany(User issuer, CompanyForm form) {
        Company existing = companyRepository.findByIssuerUser(issuer).orElse(null);

        if (existing == null) {
            // create
            if (companyRepository.existsByRegistrationNumber(form.getRegistrationNumber())) {
                throw new IllegalArgumentException("Registration number already exists");
            }
            Company c = new Company();
            c.setIssuerUser(issuer);
            applyForm(c, form);
            Company saved = companyRepository.save(c);

            auditService.log(issuer.getUserId(), "COMPANY_CREATED", "COMPANY", saved.getCompanyId(),
                    "Company profile created");
            return saved;
        } else {
            // update (registration number uniqueness check only if changed)
            if (!existing.getRegistrationNumber().equals(form.getRegistrationNumber())
                    && companyRepository.existsByRegistrationNumber(form.getRegistrationNumber())) {
                throw new IllegalArgumentException("Registration number already exists");
            }

            applyForm(existing, form);
            Company saved = companyRepository.save(existing);

            auditService.log(issuer.getUserId(), "COMPANY_UPDATED", "COMPANY", saved.getCompanyId(),
                    "Company profile updated");
            return saved;
        }
    }

    private void applyForm(Company c, CompanyForm form) {
        c.setCompanyName(form.getCompanyName());
        c.setRegistrationNumber(form.getRegistrationNumber());
        c.setAddress(form.getAddress());
        c.setContactEmail(form.getContactEmail());
        c.setContactPhone(form.getContactPhone());
    }
}