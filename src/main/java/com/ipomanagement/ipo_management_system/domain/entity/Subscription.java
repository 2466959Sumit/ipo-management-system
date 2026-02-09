package com.ipomanagement.ipo_management_system.domain.entity;

import com.ipomanagement.ipo_management_system.domain.enums.InvestorCategory;
import com.ipomanagement.ipo_management_system.domain.enums.SubscriptionStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "subscription",
        uniqueConstraints = @UniqueConstraint(name = "uq_subscription_investor_ipo", columnNames = {"ipo_id", "investor_user_id"}))
public class Subscription {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ipo_id")
    private Ipo ipo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_user_id")
    private User investorUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvestorCategory investorCategory = InvestorCategory.RETAIL;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amountPaid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status = SubscriptionStatus.SUCCESS;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // getters/setters
    public Long getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }

    public Ipo getIpo() { return ipo; }
    public void setIpo(Ipo ipo) { this.ipo = ipo; }

    public User getInvestorUser() { return investorUser; }
    public void setInvestorUser(User investorUser) { this.investorUser = investorUser; }

    public InvestorCategory getInvestorCategory() { return investorCategory; }
    public void setInvestorCategory(InvestorCategory investorCategory) { this.investorCategory = investorCategory; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}