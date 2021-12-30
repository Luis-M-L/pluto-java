package com.example.pluto.entities;

public class ExchangeError {

    public static final String NO_RESPONSE_FROM_EXCHANGE = "Unable to get response from exchange";

    private String errorCode;
    private String message;

    public ExchangeError() {
    }

    public ExchangeError(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ExchangeError{" +
                "errorCode='" + errorCode + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
