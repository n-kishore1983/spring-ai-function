package com.learning.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StockPriceResponse(
        String ticker,
        String name,
        Double price,
        String exchange,
        Long updated,
        String currency,
        Long volume
) {
}
