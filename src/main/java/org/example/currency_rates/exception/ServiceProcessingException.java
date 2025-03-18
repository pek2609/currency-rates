package org.example.currency_rates.exception;

public class ServiceProcessingException extends RuntimeException {

    public ServiceProcessingException() {
        super();
    }

    public ServiceProcessingException(String message) {
        super(message);
    }

    public ServiceProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceProcessingException(Throwable cause) {
        super(cause);
    }
}
