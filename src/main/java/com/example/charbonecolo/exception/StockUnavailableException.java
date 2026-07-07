package com.example.charbonecolo.exception;

import java.util.Map;

import com.example.charbonecolo.dto.SessionDetailErrorWrapper;

public class StockUnavailableException extends Exception {
    private Map<Integer, SessionDetailErrorWrapper> errors;

    public Map<Integer, SessionDetailErrorWrapper> getErrors() {
        return errors;
    }

    public void setErrors(Map<Integer, SessionDetailErrorWrapper> errors) {
        this.errors = errors;
    }
}
