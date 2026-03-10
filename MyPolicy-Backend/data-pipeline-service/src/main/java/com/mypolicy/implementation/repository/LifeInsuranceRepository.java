package com.mypolicy.implementation.repository;

import com.mypolicy.implementation.model.LifeInsurance;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LifeInsuranceRepository extends MongoRepository<LifeInsurance, String> {
}
