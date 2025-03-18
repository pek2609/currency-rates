package org.example.currency_rates.repository;

import org.example.currency_rates.domain.CurrencyRate;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CurrencyRateRepository extends ReactiveCrudRepository<CurrencyRate, Long> {
    Flux<CurrencyRate> findByCurrencyType(String currencyType);
    Mono<CurrencyRate> findByCurrencyTypeAndCurrency(String currencyType, String currency);
}
