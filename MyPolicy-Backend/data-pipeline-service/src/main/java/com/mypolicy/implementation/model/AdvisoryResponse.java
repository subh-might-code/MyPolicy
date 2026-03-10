package com.mypolicy.implementation.model;

import java.util.List;

public class AdvisoryResponse {

    private Integer customerId;
    private List<AdvisoryNote> advisory;
    private AdvisorySummary summary;
    private List<UnifiedPortfolioResponse.PolicySummary> unifiedView;

    public static class AdvisorySummary {
        private int totalPolicies;
        private List<String> categoriesPresent;
        private int gapsIdentified;

        public int getTotalPolicies() { return totalPolicies; }
        public void setTotalPolicies(int totalPolicies) { this.totalPolicies = totalPolicies; }
        public List<String> getCategoriesPresent() { return categoriesPresent; }
        public void setCategoriesPresent(List<String> categoriesPresent) { this.categoriesPresent = categoriesPresent; }
        public int getGapsIdentified() { return gapsIdentified; }
        public void setGapsIdentified(int gapsIdentified) { this.gapsIdentified = gapsIdentified; }
    }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public List<AdvisoryNote> getAdvisory() { return advisory; }
    public void setAdvisory(List<AdvisoryNote> advisory) { this.advisory = advisory; }
    public AdvisorySummary getSummary() { return summary; }
    public void setSummary(AdvisorySummary summary) { this.summary = summary; }
    public List<UnifiedPortfolioResponse.PolicySummary> getUnifiedView() { return unifiedView; }
    public void setUnifiedView(List<UnifiedPortfolioResponse.PolicySummary> unifiedView) { this.unifiedView = unifiedView; }
}
