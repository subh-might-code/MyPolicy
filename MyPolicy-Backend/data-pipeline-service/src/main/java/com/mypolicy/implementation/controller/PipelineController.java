package com.mypolicy.implementation.controller;

import com.mypolicy.implementation.service.PipelineOrchestratorService;
import com.mypolicy.implementation.service.StitchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Tag(name = "Pipeline", description = "CSV upload and pipeline execution")

@RestController
@RequestMapping("/api/pipeline")
public class PipelineController {

    private final PipelineOrchestratorService pipelineOrchestrator;

    public PipelineController(PipelineOrchestratorService pipelineOrchestrator) {
        this.pipelineOrchestrator = pipelineOrchestrator;
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runPipeline() {
        StitchingService.StitchingResult result = pipelineOrchestrator.runFullPipeline();
        return ResponseEntity.ok(Map.of(
                "totalProcessed", result.totalProcessed(),
                "matched", result.matched(),
                "unmatched", result.unmatched()
        ));
    }

    @Operation(summary = "Upload CSV", description = "Upload a CSV file. Click 'Choose File' for file, enter collectionName in the text field.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadCsv(
            @RequestPart("file") @Schema(type = "string", format = "binary", description = "CSV file") MultipartFile file,
            @RequestPart("collectionName") @Schema(example = "auto_insurance") String collectionName) throws IOException {
        StitchingService.StitchingResult result = pipelineOrchestrator.uploadAndProcess(file, collectionName);
        return ResponseEntity.ok(Map.of(
                "message", "File uploaded and pipeline executed",
                "collectionName", collectionName,
                "totalProcessed", result.totalProcessed(),
                "matched", result.matched(),
                "unmatched", result.unmatched()
        ));
    }
}
