package com.mypolicy.customer.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "customers")
public class Customer {

  @Id
  private String customerId;

  private String firstName;
  private String lastName;

  @Indexed(unique = true)
  private String email;

  @Indexed(unique = true)
  private String mobileNumber;

  @Indexed(unique = true)
  private String panNumber;

  private LocalDate dateOfBirth;
  private String passwordHash;

  private String permanentAddressLine1;
  private String permanentAddressLine2;
  private String permanentAddressLine3;
  private String permanentAddressCity;
  private String permanentAddressZip;
  private String customerAddressZip;
  private String address;

  @Builder.Default
  private CustomerStatus status = CustomerStatus.ACTIVE;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
