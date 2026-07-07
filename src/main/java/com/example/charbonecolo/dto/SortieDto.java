package com.example.charbonecolo.dto;

import java.time.LocalDateTime;

public class SortieDto {

    private Integer id;
    private String produitNom;
    private Integer quantite;
    private String motifLibelle;
    private LocalDateTime dateMouvement;
    private String lotsConsommes;

    // Constructeur EXACT pour le mapping depuis Object[]
    public SortieDto(Integer id, String produitNom, Integer quantite,
                      String motifLibelle, LocalDateTime dateMouvement, String lotsConsommes) {
        this.id = id;
        this.produitNom = produitNom;
        this.quantite = quantite;
        this.motifLibelle = motifLibelle;
        this.dateMouvement = dateMouvement;
        this.lotsConsommes = lotsConsommes;
    }

    public SortieDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getProduitNom() { return produitNom; }
    public void setProduitNom(String produitNom) { this.produitNom = produitNom; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public String getMotifLibelle() { return motifLibelle; }
    public void setMotifLibelle(String motifLibelle) { this.motifLibelle = motifLibelle; }

    public LocalDateTime getDateMouvement() { return dateMouvement; }
    public void setDateMouvement(LocalDateTime dateMouvement) { this.dateMouvement = dateMouvement; }

    public String getLotsConsommes() { return lotsConsommes; }
    public void setLotsConsommes(String lotsConsommes) { this.lotsConsommes = lotsConsommes; }
}