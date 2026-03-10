package com.mypolicy.implementation.service;

import com.mypolicy.implementation.model.StandardizedRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class PipelineOrchestratorService {

    private final MetadataIngestionService metadataIngestionService;
    private final StitchingService stitchingService;
    private final FileProcessingService fileProcessingService;

    public PipelineOrchestratorService(MetadataIngestionService metadataIngestionService,
                                      StitchingService stitchingService,
                                      FileProcessingService fileProcessingService) {
        this.metadataIngestionService = metadataIngestionService;
        this.stitchingService = stitchingService;
        this.fileProcessingService = fileProcessingService;
    }

    public StitchingService.StitchingResult runFullPipeline() {
        Map<String, List<StandardizedRecord>> standardized = metadataIngestionService.standardizeAllPolicies();
        List<StandardizedRecord> allPolicies = standardized.values().stream()
                .flatMap(List::stream)
                .toList();
        return stitchingService.stitchPolicies(allPolicies);
    }

    /**
     * Accepts an uploaded CSV file and collection name. Parses the file, ingests into MongoDB,
     * then triggers full standardization and stitching.
     */
    public StitchingService.StitchingResult uploadAndProcess(MultipartFile file, String collectionName) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }
        List<Map<String, Object>> rows = fileProcessingService.parseCsv(file.getInputStream());
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("CSV file has no data rows");
        }
        metadataIngestionService.ingestRecords(collectionName, rows);
        List<StandardizedRecord> allPolicies = metadataIngestionService.standardizeAllPolicies().values().stream()
                .flatMap(List::stream)
                .toList();
        return stitchingService.stitchPolicies(allPolicies);
    }
}
