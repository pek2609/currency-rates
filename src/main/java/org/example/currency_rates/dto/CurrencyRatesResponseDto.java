package org.example.currency_rates.dto;


import java.util.List;

public record CurrencyRatesResponseDto(List<CurrencyRateDto> fiat, List<CurrencyRateDto> crypto) {
}
