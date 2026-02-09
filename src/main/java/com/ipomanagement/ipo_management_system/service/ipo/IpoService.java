package com.ipomanagement.ipo_management_system.service.ipo;

import com.ipomanagement.ipo_management_system.domain.entity.Company;
import com.ipomanagement.ipo_management_system.domain.entity.Ipo;
import com.ipomanagement.ipo_management_system.domain.entity.User;
import com.ipomanagement.ipo_management_system.domain.enums.IpoStatus;
import com.ipomanagement.ipo_management_system.repository.CompanyRepository;
import com.ipomanagement.ipo_management_system.repository.IpoRepository;
import com.ipomanagement.ipo_management_system.service.audit.AuditService;
import com.ipomanagement.ipo_management_system.web.form.IpoForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class IpoService {

    private final IpoRepository ipoRepository;
    private final CompanyRepository companyRepository;
    private final AuditService auditService;

    public IpoService(IpoRepository ipoRepository, CompanyRepository companyRepository, AuditService auditService) {
        this.ipoRepository = ipoRepository;
        this.companyRepository = companyRepository;
        this.auditService = auditService;
    }

    public Company requireIssuerCompany(User issuer) {
        return companyRepository.findByIssuerUser(issuer)
                .orElseThrow(() -> new IllegalStateException("Company profile not found. Please create company first."));
    }

    public List<Ipo> listForIssuerCompany(Company company) {
        return ipoRepository.findByCompany(company);
    }

    public Ipo getOwnedIpoOrThrow(Long ipoId, Company company) {
        Ipo ipo = ipoRepository.findById(ipoId).orElseThrow();
        if (!ipo.getCompany().getCompanyId().equals(company.getCompanyId())) {
            throw new IllegalArgumentException("Not allowed (IPO not owned by issuer)");
        }
        return ipo;
    }

    @Transactional
    public Ipo createIpo(User issuer, Company company, IpoForm form) {
        validateForm(form);

        Ipo ipo = new Ipo();
        ipo.setCompany(company);
        validateLotSizeVsTotalShares(form);
        applyForm(ipo, form);

        // initial status minimal: OPEN
        ipo.setStatus(IpoStatus.OPEN);

        Ipo saved = ipoRepository.save(ipo);


        auditService.log(issuer.getUserId(), "IPO_CREATED", "IPO", saved.getIpoId(),
                "IPO created by issuer");

        return saved;
    }

    @Transactional
    public Ipo updateIpo(User issuer, Ipo existing, IpoForm form) {
        // rule-based: allow update only before openDate
        LocalDate today = LocalDate.now();
        if (!today.isBefore(existing.getOpenDate())) {
            throw new IllegalStateException("Not allowed: IPO cannot be edited on/after open date");
        }
        if (existing.getStatus() == IpoStatus.CLOSED || existing.getStatus() == IpoStatus.ALLOTTED) {
            throw new IllegalStateException("Not allowed: IPO already closed/allotted");
        }

        validateForm(form);
        validateLotSizeVsTotalShares(form);
        applyForm(existing, form);

        Ipo saved = ipoRepository.save(existing);

        auditService.log(issuer.getUserId(), "IPO_UPDATED", "IPO", saved.getIpoId(),
                "IPO updated by issuer");

        return saved;
    }

    private void validateForm(IpoForm form) {
        if (form.getOpenDate() != null && form.getCloseDate() != null) {
            if (!form.getOpenDate().isBefore(form.getCloseDate())) {
                throw new IllegalArgumentException("openDate must be before closeDate");
            }
        }

        BigDecimal sum = form.getRetailQuotaPct()
                .add(form.getNiiQuotaPct())
                .add(form.getQibQuotaPct());

        // simple exact check (you can relax if needed)
        if (sum.compareTo(new BigDecimal("100.00")) != 0 && sum.compareTo(new BigDecimal("100")) != 0) {
            throw new IllegalArgumentException("Quota % must total 100");
        }
    }

    private void applyForm(Ipo ipo, IpoForm form) {
        ipo.setIssuePrice(form.getIssuePrice());
        ipo.setLotSize(form.getLotSize());
        ipo.setTotalSharesOffered(form.getTotalSharesOffered());
        ipo.setOpenDate(form.getOpenDate());
        ipo.setCloseDate(form.getCloseDate());

        ipo.setRetailQuotaPct(form.getRetailQuotaPct());
        ipo.setNiiQuotaPct(form.getNiiQuotaPct());
        ipo.setQibQuotaPct(form.getQibQuotaPct());
        ipo.setRetailMaxApplicationAmount(form.getRetailMaxApplicationAmount());
    }

    private void validateLotSizeVsTotalShares(IpoForm form) {
        Integer lotSize = form.getLotSize();
        Integer totalShares = form.getTotalSharesOffered();

        if (lotSize == null || totalShares == null) return; // bean validation will catch nulls if required

        if (lotSize <= 0) {
            throw new IllegalArgumentException("Lot size must be greater than 0.");
        }
        if (totalShares <= 0) {
            throw new IllegalArgumentException("Total shares offered must be greater than 0.");
        }
        if (totalShares < lotSize) {
            throw new IllegalArgumentException("Total shares offered must be at least the lot size (minimum 1 full lot).");
        }
        if (totalShares % lotSize != 0) {
            throw new IllegalArgumentException("Total shares offered must be a multiple of the lot size.");
        }
    }
}