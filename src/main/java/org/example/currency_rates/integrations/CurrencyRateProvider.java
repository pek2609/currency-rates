package org.example.currency_rates.integrations;

import org.example.currency_rates.dto.CurrencyRateDto;
import reactor.core.publisher.Flux;

public interface CurrencyRateProvider {

    Flux<CurrencyRateDto> getFiatRates();
    Flux<CurrencyRateDto> getCryptoRates();

}
