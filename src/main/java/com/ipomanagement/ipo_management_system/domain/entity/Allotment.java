package com.ipomanagement.ipo_management_system.domain.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "allotment")
public class Allotment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long allotmentId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", unique = true)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allotment_run_id")
    private AllotmentRun allotmentRun;

    @Column(nullable = false)
    private Integer sharesAllotted = 0;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // getters/setters
    public Long getAllotmentId() { return allotmentId; }
    public void setAllotmentId(Long allotmentId) { this.allotmentId = allotmentId; }

    public Subscription getSubscription() { return subscription; }
    public void setSubscription(Subscription subscription) { this.subscription = subscription; }

    public AllotmentRun getAllotmentRun() { return allotmentRun; }
    public void setAllotmentRun(AllotmentRun allotmentRun) { this.allotmentRun = allotmentRun; }

    public Integer getSharesAllotted() { return sharesAllotted; }
    public void setSharesAllotted(Integer sharesAllotted) { this.sharesAllotted = sharesAllotted; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}