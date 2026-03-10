package com.mypolicy.implementation.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "unified_portfolio")
public class UnifiedPortfolioRecord {

    @Id
    private String id;
    private Integer customerId;
    private String policyId;
    private String insurer;
    private BigDecimal premium;
    private Integer sumAssured;
    private Integer startDate;
    private Integer policyEnd;
    private String sourceCollection;
    private String matchMethod;
    private String encryptedPan;
    private String encryptedMobile;
    private String encryptedEmail;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }
    public String getInsurer() { return insurer; }
    public void setInsurer(String insurer) { this.insurer = insurer; }
    public BigDecimal getPremium() { return premium; }
    public void setPremium(BigDecimal premium) { this.premium = premium; }
    public Integer getSumAssured() { return sumAssured; }
    public void setSumAssured(Integer sumAssured) { this.sumAssured = sumAssured; }
    public Integer getStartDate() { return startDate; }
    public void setStartDate(Integer startDate) { this.startDate = startDate; }
    public Integer getPolicyEnd() { return policyEnd; }
    public void setPolicyEnd(Integer policyEnd) { this.policyEnd = policyEnd; }
    public String getSourceCollection() { return sourceCollection; }
    public void setSourceCollection(String sourceCollection) { this.sourceCollection = sourceCollection; }
    public String getMatchMethod() { return matchMethod; }
    public void setMatchMethod(String matchMethod) { this.matchMethod = matchMethod; }
    public String getEncryptedPan() { return encryptedPan; }
    public void setEncryptedPan(String encryptedPan) { this.encryptedPan = encryptedPan; }
    public String getEncryptedMobile() { return encryptedMobile; }
    public void setEncryptedMobile(String encryptedMobile) { this.encryptedMobile = encryptedMobile; }
    public String getEncryptedEmail() { return encryptedEmail; }
    public void setEncryptedEmail(String encryptedEmail) { this.encryptedEmail = encryptedEmail; }
}
