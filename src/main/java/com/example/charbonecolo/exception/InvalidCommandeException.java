package com.example.charbonecolo.exception;

import java.util.Map;

import com.example.charbonecolo.controller.CommandeController;
import com.example.charbonecolo.model.CommandeModel;

public class InvalidCommandeException extends Exception {
    Map<String, String> fieldErrors;
    CommandeModel input;

    public CommandeModel getInput() {
        return input;
    }

    public void setInput(CommandeModel input) {
        this.input = input;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
