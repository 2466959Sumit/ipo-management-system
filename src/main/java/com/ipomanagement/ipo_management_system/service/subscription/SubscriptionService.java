package com.ipomanagement.ipo_management_system.service.subscription;

import com.ipomanagement.ipo_management_system.domain.entity.Ipo;
import com.ipomanagement.ipo_management_system.domain.entity.Subscription;
import com.ipomanagement.ipo_management_system.domain.entity.User;
import com.ipomanagement.ipo_management_system.domain.enums.InvestorCategory;
import com.ipomanagement.ipo_management_system.domain.enums.SubscriptionStatus;
import com.ipomanagement.ipo_management_system.repository.AllotmentRepository;
import com.ipomanagement.ipo_management_system.repository.IpoRepository;
import com.ipomanagement.ipo_management_system.repository.SubscriptionRepository;
import com.ipomanagement.ipo_management_system.service.audit.AuditService;
import com.ipomanagement.ipo_management_system.web.form.SubscribeForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionService {

    private final IpoRepository ipoRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AllotmentRepository allotmentRepository;
    private final AuditService auditService;

    public SubscriptionService(IpoRepository ipoRepository,
                               SubscriptionRepository subscriptionRepository,
                               AllotmentRepository allotmentRepository,
                               AuditService auditService) {
        this.ipoRepository = ipoRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.allotmentRepository = allotmentRepository;
        this.auditService = auditService;
    }

    public List<Subscription> mySubscriptions(User investor) {
        return subscriptionRepository.findByInvestorUserOrderByCreatedAtDesc(investor);
    }

    public Ipo getIpoOrThrow(Long ipoId) {
        // Fetch company to avoid LazyInitializationException in Thymeleaf
        return ipoRepository.findByIdWithCompany(ipoId).orElseThrow();
    }

    public Subscription getSubscriptionOrThrow(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId).orElseThrow();
    }

    public boolean hasAllotment(Subscription s) {
        return allotmentRepository.findBySubscription(s).isPresent();
    }

    @Transactional
    public Subscription subscribe(User investor, Long ipoId, SubscribeForm form) {
        Ipo ipo = getIpoOrThrow(ipoId);
        LocalDate today = LocalDate.now();

        if (!ipo.isOpenForSubscription(today)) {
            throw new IllegalStateException("IPO is not open for subscription");
        }

        if (subscriptionRepository.findByIpoAndInvestorUser(ipo, investor).isPresent()) {
            throw new IllegalStateException("You already have a subscription for this IPO");
        }

        int lotSize = ipo.getLotSize();
        int qty = form.getQuantity();

        if (qty < lotSize) {
            throw new IllegalArgumentException("Quantity must be at least lot size (" + lotSize + ")");
        }
        if (qty % lotSize != 0) {
            throw new IllegalArgumentException("Quantity must be a multiple of lot size (" + lotSize + ")");
        }

        InvestorCategory category = form.getInvestorCategory();
        BigDecimal amountPaid = ipo.getIssuePrice().multiply(BigDecimal.valueOf(qty));

        if (category == InvestorCategory.RETAIL) {
            if (amountPaid.compareTo(ipo.getRetailMaxApplicationAmount()) > 0) {
                throw new IllegalArgumentException("Retail cap exceeded. Max allowed: " + ipo.getRetailMaxApplicationAmount());
            }
        }

        if (!form.isPaymentSuccess()) {
            Subscription failed = new Subscription();
            failed.setIpo(ipo);
            failed.setInvestorUser(investor);
            failed.setInvestorCategory(category);
            failed.setQuantity(qty);
            failed.setAmountPaid(amountPaid);
            failed.setStatus(SubscriptionStatus.FAILED);

            Subscription saved = subscriptionRepository.save(failed);

            auditService.log(investor.getUserId(), "SUBSCRIPTION_FAILED", "SUBSCRIPTION", saved.getSubscriptionId(),
                    "Payment failed (simulation)");
            return saved;
        }

        Subscription s = new Subscription();
        s.setIpo(ipo);
        s.setInvestorUser(investor);
        s.setInvestorCategory(category);
        s.setQuantity(qty);
        s.setAmountPaid(amountPaid);
        s.setStatus(SubscriptionStatus.SUCCESS);

        Subscription saved = subscriptionRepository.save(s);

        auditService.log(investor.getUserId(), "SUBSCRIPTION_CREATED", "SUBSCRIPTION", saved.getSubscriptionId(),
                "Subscription created successfully");

        return saved;
    }

    // replace only the cancel() method with this clearer version

    @Transactional
    public void cancel(User investor, Long subscriptionId) {
        Subscription s = getSubscriptionOrThrow(subscriptionId);

        if (!s.getInvestorUser().getUserId().equals(investor.getUserId())) {
            throw new IllegalArgumentException("Not allowed");
        }

        if (s.getStatus() != SubscriptionStatus.SUCCESS) {
            throw new IllegalStateException("Only SUCCESS subscriptions can be cancelled");
        }

        Ipo ipo = s.getIpo();
        LocalDate today = LocalDate.now();

        // hard blocks
        if (ipo.getStatus() == com.ipomanagement.ipo_management_system.domain.enums.IpoStatus.CLOSED
                || ipo.getStatus() == com.ipomanagement.ipo_management_system.domain.enums.IpoStatus.ALLOTTED) {
            throw new IllegalStateException("Not allowed: IPO already closed/allotted");
        }

        // date window check
        if (today.isBefore(ipo.getOpenDate()) || today.isAfter(ipo.getCloseDate())) {
            throw new IllegalStateException("Not allowed: cancellation only within IPO open window");
        }

        s.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(s);

        auditService.log(investor.getUserId(), "SUBSCRIPTION_CANCELLED", "SUBSCRIPTION", s.getSubscriptionId(),
                "Subscription cancelled by investor");
    }
}