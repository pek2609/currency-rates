package org.example.currency_rates.controller;

import org.example.currency_rates.dto.CurrencyRatesResponseDto;
import org.example.currency_rates.service.CurrencyRateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/currency-rates")
public class CurrencyRateController {
    private CurrencyRateService currencyRateService;

    public CurrencyRateController(CurrencyRateService currencyRateService) {
        this.currencyRateService = currencyRateService;
    }

    @GetMapping
    public Mono<CurrencyRatesResponseDto> getCurrencyRates() {
        return currencyRateService.getCurrencyRatesData();
    }
}
