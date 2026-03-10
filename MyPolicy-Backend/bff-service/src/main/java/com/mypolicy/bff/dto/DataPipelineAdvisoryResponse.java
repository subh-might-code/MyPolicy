package com.mypolicy.bff.dto;

import lombok.Data;

import java.util.List;

/**
 * Maps to data-pipeline-service AdvisoryResponse.
 */
@Data
public class DataPipelineAdvisoryResponse {
  private Integer customerId;
  private List<AdvisoryNote> advisory;
  private AdvisorySummary summary;
  private List<DataPipelinePortfolioResponse.PolicySummary> unifiedView;

  @Data
  public static class AdvisoryNote {
    private String type;
    private String severity;
    private String message;
    private String policyId;
  }

  @Data
  public static class AdvisorySummary {
    private int totalPolicies;
    private List<String> categoriesPresent;
    private int gapsIdentified;
  }
}
