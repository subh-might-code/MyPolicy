package com.mypolicy.bff.service;

import com.mypolicy.bff.client.DataPipelineClient;
import com.mypolicy.bff.dto.DataPipelineAdvisoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Fetches coverage advisory from data-pipeline-service (unified_portfolio).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdvisoryService {

  private final DataPipelineClient dataPipelineClient;

  /**
   * Get coverage advisory for customer. customerId is the Integer from customer_details.
   */
  public DataPipelineAdvisoryResponse getAdvisory(String customerId) {
    log.info("Fetching advisory for customer: {}", customerId);

    Integer customerIdInt = parseCustomerId(customerId);
    if (customerIdInt == null) {
      throw new IllegalArgumentException("Invalid customer ID: " + customerId);
    }

    return dataPipelineClient.getAdvisory(customerIdInt);
  }

  private Integer parseCustomerId(String customerId) {
    if (customerId == null || customerId.isBlank()) return null;
    try {
      return Integer.parseInt(customerId.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
