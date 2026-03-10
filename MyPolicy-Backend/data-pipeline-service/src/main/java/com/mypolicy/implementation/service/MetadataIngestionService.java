package com.mypolicy.implementation.service;

import com.mypolicy.implementation.config.MappingConfig;
import com.mypolicy.implementation.model.StandardizedRecord;
import com.mypolicy.implementation.util.DataMassagingUtil;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generic Metadata-Driven Ingestion. Uses mapping config to standardize records
 * from any insurer collection without hard-coding field names.
 */
@Service
public class MetadataIngestionService {

    private static final Logger log = LoggerFactory.getLogger(MetadataIngestionService.class);

    private final MongoTemplate mongoTemplate;
    private final MappingConfig mappingConfig;
    private final AuditLogger auditLogger;

    public MetadataIngestionService(MongoTemplate mongoTemplate, MappingConfig mappingConfig, AuditLogger auditLogger) {
        this.mongoTemplate = mongoTemplate;
        this.mappingConfig = mappingConfig;
        this.auditLogger = auditLogger;
    }

    private static final List<String> POLICY_COLLECTIONS = List.of(
            "life_insurance", "auto_insurance", "health_insurance"
    );

    public List<StandardizedRecord> standardizeCollection(String collectionName) {
        Map<String, String> mapping = mappingConfig.getMappingForCollection(collectionName);
        if (mapping.isEmpty()) {
            log.warn("No mapping for collection: {}", collectionName);
            return List.of();
        }

        List<Document> docs = mongoTemplate.findAll(Document.class, collectionName);
        List<StandardizedRecord> records = new ArrayList<>();

        for (Document doc : docs) {
            String objectId = doc.getObjectId("_id").toString();
            Map<String, Object> docMap = doc.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> (Object) e.getValue()));
            StandardizedRecord rec = StandardizedRecord.fromMongoDoc(
                    collectionName, objectId, mapping, docMap
            );
            records.add(rec);
        }

        auditLogger.logMapping(collectionName, records.size());
        log.info("Standardized {} records from {}", records.size(), collectionName);
        return records;
    }

    /**
     * Standardize all policy collections (life, auto, health). Excludes customer_details.
     */
    public Map<String, List<StandardizedRecord>> standardizeAllPolicies() {
        return POLICY_COLLECTIONS.stream()
                .collect(Collectors.toMap(c -> c, this::standardizeCollection));
    }

    /**
     * Ingest parsed CSV rows into MongoDB. Inserts documents into the given collection.
     * Call standardizeAllPolicies() after to get StandardizedRecords for stitching.
     *
     * @param collectionName Target collection (e.g. auto_insurance, life_insurance, health_insurance)
     * @param rows Parsed CSV rows as List of Maps (header -> value)
     */
    public void ingestRecords(String collectionName, List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            log.warn("No rows to ingest for collection: {}", collectionName);
            return;
        }

        Map<String, String> mapping = mappingConfig.getMappingForCollection(collectionName);
        if (mapping.isEmpty()) {
            log.warn("No mapping for collection: {}", collectionName);
            throw new IllegalArgumentException("Unknown collection: " + collectionName);
        }

        List<Document> documents = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> coerced = coerceNumericFields(row, mapping);
            documents.add(new Document(coerced));
        }

        mongoTemplate.insert(documents, collectionName);
        auditLogger.logMapping(collectionName + "_ingest", documents.size());
        log.info("Ingested {} records into {}", documents.size(), collectionName);
    }

    private Map<String, Object> coerceNumericFields(Map<String, Object> row, Map<String, String> mapping) {
        List<String> numericSourceKeys = List.of("DOB", "Mobile", "PolicyStartDate", "PolicyEndDate",
                "PolicyStart", "PolicyEnd", "IDV", "AnnualPremium", "SumAssured", "AnnualPrem",
                "Coverage Amount", "Annual Premium", "Policy Start Date", "Policy End Date",
                "customerId", "refPhoneMobile", "datBirthCust");
        Map<String, Object> result = new HashMap<>(row);
        for (String key : result.keySet()) {
            Object val = result.get(key);
            if (val != null && val instanceof String s && !s.isBlank() && numericSourceKeys.contains(key)) {
                try {
                    if (s.contains(".")) {
                        result.put(key, Double.parseDouble(s));
                    } else {
                        long n = Long.parseLong(s.replaceAll("[^0-9-]", ""));
                        result.put(key, n <= Integer.MAX_VALUE ? (int) n : n);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return result;
    }
}
