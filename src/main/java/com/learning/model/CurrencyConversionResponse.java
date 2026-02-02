package com.learning.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CurrencyConversionResponse(
        @JsonProperty("currency_pair")
        String currencyPair,
        @JsonProperty("exchange_rate")
        Double exchangeRate
) {
}
