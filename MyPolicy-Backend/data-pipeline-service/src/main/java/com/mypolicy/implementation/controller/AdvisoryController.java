package com.mypolicy.implementation.controller;

import com.mypolicy.implementation.model.AdvisoryResponse;
import com.mypolicy.implementation.service.AdvisoryRuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/advisory")
public class AdvisoryController {

    private final AdvisoryRuleService advisoryRuleService;

    public AdvisoryController(AdvisoryRuleService advisoryRuleService) {
        this.advisoryRuleService = advisoryRuleService;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<AdvisoryResponse> getAdvisory(@PathVariable Integer customerId) {
        return ResponseEntity.ok(advisoryRuleService.generateAdvisory(customerId));
    }
}
