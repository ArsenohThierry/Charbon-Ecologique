package com.example.charbonecolo.exception;

import java.util.Map;

import com.example.charbonecolo.dto.DetailErrorWrapper;

public class InvalidDetailException extends Exception {
    private DetailErrorWrapper fieldErrors;

    public DetailErrorWrapper getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(DetailErrorWrapper fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
