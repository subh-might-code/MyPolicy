package com.mypolicy.implementation.model;

import com.mypolicy.implementation.util.DataMassagingUtil;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Standardized record after metadata mapping. Uses consistent keys regardless of source.
 */
public class StandardizedRecord {

    private String sourceCollection;
    private String originalId;
    private String policyId;
    private BigDecimal premium;
    private Integer sumAssured;
    private Integer startDate;
    private Integer policyEnd;
    private String pan;
    private Object mobile;
    private String email;
    private Integer dob;
    private String insurer;
    private Integer custId;

    public static StandardizedRecord fromMongoDoc(String collectionName, String objectId, Map<String, String> mapping, Map<String, Object> doc) {
        StandardizedRecord rec = new StandardizedRecord();
        rec.sourceCollection = collectionName;
        rec.originalId = objectId;

        for (Map.Entry<String, String> e : mapping.entrySet()) {
            String standardKey = e.getKey();
            String sourceKey = e.getValue();
            Object val = doc.get(sourceKey);
            if (val == null) continue;
            switch (standardKey) {
                case "policy_id" -> rec.policyId = asString(val);
                case "premium" -> rec.premium = DataMassagingUtil.normalizeCurrency(val);
                case "sum_assured" -> rec.sumAssured = toInt(val);
                case "start_date" -> rec.startDate = toInt(val);
                case "policy_end" -> rec.policyEnd = toInt(val);
                case "pan" -> rec.pan = asString(val);
                case "mobile" -> rec.mobile = DataMassagingUtil.normalizeMobile(val);
                case "email" -> rec.email = asString(val);
                case "dob" -> rec.dob = toInt(val);
                case "insurer" -> rec.insurer = asString(val);
                case "cust_id" -> rec.custId = toInt(val);
                default -> {}
            }
        }
        return rec;
    }

    private static String asString(Object o) { return o == null ? null : o.toString(); }
    private static Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(o.toString()); } catch (NumberFormatException e) { return null; }
    }
    public String getSourceCollection() { return sourceCollection; }
    public String getOriginalId() { return originalId; }
    public String getPolicyId() { return policyId; }
    public BigDecimal getPremium() { return premium; }
    public Integer getSumAssured() { return sumAssured; }
    public Integer getStartDate() { return startDate; }
    public Integer getPolicyEnd() { return policyEnd; }
    public String getPan() { return pan; }
    public Object getMobile() { return mobile; }
    public String getEmail() { return email; }
    public Integer getDob() { return dob; }
    public String getInsurer() { return insurer; }
    public Integer getCustId() { return custId; }
}
