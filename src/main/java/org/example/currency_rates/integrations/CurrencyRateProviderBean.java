package org.example.currency_rates.integrations;

import org.example.currency_rates.dto.CurrencyRateDto;
import org.example.currency_rates.dto.CryptoCurrencyRateExternalDto;
import org.example.currency_rates.dto.FiatCurrencyRateExternalDto;
import org.example.currency_rates.exception.CurrencyRateProviderException;
import org.example.currency_rates.service.CurrencyRateServiceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class CurrencyRateProviderBean implements CurrencyRateProvider {
    private static final Logger log = LoggerFactory.getLogger(CurrencyRateProviderBean.class);
    private final WebClient webClient;
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    public CurrencyRateProviderBean(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Flux<CurrencyRateDto> getFiatRates() {
        return webClient.get()
                .uri("/fiat-currency-rates")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new CurrencyRateProviderException("Failed to get fiat rates: " + response.statusCode())))
                .bodyToFlux(FiatCurrencyRateExternalDto.class)
                .map(FiatCurrencyRateExternalDto::toCurrencyRateDto)
                .timeout(TIMEOUT)
                .doOnError(e -> log.error("Error fetching fiat rates", e));
    }

    @Override
    public Flux<CurrencyRateDto> getCryptoRates() {
        return webClient.get()
                .uri("/crypto-currency-rates")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new CurrencyRateProviderException("Failed to get crypto rates: " + response.statusCode())))
                .bodyToFlux(CryptoCurrencyRateExternalDto.class)
                .map(CryptoCurrencyRateExternalDto::toCurrencyRateDto)
                .timeout(TIMEOUT)
                .doOnError(e -> log.error("Error fetching fiat rates", e));
    }


}
