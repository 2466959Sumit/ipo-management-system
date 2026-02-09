package com.ipomanagement.ipo_management_system.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CompanyForm {

    @NotBlank @Size(max = 150)
    private String companyName;

    @NotBlank @Size(max = 50)
    private String registrationNumber;

    @Size(max = 255)
    private String address;

    @Size(max = 100)
    private String contactEmail;

    @Size(max = 30)
    private String contactPhone;

    // getters/setters
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
}