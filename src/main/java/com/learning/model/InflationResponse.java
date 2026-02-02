package com.learning.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InflationResponse(
        String country,
        @JsonProperty("country_code")
        String countryCode,
        String type,
        String period,
        @JsonProperty("monthly_rate_pct")
        Double monthlyRatePct,
        @JsonProperty("yearly_rate_pct")
        Double yearlyRatePct
)  {

}