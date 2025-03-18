package org.example.currency_rates.dto;

import java.math.BigDecimal;

public record CryptoCurrencyRateExternalDto(String name, BigDecimal value) {

    public CurrencyRateDto toCurrencyRateDto() {
        return new CurrencyRateDto(name, value);
    }
}
