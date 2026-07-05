package com.example.charbonecolo.dto;

import java.time.LocalDateTime;

public class EntreeDto {

    private Integer id;
    private LocalDateTime dateMouvement;
    private String lotReference;
    private String matiereLibelle;
    private Integer quantite;
    private String fournisseurNom;

    public EntreeDto(Integer id, LocalDateTime dateMouvement, String lotReference,
                      String matiereLibelle, Integer quantite, String fournisseurNom) {
        this.id = id;
        this.dateMouvement = dateMouvement;
        this.lotReference = lotReference;
        this.matiereLibelle = matiereLibelle;
        this.quantite = quantite;
        this.fournisseurNom = fournisseurNom;
        
    }

    public EntreeDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getDateMouvement() { return dateMouvement; }
    public void setDateMouvement(LocalDateTime dateMouvement) { this.dateMouvement = dateMouvement; }

    public String getLotReference() { return lotReference; }
    public void setLotReference(String lotReference) { this.lotReference = lotReference; }

    public String getMatiereLibelle() { return matiereLibelle; }
    public void setMatiereLibelle(String matiereLibelle) { this.matiereLibelle = matiereLibelle; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public String getFournisseurNom() { return fournisseurNom; }
    public void setFournisseurNom(String fournisseurNom) { this.fournisseurNom = fournisseurNom; }
}