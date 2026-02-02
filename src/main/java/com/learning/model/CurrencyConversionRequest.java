package com.learning.model;

import org.springframework.ai.tool.annotation.ToolParam;

public record CurrencyConversionRequest(
        @ToolParam(description = "source currency code, e.g., USD") String from,
        @ToolParam(description = "target currency code, e.g., EUR") String to
) {
}
