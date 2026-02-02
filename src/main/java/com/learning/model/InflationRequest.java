package com.learning.model;

import org.springframework.ai.tool.annotation.ToolParam;


public record InflationRequest(
        @ToolParam(description = "The 2-letter country, e.g., CA") String country,
        @ToolParam(description = "the inflation type eg. HICP,CPI") String type) {
}
