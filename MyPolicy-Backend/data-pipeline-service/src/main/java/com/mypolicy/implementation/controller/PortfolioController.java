package com.mypolicy.implementation.controller;

import com.mypolicy.implementation.model.UnifiedPortfolioResponse;
import com.mypolicy.implementation.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<UnifiedPortfolioResponse> getUnifiedPortfolio(@PathVariable Integer customerId) {
        return ResponseEntity.ok(portfolioService.getUnifiedPortfolio(customerId));
    }
}
