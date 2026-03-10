package com.mypolicy.customer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Model for customer_details collection (MongoDB).
 * Used for login authentication - matches full name + PAN.
 * Same schema as data-pipeline-service's customer_details.
 */
@Document(collection = "customer_details")
public class CustomerDetails {

    @Id
    private String id;
    private Integer customerId;
    private String refCustItNum;      // PAN - used as "password" for login
    private Object refPhoneMobile;
    private Integer datBirthCust;
    private String custEmailID;
    private String customerFullName;   // Full name - used as "Customer ID/User ID" for login

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public String getRefCustItNum() { return refCustItNum; }
    public void setRefCustItNum(String refCustItNum) { this.refCustItNum = refCustItNum; }
    public Object getRefPhoneMobile() { return refPhoneMobile; }
    public void setRefPhoneMobile(Object refPhoneMobile) { this.refPhoneMobile = refPhoneMobile; }
    public Integer getDatBirthCust() { return datBirthCust; }
    public void setDatBirthCust(Integer datBirthCust) { this.datBirthCust = datBirthCust; }
    public String getCustEmailID() { return custEmailID; }
    public void setCustEmailID(String custEmailID) { this.custEmailID = custEmailID; }
    public String getCustomerFullName() { return customerFullName; }
    public void setCustomerFullName(String customerFullName) { this.customerFullName = customerFullName; }
}
