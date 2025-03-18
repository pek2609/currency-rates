package org.example.currency_rates.dto;


import java.math.BigDecimal;

public record FiatCurrencyRateExternalDto(String currency, BigDecimal rate) {

    public CurrencyRateDto toCurrencyRateDto() {
        return new CurrencyRateDto(currency, rate);
    }
}
