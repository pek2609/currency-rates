package org.example.currency_rates.service;

import org.example.currency_rates.domain.CurrencyRate;
import org.example.currency_rates.dto.CurrencyRatesResponseDto;
import reactor.core.publisher.Mono;

public interface CurrencyRateService {

    Mono<CurrencyRatesResponseDto> getCurrencyRatesData();

    Mono<CurrencyRate> refreshCurrencyRate(CurrencyRate currencyRate);
}
