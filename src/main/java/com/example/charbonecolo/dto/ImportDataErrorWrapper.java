package com.example.charbonecolo.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class ImportDataErrorWrapper {

    private int ligne;
    private Map<String, String> errors;

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    public ImportDataErrorWrapper() {
        errors = new LinkedHashMap<>();
    }

    public void setLigne(int ligne) {
        this.ligne = ligne;
    }

    public int getLigne() {
        return ligne;
    }

    public void addError(String attribute,String message){
        errors.put(attribute, message);
    }

    public boolean isEmpty() {
        return errors.isEmpty();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Ligne ").append(ligne).append(": ");

        for (Map.Entry<String, String> entry : errors.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("; ");
        }

        if (sb.length() >= 2 && sb.substring(sb.length() - 2).equals("; ")) {
            sb.setLength(sb.length() - 2);
        }

        return sb.toString();
    }
}