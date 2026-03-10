package com.mypolicy.implementation.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "health_insurance")
public class HealthInsurance {

    @Id
    private String id;
    @Field("Policy Number")
    private String policyNumber;
    @Field("Customer Name")
    private String customerName;
    private Integer DOB;
    private String PAN;
    private Object Mobile;
    private String Email;
    private String Insurer;
    @Field("Plan Name")
    private String planName;
    @Field("Coverage Amount")
    private Integer coverageAmount;
    @Field("Annual Premium")
    private Integer annualPremium;
    @Field("Policy Start Date")
    private Integer policyStartDate;
    @Field("Policy End Date")
    private Integer policyEndDate;
    @Field("Policy Type")
    private String policyType;
    private String City;

    public String getId() { return id; }
    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }
    public Integer getDOB() { return DOB; }
    public void setDOB(Integer DOB) { this.DOB = DOB; }
    public String getPAN() { return PAN; }
    public void setPAN(String PAN) { this.PAN = PAN; }
    public Object getMobile() { return Mobile; }
    public void setMobile(Object mobile) { this.Mobile = mobile; }
    public String getEmail() { return Email; }
    public void setEmail(String email) { this.Email = email; }
    public String getInsurer() { return Insurer; }
    public void setInsurer(String insurer) { this.Insurer = insurer; }
    public Integer getCoverageAmount() { return coverageAmount; }
    public void setCoverageAmount(Integer coverageAmount) { this.coverageAmount = coverageAmount; }
    public Integer getAnnualPremium() { return annualPremium; }
    public void setAnnualPremium(Integer annualPremium) { this.annualPremium = annualPremium; }
    public Integer getPolicyStartDate() { return policyStartDate; }
    public void setPolicyStartDate(Integer policyStartDate) { this.policyStartDate = policyStartDate; }
    public Integer getPolicyEndDate() { return policyEndDate; }
    public void setPolicyEndDate(Integer policyEndDate) { this.policyEndDate = policyEndDate; }
}
