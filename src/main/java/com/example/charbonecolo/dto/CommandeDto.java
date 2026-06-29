package com.example.charbonecolo.dto;

import java.time.LocalDateTime;

public class CommandeDto {
    private Integer id;
    private String reference;
    private LocalDateTime dateCommande;
    private String clientNom;
    private Double montant;
    private Integer idCommandeStatuts;
    private String statutLibelle;

    // 1. Le constructeur EXACT pour le @SqlResultSetMapping
    public CommandeDto(Integer id, String reference, LocalDateTime dateCommande, 
                       String clientNom, Double montant, Integer idCommandeStatuts, String statutLibelle) {
        this.id = id;
        this.reference = reference;
        this.dateCommande = dateCommande;
        this.clientNom = clientNom;
        this.montant = montant;
        this.idCommandeStatuts = idCommandeStatuts;
        this.statutLibelle = statutLibelle;
    }

    // 2. Constructeur par défaut (recommandé)
    public CommandeDto() {
    }

    // 3. Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDateTime getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDateTime dateCommande) {
        this.dateCommande = dateCommande;
    }

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public Integer getIdCommandeStatuts() {
        return idCommandeStatuts;
    }

    public void setIdCommandeStatuts(Integer idCommandeStatuts) {
        this.idCommandeStatuts = idCommandeStatuts;
    }

    public String getStatutLibelle() {
        return statutLibelle;
    }

    public void setStatutLibelle(String statutLibelle) {
        this.statutLibelle = statutLibelle;
    }
}