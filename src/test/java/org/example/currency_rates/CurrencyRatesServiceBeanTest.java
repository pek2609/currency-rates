package org.example.currency_rates;

import org.example.currency_rates.domain.CurrencyRate;
import org.example.currency_rates.domain.CurrencyRateType;
import org.example.currency_rates.dto.CurrencyRateDto;
import org.example.currency_rates.exception.CurrencyRateProviderException;
import org.example.currency_rates.integrations.CurrencyRateProvider;
import org.example.currency_rates.repository.CurrencyRateRepository;
import org.example.currency_rates.service.CurrencyRateServiceBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CurrencyRatesServiceBeanTest {

    @Mock
    private CurrencyRateRepository currencyRateRepository;

    @Mock
    private CurrencyRateProvider currencyRateProvider;

    @InjectMocks
    private CurrencyRateServiceBean currencyRateService;

    @BeforeEach
    void setUp() {
        currencyRateService = new CurrencyRateServiceBean(currencyRateRepository, currencyRateProvider);
    }

    @Test
    void getCurrencyRatesData_successfulProcessing() {
        CurrencyRateDto fiatDto = new CurrencyRateDto("USD", BigDecimal.valueOf(23.111998311823097));
        CurrencyRateDto cryptoDto = new CurrencyRateDto("BTC", BigDecimal.valueOf(39490.66971705329));

        when(currencyRateProvider.getFiatRates()).thenReturn(Flux.just(fiatDto));
        when(currencyRateProvider.getCryptoRates()).thenReturn(Flux.just(cryptoDto));

        when(currencyRateRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(currencyRateRepository.findByCurrencyTypeAndCurrency(anyString(), anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(currencyRateService.getCurrencyRatesData())
                .expectNextMatches(response ->
                        response.fiat().getFirst().equals(fiatDto) &&
                                response.crypto().getFirst().equals(cryptoDto)
                )
                .verifyComplete();
    }

    @Test
    void getCurrencyRatesData_providerException_FiatFails() {
        CurrencyRateDto cryptoDto = new CurrencyRateDto("BTC", BigDecimal.valueOf(39490.66971705329));

        when(currencyRateProvider.getFiatRates())
                .thenReturn(Flux.error(new CurrencyRateProviderException("API down")));
        when(currencyRateProvider.getCryptoRates())
                .thenReturn(Flux.just(cryptoDto));

        CurrencyRate savedFiat = new CurrencyRate(CurrencyRateType.fiat.name(), "USD", BigDecimal.valueOf(23.111998311823097));
        CurrencyRateDto savedFiatDto = new CurrencyRateDto("USD", BigDecimal.valueOf(23.111998311823097));
        when(currencyRateRepository.findByCurrencyType(CurrencyRateType.fiat.name()))
                .thenReturn(Flux.just(savedFiat));
        when(currencyRateRepository.save(any()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(currencyRateRepository.findByCurrencyTypeAndCurrency(anyString(), anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(currencyRateService.getCurrencyRatesData())
                .expectNextMatches(response ->
                        response.fiat().getFirst().equals(savedFiatDto) &&
                                response.crypto().getFirst().equals(cryptoDto)
                )
                .verifyComplete();
    }

    @Test
    void getCurrencyRatesData_providerException_CryptoFails() {
        CurrencyRateDto fiatDto = new CurrencyRateDto("USD", BigDecimal.valueOf(23.111998311823097));

        when(currencyRateProvider.getFiatRates())
                .thenReturn(Flux.just(fiatDto));
        when(currencyRateProvider.getCryptoRates())
                .thenReturn(Flux.error(new CurrencyRateProviderException("API down")));

        CurrencyRate savedCrypto = new CurrencyRate(CurrencyRateType.crypto.name(), "BTC", BigDecimal.valueOf(39490.66971705329));
        CurrencyRateDto savedCryptoDto = new CurrencyRateDto("BTC", BigDecimal.valueOf(39490.66971705329));

        when(currencyRateRepository.findByCurrencyType(CurrencyRateType.crypto.name()))
                .thenReturn(Flux.just(savedCrypto));
        when(currencyRateRepository.save(any()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(currencyRateRepository.findByCurrencyTypeAndCurrency(anyString(), anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(currencyRateService.getCurrencyRatesData())
                .expectNextMatches(response ->
                        response.fiat().getFirst().equals(fiatDto) &&
                                response.crypto().getFirst().equals(savedCryptoDto)
                )
                .verifyComplete();
    }
}
