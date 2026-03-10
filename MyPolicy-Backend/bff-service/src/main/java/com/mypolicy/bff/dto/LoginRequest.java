package com.mypolicy.bff.dto;

import lombok.Data;

@Data
public class LoginRequest {
  /** Customer ID / User ID = full name of the customer */
  private String customerIdOrUserId;
  /** Password = PAN card number */
  private String password;
}
