package com.example.charbonecolo.dto;

public class DetailErrorWrapper {
    private Integer id;
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    private String produitError;
    private String quantiteError;
    public String getProduitError() {
        return produitError;
    }
    public void setProduitError(String produitError) {
        this.produitError = produitError;
    }
    public String getQuantiteError() {
        return quantiteError;
    }
    public void setQuantiteError(String quantiteError) {
        this.quantiteError = quantiteError;
    }
}
