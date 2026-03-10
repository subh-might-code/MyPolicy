package com.mypolicy.implementation.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Standardized output from the metadata engine. All insurer formats map to this schema.
 */
public class StandardizedPolicyDTO {

    private String sourceCollection;
    private String originalId;
    private String policyId;
    private BigDecimal premium;
    private Integer sumAssured;
    private LocalDate startDate;
    private LocalDate policyEnd;
    private String pan;
    private Object mobile;
    private String email;
    private LocalDate dob;
    private String insurer;

    public String getSourceCollection() { return sourceCollection; }
    public void setSourceCollection(String sourceCollection) { this.sourceCollection = sourceCollection; }
    public String getOriginalId() { return originalId; }
    public void setOriginalId(String originalId) { this.originalId = originalId; }
    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }
    public BigDecimal getPremium() { return premium; }
    public void setPremium(BigDecimal premium) { this.premium = premium; }
    public Integer getSumAssured() { return sumAssured; }
    public void setSumAssured(Integer sumAssured) { this.sumAssured = sumAssured; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getPolicyEnd() { return policyEnd; }
    public void setPolicyEnd(LocalDate policyEnd) { this.policyEnd = policyEnd; }
    public String getPan() { return pan; }
    public void setPan(String pan) { this.pan = pan; }
    public Object getMobile() { return mobile; }
    public void setMobile(Object mobile) { this.mobile = mobile; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public String getInsurer() { return insurer; }
    public void setInsurer(String insurer) { this.insurer = insurer; }
}
