package com.example.charbonecolo.exception;

public class ImportException extends BusinessException {
    public ImportException(String message) {
        super(message);
    }

    public ImportException(String message, Throwable cause) {
        super(message + " (Cause: " + (cause != null ? cause.getMessage() : "inconnue") + ")");
    }
}
