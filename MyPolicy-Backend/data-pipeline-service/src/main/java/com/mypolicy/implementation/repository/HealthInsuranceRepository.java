package com.mypolicy.implementation.repository;

import com.mypolicy.implementation.model.HealthInsurance;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HealthInsuranceRepository extends MongoRepository<HealthInsurance, String> {
}
