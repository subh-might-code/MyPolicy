package com.mypolicy.implementation.model;

public class AdvisoryNote {

    public enum Type { PROTECTION_GAP, SUM_ASSURED_ADEQUACY, TEMPORAL_GAP }
    public enum Severity { high, medium, low }

    private Type type;
    private Severity severity;
    private String message;
    private String policyId;

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }
}
