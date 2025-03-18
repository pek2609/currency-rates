package org.example.currency_rates.dto;

import java.math.BigDecimal;

public record CurrencyRateDto(String currency, BigDecimal rate) {
    @Override
    public String toString() {
        return "CurrencyRateDto{" +
                "currency='" + currency + '\'' +
                ", rate=" + rate +
                '}';
    }
}
