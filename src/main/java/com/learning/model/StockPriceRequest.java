package com.learning.model;

import org.springframework.ai.tool.annotation.ToolParam;

public record StockPriceRequest(
        @ToolParam(description = "The stock ticker symbol, e.g., AAPL") String symbol
) {
}
