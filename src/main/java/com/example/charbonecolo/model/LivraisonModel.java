package com.example.charbonecolo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "livraison")
public class LivraisonModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // @Column(name = "reference", nullable = false, unique = true)
    @Transient
    private String reference;

    // @Column(name = "date_livraison", nullable = false)
    @Transient
    private LocalDate dateLivraison;

    // @Column(name = "adresse_livraison", nullable = false)
    @Transient
    private String adresseLivraison;

    // @Column(name = "statut", nullable = false)
    @Transient
    private String statut;

    // @Column(name = "id_commande")
    @Transient
    private Long idCommande;

    // @Column(name = "date_creation")
    @Transient
    private LocalDateTime dateCreation;

    // @Column(name = "actif")
    @Transient
    private Boolean actif;

    // -------- Getters & Setters --------

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public LocalDate getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(LocalDate dateLivraison) { this.dateLivraison = dateLivraison; }

    public String getAdresseLivraison() { return adresseLivraison; }
    public void setAdresseLivraison(String adresseLivraison) { this.adresseLivraison = adresseLivraison; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Long getIdCommande() { return idCommande; }
    public void setIdCommande(Long idCommande) { this.idCommande = idCommande; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
}