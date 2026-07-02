package com.example.charbonecolo.dto;

public class EntreeStockDTO {
    private Integer id;
    private String reference;
    private String dateEntree;
    private String fournisseurNom;
    private Double montant;
    private Integer idEntreeStockStatuts;
    private String statutLibelle;

    // 1. Le constructeur EXACT pour le @SqlResultSetMapping
    public EntreeStockDTO(Integer id, String reference, String dateEntree, 
                       String fournisseurNom, Double montant, Integer idEntreeStockStatuts, String statutLibelle) {
        this.id = id;
        this.reference = reference;
        this.dateEntree = dateEntree;
        this.fournisseurNom = fournisseurNom;
        this.montant = montant;
        this.idEntreeStockStatuts = idEntreeStockStatuts;
        this.statutLibelle = statutLibelle;
    }

    // 2. Constructeur par défaut (recommandé)
    public EntreeStockDTO() {
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
}
