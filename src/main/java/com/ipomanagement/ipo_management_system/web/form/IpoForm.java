package com.ipomanagement.ipo_management_system.web.form;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class IpoForm {

    @NotNull @DecimalMin("0.01")
    private BigDecimal issuePrice;

    @NotNull @Min(1)
    private Integer lotSize;

    @NotNull @Min(1)
    private Integer totalSharesOffered;

    @NotNull
    private LocalDate openDate;

    @NotNull
    private LocalDate closeDate;

    @NotNull @DecimalMin("0.00")
    private BigDecimal retailQuotaPct;

    @NotNull @DecimalMin("0.00")
    private BigDecimal niiQuotaPct;

    @NotNull @DecimalMin("0.00")
    private BigDecimal qibQuotaPct;

    @NotNull @DecimalMin("0.01")
    private BigDecimal retailMaxApplicationAmount;

    // getters/setters
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

    public BigDecimal getRetailQuotaPct() { return retailQuotaPct; }
    public void setRetailQuotaPct(BigDecimal retailQuotaPct) { this.retailQuotaPct = retailQuotaPct; }

    public BigDecimal getNiiQuotaPct() { return niiQuotaPct; }
    public void setNiiQuotaPct(BigDecimal niiQuotaPct) { this.niiQuotaPct = niiQuotaPct; }

    public BigDecimal getQibQuotaPct() { return qibQuotaPct; }
    public void setQibQuotaPct(BigDecimal qibQuotaPct) { this.qibQuotaPct = qibQuotaPct; }

    public BigDecimal getRetailMaxApplicationAmount() { return retailMaxApplicationAmount; }
    public void setRetailMaxApplicationAmount(BigDecimal retailMaxApplicationAmount) { this.retailMaxApplicationAmount = retailMaxApplicationAmount; }
}