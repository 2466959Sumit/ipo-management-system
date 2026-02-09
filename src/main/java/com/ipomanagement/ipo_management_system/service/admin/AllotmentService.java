package com.ipomanagement.ipo_management_system.service.admin;

import com.ipomanagement.ipo_management_system.domain.entity.*;
import com.ipomanagement.ipo_management_system.domain.enums.InvestorCategory;
import com.ipomanagement.ipo_management_system.domain.enums.IpoStatus;
import com.ipomanagement.ipo_management_system.domain.enums.SubscriptionStatus;
import com.ipomanagement.ipo_management_system.repository.*;
import com.ipomanagement.ipo_management_system.service.audit.AuditService;
import com.ipomanagement.ipo_management_system.util.RandomUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AllotmentService {

    private final IpoRepository ipoRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AllotmentRunRepository allotmentRunRepository;
    private final AllotmentRepository allotmentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public AllotmentService(IpoRepository ipoRepository,
                            SubscriptionRepository subscriptionRepository,
                            AllotmentRunRepository allotmentRunRepository,
                            AllotmentRepository allotmentRepository,
                            UserRepository userRepository,
                            AuditService auditService) {
        this.ipoRepository = ipoRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.allotmentRunRepository = allotmentRunRepository;
        this.allotmentRepository = allotmentRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void closeIpo(Long ipoId, Long adminUserId) {
        Ipo ipo = ipoRepository.findByIdWithCompany(ipoId).orElseThrow();

        if (ipo.getStatus() == IpoStatus.CLOSED || ipo.getStatus() == IpoStatus.ALLOTTED) {
            return; // idempotent close
        }

        ipo.setStatus(IpoStatus.CLOSED);
        ipoRepository.save(ipo);

        auditService.log(adminUserId, "IPO_CLOSED", "IPO", ipoId, "IPO closed by admin");
    }

    @Transactional
    public AllotmentRun runAllotment(Long ipoId, Long adminUserId) {
        Ipo ipo = ipoRepository.findByIdWithCompany(ipoId).orElseThrow();

        if (ipo.getStatus() != IpoStatus.CLOSED) {
            throw new IllegalStateException("IPO must be CLOSED before running allotment");
        }
        if (allotmentRunRepository.existsByIpo(ipo) || ipo.getStatus() == IpoStatus.ALLOTTED) {
            throw new IllegalStateException("Allotment already run for this IPO");
        }

        User admin = userRepository.findById(adminUserId).orElseThrow();

        // Eligible subscriptions
        List<Subscription> eligible = subscriptionRepository.findByIpoAndStatusWithInvestor(ipo, SubscriptionStatus.SUCCESS);

        long seed = Instant.now().toEpochMilli();
        Random random = RandomUtil.seeded(seed);

        AllotmentRun run = new AllotmentRun();
        run.setIpo(ipo);
        run.setExecutedByAdminUser(admin);
        run.setRandomSeed(seed);
        run.setMethod("LOTTERY");
        run.setStatus("COMPLETED");

        AllotmentRun savedRun = allotmentRunRepository.save(run);

        // category-wise supply (in LOTS)
        int lotSize = ipo.getLotSize();
        int totalShares = ipo.getTotalSharesOffered();

        int retailLots = lotsAvailable(totalShares, ipo.getRetailQuotaPct(), lotSize);
        int niiLots = lotsAvailable(totalShares, ipo.getNiiQuotaPct(), lotSize);
        int qibLots = lotsAvailable(totalShares, ipo.getQibQuotaPct(), lotSize);

        Map<InvestorCategory, Integer> lotsSupply = new EnumMap<>(InvestorCategory.class);
        lotsSupply.put(InvestorCategory.RETAIL, retailLots);
        lotsSupply.put(InvestorCategory.NII, niiLots);
        lotsSupply.put(InvestorCategory.QIB, qibLots);

        // group by category
        Map<InvestorCategory, List<Subscription>> byCat = eligible.stream()
                .collect(Collectors.groupingBy(Subscription::getInvestorCategory, () -> new EnumMap<>(InvestorCategory.class), Collectors.toList()));

        // For each category: allot
        for (InvestorCategory cat : InvestorCategory.values()) {
            List<Subscription> subs = byCat.getOrDefault(cat, List.of());
            int supplyLots = lotsSupply.get(cat);

            allotCategory(cat, subs, supplyLots, lotSize, ipo.getIssuePrice(), savedRun, random);
        }

        // set IPO to ALLOTTED
        ipo.setStatus(IpoStatus.ALLOTTED);
        ipoRepository.save(ipo);

        auditService.log(adminUserId, "ALLOTMENT_RUN", "IPO", ipoId, "Allotment run completed. Seed=" + seed);
        auditService.log(adminUserId, "REFUNDS_PROCESSED", "IPO", ipoId, "Refunds computed and saved");

        return savedRun;
    }

    private int lotsAvailable(int totalShares, BigDecimal quotaPct, int lotSize) {
        BigDecimal shares = BigDecimal.valueOf(totalShares).multiply(quotaPct).divide(BigDecimal.valueOf(100));
        int categoryShares = shares.intValue(); // floor
        return categoryShares / lotSize; // floor
    }

    private void allotCategory(InvestorCategory cat,
                               List<Subscription> subs,
                               int supplyLots,
                               int lotSize,
                               BigDecimal issuePrice,
                               AllotmentRun run,
                               Random random) {

        if (subs == null || subs.isEmpty()) return;

        // compute lots requested per subscription (0 lots if quantity < lotSize)
        Map<Subscription, Integer> requestedLots = new HashMap<>();
        int totalRequestedLots = 0;
        for (Subscription s : subs) {
            int reqLots = s.getQuantity() / lotSize; // floor
            if (reqLots < 0) reqLots = 0;
            requestedLots.put(s, reqLots);
            totalRequestedLots += reqLots;
        }

        // If nothing requested in lots (all quantities < lotSize), everyone gets 0 (keep behavior stable)
        if (totalRequestedLots <= 0 || supplyLots <= 0) {
            for (Subscription s : subs) {
                saveAllotment(s, run, 0, issuePrice);
            }
            return;
        }

        // Not oversubscribed -> full allotment as requested
        if (totalRequestedLots <= supplyLots) {
            for (Subscription s : subs) {
                int sharesAllotted = requestedLots.get(s) * lotSize;
                saveAllotment(s, run, sharesAllotted, issuePrice);
            }
            return;
        }

        // Oversubscribed -> realistic two-round distribution
        // Round 1: give 1 lot to as many applicants as possible (who requested >= 1 lot)
        List<Subscription> shuffled = new ArrayList<>(subs);
        Collections.shuffle(shuffled, random);

        Map<Subscription, Integer> allottedLots = new HashMap<>();
        int lotsLeft = supplyLots;

        for (Subscription s : shuffled) {
            if (lotsLeft == 0) break;

            int req = requestedLots.getOrDefault(s, 0);
            if (req <= 0) {
                allottedLots.putIfAbsent(s, 0);
                continue;
            }

            allottedLots.put(s, 1);
            lotsLeft--;
        }

        if (lotsLeft > 0) {
            // Round 2: distribute remaining lots to applicants who requested more than currently allotted
            // Keep allocating 1 lot at a time randomly until lots exhaust or no one needs more.
            List<Subscription> eligible = shuffled.stream()
                    .filter(s -> requestedLots.getOrDefault(s, 0) > allottedLots.getOrDefault(s, 0))
                    .collect(Collectors.toCollection(ArrayList::new));

            while (lotsLeft > 0 && !eligible.isEmpty()) {
                int idx = random.nextInt(eligible.size());
                Subscription picked = eligible.get(idx);

                int req = requestedLots.getOrDefault(picked, 0);
                int already = allottedLots.getOrDefault(picked, 0);

                if (already < req) {
                    allottedLots.put(picked, already + 1);
                    lotsLeft--;
                }

                // remove if fully satisfied now
                if (allottedLots.getOrDefault(picked, 0) >= req) {
                    eligible.remove(idx);
                }
            }
        }

        // Save allotments for everyone (including those who received 0 lots)
        for (Subscription s : subs) {
            int lotsAllotted = allottedLots.getOrDefault(s, 0);
            int sharesAllotted = lotsAllotted * lotSize;
            saveAllotment(s, run, sharesAllotted, issuePrice);
        }
    }

    private void saveAllotment(Subscription s, AllotmentRun run, int sharesAllotted, BigDecimal issuePrice) {
        BigDecimal allottedValue = issuePrice.multiply(BigDecimal.valueOf(sharesAllotted));
        BigDecimal refund = s.getAmountPaid().subtract(allottedValue);
        if (refund.compareTo(BigDecimal.ZERO) < 0) refund = BigDecimal.ZERO;

        Allotment a = new Allotment();
        a.setSubscription(s);
        a.setAllotmentRun(run);
        a.setSharesAllotted(sharesAllotted);
        a.setRefundAmount(refund);

        allotmentRepository.save(a);
    }
}