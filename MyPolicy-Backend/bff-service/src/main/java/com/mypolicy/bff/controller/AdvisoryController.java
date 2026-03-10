package com.mypolicy.bff.controller;

import com.mypolicy.bff.dto.DataPipelineAdvisoryResponse;
import com.mypolicy.bff.service.AdvisoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Coverage advisory from data-pipeline-service (unified_portfolio).
 */
@RestController
@RequestMapping("/api/bff/advisory")
@RequiredArgsConstructor
public class AdvisoryController {

  private final AdvisoryService advisoryService;

  @GetMapping("/{customerId}")
  public ResponseEntity<DataPipelineAdvisoryResponse> getAdvisory(@PathVariable String customerId) {
    return ResponseEntity.ok(advisoryService.getAdvisory(customerId));
  }
}
