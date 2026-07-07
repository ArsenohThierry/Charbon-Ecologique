package com.example.charbonecolo.exception;

public class FieldBusinessException extends BusinessException {

    private final String champ;

    public FieldBusinessException(String champ, String message) {
        super(message);
        this.champ = champ;
    }

    public String getChamp() {
        return champ;
    }
}