package com.mypolicy.implementation.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "life_insurance")
public class LifeInsurance {

    @Id
    private String id;
    @Field("PolicyNum")
    private String PolicyNum;
    @Field("CustomerName")
    private String customerName;
    private Integer DOB;
    private String PAN;
    private Object Mobile;
    private String Email;
    private String Insurer;
    private String PlanName;
    private Integer PolicyTerm;
    private Integer SumAssured;
    private Integer AnnualPrem;
    private Integer PolicyStart;
    private Integer PolicyEnd;
    private String NomineeName;
    private String NomineeRelation;
    private String City;

    public String getId() { return id; }
    public String getPolicyNum() { return PolicyNum; }
    public void setPolicyNum(String policyNum) { this.PolicyNum = policyNum; }
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
    public Integer getSumAssured() { return SumAssured; }
    public void setSumAssured(Integer sumAssured) { this.SumAssured = sumAssured; }
    public Integer getAnnualPrem() { return AnnualPrem; }
    public void setAnnualPrem(Integer annualPrem) { this.AnnualPrem = annualPrem; }
    public Integer getPolicyStart() { return PolicyStart; }
    public void setPolicyStart(Integer policyStart) { this.PolicyStart = policyStart; }
    public Integer getPolicyEnd() { return PolicyEnd; }
    public void setPolicyEnd(Integer policyEnd) { this.PolicyEnd = policyEnd; }
}
