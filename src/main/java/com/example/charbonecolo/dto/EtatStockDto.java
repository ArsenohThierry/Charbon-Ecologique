package com.example.charbonecolo.dto;

public class EtatStockDto {

    private Integer lotId;
    private String produitNom;
    private String reference;
    private Integer totalEntree;
    private Integer totalSortie;
    private Integer restant;

    public EtatStockDto(Integer lotId, String produitNom, String reference,
                         Integer totalEntree, Integer totalSortie, Integer restant) {
        this.lotId = lotId;
        this.produitNom = produitNom;
        this.reference = reference;
        this.totalEntree = totalEntree;
        this.totalSortie = totalSortie;
        this.restant = restant;
    }

    public EtatStockDto() {}

    public Integer getLotId() { return lotId; }
    public void setLotId(Integer lotId) { this.lotId = lotId; }

    public String getProduitNom() { return produitNom; }
    public void setProduitNom(String produitNom) { this.produitNom = produitNom; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Integer getTotalEntree() { return totalEntree; }
    public void setTotalEntree(Integer totalEntree) { this.totalEntree = totalEntree; }

    public Integer getTotalSortie() { return totalSortie; }
    public void setTotalSortie(Integer totalSortie) { this.totalSortie = totalSortie; }

    public Integer getRestant() { return restant; }
    public void setRestant(Integer restant) { this.restant = restant; }
}