package com.learning.tools;

import com.learning.exceptions.AIException;
import com.learning.model.CurrencyConversionRequest;
import com.learning.model.CurrencyConversionResponse;
import com.learning.model.InflationRequest;
import com.learning.model.InflationResponse;
import com.learning.model.StockPriceRequest;
import com.learning.model.StockPriceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


@Slf4j
@Service
public class NinjaTools {

    @Value("${ninjas.api.key}")
    private String ninjaApiKey;

    private final RestClient restClient;

    public NinjaTools() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.api-ninjas.com/v1")
                .build();
    }

    @Tool(description = "Get the latest inflation data for a specific country", name = "get_inflation_data")
    public InflationResponse getInflationResponse(InflationRequest request) {
        try {
            log.info("Invoking ninja api to get inflation data for country: {} and type: {}", request.country(), request.type());
            InflationResponse[] responses = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/inflation")
                            .queryParam("country", request.country())
                            .queryParam("type", request.type())
                            .build())
                    .header("X-Api-Key", ninjaApiKey)
                    .retrieve()
                    .body(InflationResponse[].class);

            if (responses != null && responses.length > 0) {
                return responses[0];
            } else {
                throw new AIException("No inflation data found for the given country and type");
            }
        } catch (AIException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error while calling Ninja API: {}", e.getMessage());
            throw new AIException("Error while fetching inflation data: " + e.getMessage());
        }
    }

    @Tool(description = "Convert currency from one to another", name = "convert_currency")
    public CurrencyConversionResponse convertCurrency(CurrencyConversionRequest request) {
            try {
                    log.info("Invoking ninja api to convert currency from: {} to: {} ", request.from(), request.to());
                    CurrencyConversionResponse response = restClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/exchangerate")
                                    .queryParam("pair", request.from()+"_"+request.to())
                                    .build())
                            .header("X-Api-Key", ninjaApiKey)
                            .retrieve()
                            .body(CurrencyConversionResponse.class);
                    if(response == null) {
                        throw new AIException("No currency conversion data found for the given pair");
                    }
                    return response;
            } catch (Exception ex) {
                log.error("Error while calling Ninja API for currency conversion: {}", ex.getMessage());
                throw new AIException("Error while converting currency: " + ex.getMessage());
            }
    }

    @Tool(description = "Get the current stock price for a given ticker symbol", name = "stock_price")
    public StockPriceResponse getStockPrice(StockPriceRequest request) {
        try {
            log.info("Invoking ninja api to get stock price for ticker: {}", request.symbol());
            StockPriceResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/stockprice")
                            .queryParam("ticker", request.symbol())
                            .build())
                    .header("X-Api-Key", ninjaApiKey)
                    .retrieve()
                    .body(StockPriceResponse.class);
            if (response == null) {
                throw new AIException("No stock price data found for ticker: " + request.symbol());
            }
            return response;
        } catch (AIException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error while calling Ninja API for stock price: {}", e.getMessage());
            throw new AIException("Error while fetching stock price: " + e.getMessage());
        }
    }
}
