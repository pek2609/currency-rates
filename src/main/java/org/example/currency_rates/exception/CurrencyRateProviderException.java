package org.example.currency_rates.exception;

public class CurrencyRateProviderException extends RuntimeException {

    public CurrencyRateProviderException() {
        super();
    }

    public CurrencyRateProviderException(String message) {
        super(message);
    }

    public CurrencyRateProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public CurrencyRateProviderException(Throwable cause) {
        super(cause);
    }
}
