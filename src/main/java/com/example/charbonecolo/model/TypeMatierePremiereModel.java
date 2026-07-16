package com.example.charbonecolo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

    import jakarta.persistence.Column;
    import jakarta.persistence.Entity;
    import jakarta.persistence.FetchType;
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.GenerationType;
    import jakarta.persistence.Id;
    import jakarta.persistence.JoinColumn;
    import jakarta.persistence.ManyToOne;
    import jakarta.persistence.Table;

    import java.math.BigDecimal;
    import java.time.LocalDateTime;

    @Entity
    @Table(name = "type_matiere_premiere")
    public class TypeMatierePremiereModel {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @Column(name = "reference", length = 50, nullable = false, unique = true)
        private String reference;

        @Column(name = "libelle", length = 150, nullable = false)
        private String libelle;

    @Column(name = "prix_unitaire", precision = 10, scale = 2, nullable = false)
    private BigDecimal prixUnitaire;

    @Column(name = "rendement", precision = 5, scale = 2, nullable = false)
    private BigDecimal rendement;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "id_fournisseur", nullable = false)
        private FournisseurModel fournisseur;

    @Column(name = "date_ajout", nullable = false, updatable = false)
    private LocalDateTime dateAjout;

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    // ---- Constructeurs ----
    public TypeMatierePremiereModel() {
    }

    public TypeMatierePremiereModel(String reference, String libelle, BigDecimal prixUnitaire, FournisseurModel fournisseur) {
        this.reference = reference;
        this.libelle = libelle;
        this.prixUnitaire = prixUnitaire;
        this.fournisseur = fournisseur;
    }

        // ---- Getters / Setters ----
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }

        public String getLibelle() { return libelle; }
        public void setLibelle(String libelle) { this.libelle = libelle; }

        public BigDecimal getPrixUnitaire() { return prixUnitaire; }
        public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }

        public FournisseurModel getFournisseur() { return fournisseur; }
        public void setFournisseur(FournisseurModel fournisseur) { this.fournisseur = fournisseur; }

        public LocalDateTime getDateAjout() { return dateAjout; }
        public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }

        public Boolean getActif() { return actif; }
        public void setActif(Boolean actif) { this.actif = actif; }

        public BigDecimal getRendement() { return rendement; }
        public void setRendement(BigDecimal rendement) { this.rendement = rendement; }
    }
