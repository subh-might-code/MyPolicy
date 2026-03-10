package com.mypolicy.implementation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);

    private static final String AUDIT_PREFIX = "[AUDIT]";

    public void logMapping(String collectionName, int recordCount) {
        log.info("{} MAPPING | collection={} | records={} | ts={}",
                AUDIT_PREFIX, collectionName, recordCount, Instant.now());
    }

    public void logStitching(String matchMethod, String policyId, Integer customerId) {
        log.info("{} STITCHING | matchMethod={} | policyId={} | customerId={} | ts={}",
                AUDIT_PREFIX, matchMethod, policyId, customerId, Instant.now());
    }

    public void logStitchingComplete(int totalProcessed, int matched, int unmatched) {
        log.info("{} STITCHING_COMPLETE | totalProcessed={} | matched={} | unmatched={} | ts={}",
                AUDIT_PREFIX, totalProcessed, matched, unmatched, Instant.now());
    }
}
