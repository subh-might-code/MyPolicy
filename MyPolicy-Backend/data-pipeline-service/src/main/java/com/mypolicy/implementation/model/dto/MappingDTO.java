package com.mypolicy.implementation.model.dto;

import java.util.Map;

/**
 * Represents mapping config for a collection. Maps standard keys to source field names.
 * Loaded from mapping_config.json for zero-code-change metadata engine.
 */
public record MappingDTO(Map<String, String> fields) {

    public String getSourceField(String standardKey) {
        return fields != null ? fields.get(standardKey) : null;
    }
}
