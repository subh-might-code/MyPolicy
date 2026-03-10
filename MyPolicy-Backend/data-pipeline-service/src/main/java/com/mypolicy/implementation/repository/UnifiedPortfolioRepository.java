package com.mypolicy.implementation.repository;

import com.mypolicy.implementation.model.UnifiedPortfolioRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UnifiedPortfolioRepository extends MongoRepository<UnifiedPortfolioRecord, String> {

    List<UnifiedPortfolioRecord> findByCustomerId(Integer customerId);
}
