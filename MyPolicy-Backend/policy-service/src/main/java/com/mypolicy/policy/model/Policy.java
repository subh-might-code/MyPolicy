package com.mypolicy.policy.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "policies")
@CompoundIndexes({
    @CompoundIndex(name = "policyNumber_insurerId", def = "{'policyNumber': 1, 'insurerId': 1}", unique = true)
})
public class Policy {

  @Id
  private String id;

  private String customerId; // Linked to Customer Service

  private String insurerId; // Linked to Metadata Service rules

  private String policyNumber;

  private String policyType; // e.g., TERM_LIFE, HEALTH

  private String planName;

  private BigDecimal premiumAmount;

  private BigDecimal sumAssured;

  private LocalDate startDate;
  private LocalDate endDate;

  private PolicyStatus status;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
