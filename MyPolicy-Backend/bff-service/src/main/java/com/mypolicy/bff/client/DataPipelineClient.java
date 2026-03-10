package com.mypolicy.bff.client;

import com.mypolicy.bff.dto.DataPipelineAdvisoryResponse;
import com.mypolicy.bff.dto.DataPipelinePortfolioResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for data-pipeline-service.
 * Fetches unified portfolio and advisory from unified_portfolio collection.
 */
@FeignClient(name = "data-pipeline-service", contextId = "dataPipelineClient")
public interface DataPipelineClient {

  @GetMapping("/api/portfolio/{customerId}")
  DataPipelinePortfolioResponse getPortfolio(@PathVariable("customerId") Integer customerId);

  @GetMapping("/api/advisory/{customerId}")
  DataPipelineAdvisoryResponse getAdvisory(@PathVariable("customerId") Integer customerId);
}
