package com.ipomanagement.ipo_management_system.web.form;

import com.ipomanagement.ipo_management_system.domain.enums.InvestorCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SubscribeForm {

    @NotNull
    private InvestorCategory investorCategory;

    @NotNull @Min(1)
    private Integer quantity;

    /**
     * Training-mode payment simulation:
     * true = payment success, false = fail
     */
    private boolean paymentSuccess = true;

    // getters/setters
    public InvestorCategory getInvestorCategory() { return investorCategory; }
    public void setInvestorCategory(InvestorCategory investorCategory) { this.investorCategory = investorCategory; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public boolean isPaymentSuccess() { return paymentSuccess; }
    public void setPaymentSuccess(boolean paymentSuccess) { this.paymentSuccess = paymentSuccess; }
}