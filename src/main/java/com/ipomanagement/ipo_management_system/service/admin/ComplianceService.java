package com.ipomanagement.ipo_management_system.service.admin;

import com.ipomanagement.ipo_management_system.domain.entity.Allotment;
import com.ipomanagement.ipo_management_system.domain.entity.ComplianceReport;
import com.ipomanagement.ipo_management_system.domain.entity.Ipo;
import com.ipomanagement.ipo_management_system.domain.enums.InvestorCategory;
import com.ipomanagement.ipo_management_system.domain.enums.SubscriptionStatus;
import com.ipomanagement.ipo_management_system.repository.*;
import com.ipomanagement.ipo_management_system.service.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ComplianceService {

    private final IpoRepository ipoRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AllotmentRepository allotmentRepository;
    private final ComplianceReportRepository complianceReportRepository;
    private final AllotmentRunRepository allotmentRunRepository;
    private final AuditService auditService;

    public ComplianceService(IpoRepository ipoRepository,
                             SubscriptionRepository subscriptionRepository,
                             AllotmentRepository allotmentRepository,
                             ComplianceReportRepository complianceReportRepository,
                             AllotmentRunRepository allotmentRunRepository,
                             AuditService auditService) {
        this.ipoRepository = ipoRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.allotmentRepository = allotmentRepository;
        this.complianceReportRepository = complianceReportRepository;
        this.allotmentRunRepository = allotmentRunRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ComplianceReport generateReport(Long ipoId, Long adminUserId) {
        Ipo ipo = ipoRepository.findByIdWithCompany(ipoId).orElseThrow();

        ComplianceReport r = new ComplianceReport();
        r.setIpo(ipo);
        r.setGeneratedDate(LocalDate.now());
        r.setStatus("GENERATED");

        ComplianceReport saved = complianceReportRepository.save(r);

        auditService.log(adminUserId, "COMPLIANCE_REPORT_GENERATED", "COMPLIANCE_REPORT", saved.getReportId(),
                "Compliance report generated for IPO " + ipoId);

        return saved;
    }

    /**
     * Simple computed numbers for display.
     * We compute from subscriptions + allotments stored.
     */
    public Map<String, Object> computeSummary(Long ipoId) {
        Ipo ipo = ipoRepository.findByIdWithCompany(ipoId).orElseThrow();

        // Eligible demand = SUCCESS subscriptions
        List<com.ipomanagement.ipo_management_system.domain.entity.Subscription> subs =
                subscriptionRepository.findByIpoAndStatusWithInvestor(ipo, SubscriptionStatus.SUCCESS);

        Map<InvestorCategory, Integer> demandShares = new EnumMap<>(InvestorCategory.class);
        Map<InvestorCategory, Integer> demandLots = new EnumMap<>(InvestorCategory.class);

        for (InvestorCategory c : InvestorCategory.values()) {
            demandShares.put(c, 0);
            demandLots.put(c, 0);
        }

        for (var s : subs) {
            demandShares.put(s.getInvestorCategory(), demandShares.get(s.getInvestorCategory()) + s.getQuantity());
            demandLots.put(s.getInvestorCategory(), demandLots.get(s.getInvestorCategory()) + (s.getQuantity() / ipo.getLotSize()));
        }

        // Allotted + refunds (from allotment table)
        List<Allotment> allotments = allotmentRepository.findAll(); // simple; ok for training size
        int totalAllottedShares = 0;
        BigDecimal totalRefund = BigDecimal.ZERO;

        for (Allotment a : allotments) {
            if (a.getSubscription().getIpo().getIpoId().equals(ipoId)) {
                totalAllottedShares += a.getSharesAllotted();
                totalRefund = totalRefund.add(a.getRefundAmount());
            }
        }

        Map<String, Object> m = new java.util.HashMap<>();
        m.put("ipo", ipo);
        m.put("demandShares", demandShares);
        m.put("demandLots", demandLots);
        m.put("totalAllottedShares", totalAllottedShares);
        m.put("totalRefund", totalRefund);
        m.put("allotmentRun", allotmentRunRepository.findByIpo(ipo).orElse(null));
        return m;
    }

    public List<ComplianceReport> reportsForIpo(Long ipoId) {
        Ipo ipo = ipoRepository.findByIdWithCompany(ipoId).orElseThrow();
        return complianceReportRepository.findByIpoOrderByCreatedAtDesc(ipo);
    }

    public List<Ipo> listAllIposForCompliance() {
        return ipoRepository.findAllWithCompany();
    }
}