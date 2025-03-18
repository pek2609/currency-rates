package org.example.currency_rates.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CurrencyRatesExternalWebClientConfig {

    @Value("${currency.rates.external.url}")
    private String uri;

    @Value("${currency.rates.external.api.key}")
    private String apiKey;

    @Bean
    public WebClient currencyRatesExternalWebClient() {
        return WebClient.builder()
                .baseUrl(uri)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-API-KEY", apiKey)
                .build();
    }
}
