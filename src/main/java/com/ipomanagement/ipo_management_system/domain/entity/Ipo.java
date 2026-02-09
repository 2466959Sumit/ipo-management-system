package com.ipomanagement.ipo_management_system.domain.entity;

import com.ipomanagement.ipo_management_system.domain.enums.IpoStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "ipo")
public class Ipo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ipoId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal issuePrice;

    @Column(nullable = false)
    private Integer lotSize;

    @Column(nullable = false)
    private Integer totalSharesOffered;

    @Column(nullable = false)
    private LocalDate openDate;

    @Column(nullable = false)
    private LocalDate closeDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IpoStatus status = IpoStatus.OPEN;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal retailQuotaPct = new BigDecimal("35.00");

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal niiQuotaPct = new BigDecimal("15.00");

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal qibQuotaPct = new BigDecimal("50.00");

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal retailMaxApplicationAmount = new BigDecimal("200000.00");

    @Column(nullable = false)
    private String allotmentMethod = "LOTTERY";

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public boolean isOpenForSubscription(LocalDate today) {
        if (status == IpoStatus.CLOSED || status == IpoStatus.ALLOTTED) return false;
        return (today.isEqual(openDate) || today.isAfter(openDate)) &&
                (today.isEqual(closeDate) || today.isBefore(closeDate));
    }

    // getters/setters
    public Long getIpoId() { return ipoId; }
    public void setIpoId(Long ipoId) { this.ipoId = ipoId; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public BigDecimal getIssuePrice() { return issuePrice; }
    public void setIssuePrice(BigDecimal issuePrice) { this.issuePrice = issuePrice; }

    public Integer getLotSize() { return lotSize; }
    public void setLotSize(Integer lotSize) { this.lotSize = lotSize; }

    public Integer getTotalSharesOffered() { return totalSharesOffered; }
    public void setTotalSharesOffered(Integer totalSharesOffered) { this.totalSharesOffered = totalSharesOffered; }

    public LocalDate getOpenDate() { return openDate; }
    public void setOpenDate(LocalDate openDate) { this.openDate = openDate; }

    public LocalDate getCloseDate() { return closeDate; }
    public void setCloseDate(LocalDate closeDate) { this.closeDate = closeDate; }

    public IpoStatus getStatus() { return status; }
    public void setStatus(IpoStatus status) { this.status = status; }

    public BigDecimal getRetailQuotaPct() { return retailQuotaPct; }
    public void setRetailQuotaPct(BigDecimal retailQuotaPct) { this.retailQuotaPct = retailQuotaPct; }

    public BigDecimal getNiiQuotaPct() { return niiQuotaPct; }
    public void setNiiQuotaPct(BigDecimal niiQuotaPct) { this.niiQuotaPct = niiQuotaPct; }

    public BigDecimal getQibQuotaPct() { return qibQuotaPct; }
    public void setQibQuotaPct(BigDecimal qibQuotaPct) { this.qibQuotaPct = qibQuotaPct; }

    public BigDecimal getRetailMaxApplicationAmount() { return retailMaxApplicationAmount; }
    public void setRetailMaxApplicationAmount(BigDecimal retailMaxApplicationAmount) { this.retailMaxApplicationAmount = retailMaxApplicationAmount; }

    public String getAllotmentMethod() { return allotmentMethod; }
    public void setAllotmentMethod(String allotmentMethod) { this.allotmentMethod = allotmentMethod; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}