package org.example.currency_rates.service;

import org.example.currency_rates.domain.CurrencyRate;
import org.example.currency_rates.domain.CurrencyRateType;
import org.example.currency_rates.dto.CurrencyRateDto;
import org.example.currency_rates.dto.CurrencyRatesResponseDto;
import org.example.currency_rates.exception.CurrencyRateProviderException;
import org.example.currency_rates.exception.ServiceProcessingException;
import org.example.currency_rates.integrations.CurrencyRateProvider;
import org.example.currency_rates.repository.CurrencyRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
public class CurrencyRateServiceBean implements CurrencyRateService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyRateServiceBean.class);

    private final CurrencyRateRepository currencyRateRepository;
    private final CurrencyRateProvider currencyRateProvider;

    public CurrencyRateServiceBean(CurrencyRateRepository currencyRateRepository, CurrencyRateProvider currencyRateProvider) {
        this.currencyRateRepository = currencyRateRepository;
        this.currencyRateProvider = currencyRateProvider;
    }

    @Override
    public Mono<CurrencyRatesResponseDto> getCurrencyRatesData() {
        return Mono.zip(
                processCurrencyRates(currencyRateProvider.getFiatRates(), CurrencyRateType.fiat),
                processCurrencyRates(currencyRateProvider.getCryptoRates(), CurrencyRateType.crypto)
        ).map(tuple -> new CurrencyRatesResponseDto(tuple.getT1(), tuple.getT2()));
    }

    @Override
    public Mono<CurrencyRate> refreshCurrencyRate(CurrencyRate currencyRate) {
        return currencyRateRepository.findByCurrencyTypeAndCurrency(currencyRate.getCurrencyType(), currencyRate.getCurrency())
                .flatMap(existingRate -> {
                    existingRate.setRate(currencyRate.getRate());
                    return currencyRateRepository.save(existingRate);
                })
                .switchIfEmpty((currencyRateRepository.save(currencyRate)));
    }

    private Mono<List<CurrencyRateDto>> processCurrencyRates(Flux<CurrencyRateDto> currencyRateFlux, CurrencyRateType type) {
        return currencyRateFlux
                .map(dto -> toEntity(type, dto))
                .flatMap(this::refreshCurrencyRate)
                .map(this::toDto)
                .collectList()
                .onErrorResume(CurrencyRateProviderException.class, e -> {
                    log.warn("Failed to get {} currency rates from CurrencyRateProvider, returning saved data: {}", type, e.getMessage());
                    return getSavedRates(type);
                })
                .onErrorMap(throwable -> !(throwable instanceof CurrencyRateProviderException), throwable -> {
                    log.error("Unable to process {} currency rates data", type, throwable);
                    throw new ServiceProcessingException("Failed to process " + type + " rates", throwable);
                });

    }

    private Mono<List<CurrencyRateDto>> getSavedRates(CurrencyRateType type) {
        return currencyRateRepository.findByCurrencyType(type.name())
                .map(this::toDto)
                .collectList()
                .defaultIfEmpty(Collections.emptyList())
                .doOnSuccess(rates -> log.debug("Retrieved {} saved rates for {}", rates.size(), type));
    }

    private CurrencyRateDto toDto(CurrencyRate currencyRate) {
        return new CurrencyRateDto(currencyRate.getCurrency(), currencyRate.getRate());
    }

    private CurrencyRate toEntity(CurrencyRateType type, CurrencyRateDto currencyRateDto) {
        CurrencyRate currencyRate = new CurrencyRate();
        currencyRate.setCurrencyType(type.name());
        currencyRate.setCurrency(currencyRateDto.currency());
        currencyRate.setRate(currencyRateDto.rate());

        return currencyRate;
    }

}
