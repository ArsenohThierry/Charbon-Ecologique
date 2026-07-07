package com.example.charbonecolo.dto;

public class FactureErrorWrapper {

    private String fraisLivraisonError;
    private String methodePaiementError;

    public String getFraisLivraisonError() { return fraisLivraisonError; }
    public void setFraisLivraisonError(String fraisLivraisonError) { this.fraisLivraisonError = fraisLivraisonError; }

    public String getMethodePaiementError() { return methodePaiementError; }
    public void setMethodePaiementError(String methodePaiementError) { this.methodePaiementError = methodePaiementError; }

    public boolean hasError() {
        return fraisLivraisonError != null || methodePaiementError != null;
    }
}
