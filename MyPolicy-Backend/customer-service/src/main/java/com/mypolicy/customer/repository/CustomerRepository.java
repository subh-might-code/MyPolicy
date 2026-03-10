package com.mypolicy.customer.repository;

import com.mypolicy.customer.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {
  Optional<Customer> findByEmail(String email);

  Optional<Customer> findByMobileNumber(String mobileNumber);

  Optional<Customer> findByPanNumber(String panNumber);

  boolean existsByEmail(String email);

  boolean existsByMobileNumber(String mobileNumber);
}
