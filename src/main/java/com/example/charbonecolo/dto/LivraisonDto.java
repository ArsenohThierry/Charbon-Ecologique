package com.example.charbonecolo.dto;

import java.time.LocalDateTime;

public class LivraisonDto {

    private Integer id;
    private String reference;
    private LocalDateTime dateLivraison;
    private String lieu;
    private String livreurNom;
    private String statutLibelle;
    private String commandeReference;
    private Integer commandeId;

    public LivraisonDto(Integer id, String reference, LocalDateTime dateLivraison,
                        String lieu, String livreurNom, String statutLibelle,
                        String commandeReference, Integer commandeId) {
        this.id = id;
        this.reference = reference;
        this.dateLivraison = dateLivraison;
        this.lieu = lieu;
        this.livreurNom = livreurNom;
        this.statutLibelle = statutLibelle;
        this.commandeReference = commandeReference;
        this.commandeId = commandeId;
    }

    public LivraisonDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public LocalDateTime getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(LocalDateTime dateLivraison) { this.dateLivraison = dateLivraison; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getLivreurNom() { return livreurNom; }
    public void setLivreurNom(String livreurNom) { this.livreurNom = livreurNom; }

    public String getStatutLibelle() { return statutLibelle; }
    public void setStatutLibelle(String statutLibelle) { this.statutLibelle = statutLibelle; }

     public String getCommandeReference() { return commandeReference; }
    public void setCommandeReference(String commandeReference) { this.commandeReference = commandeReference; }

    public Integer getCommandeId() { return commandeId; }
    public void setCommandeId(Integer commandeId) { this.commandeId = commandeId; }
}