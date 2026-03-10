package com.mypolicy.implementation.repository;

import com.mypolicy.implementation.model.AutoInsurance;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AutoInsuranceRepository extends MongoRepository<AutoInsurance, String> {
}
