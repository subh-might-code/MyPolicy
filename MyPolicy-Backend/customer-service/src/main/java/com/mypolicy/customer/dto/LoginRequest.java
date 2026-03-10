package com.mypolicy.customer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
  /** Customer ID / User ID = full name of the customer */
  @NotBlank(message = "Customer ID / User ID (full name) is required")
  private String customerIdOrUserId;

  /** Password = PAN card number (matched against customer_details.refCustItNum) */
  @NotBlank(message = "Password (PAN) is required")
  private String password;
}
