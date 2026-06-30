package com.example.charbonecolo.model;

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

@Entity
@Table(name = "lot_production")
public class LotProductionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reference", nullable = false, unique = true, length = 50)
    private String reference;

    // TODO: Replace with proper relationship when TypeMatierePremiereModel is ready
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_type_matiere_premiere", nullable = false)
    private TypeMatierePremiereModel typeMatierePremiere;
    // @Column(name = "id_type_matiere_premiere", nullable = false)
    // private Integer idTypeMatierePremiere;

    // TODO: Replace with proper relationship when ProduitModel is ready
        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "id_produit", nullable = false)
        private ProduitModel produit;
    // @Column(name = "id_produit", nullable = false)
    // private Integer idProduit;

    @Column(name = "quantite_matiere_utilisee", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantiteMatiereUtilisee;

    @Column(name = "quantite_produit_prevue", nullable = false)
    private Integer quantiteProduitPrevues;

    @Column(name = "quantite_produit_reelle")
    private Integer quantiteProduitReelle;

    @Column(name = "date_fin_reelle")
    private LocalDateTime dateFinReelle;

    @Column(name = "remarques")
    private String remarques;

    @Column(name = "date_entree_lot", nullable = false)
    private LocalDateTime dateEntreeLot;

    // Getters and Setters
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

    public TypeMatierePremiereModel getTypeMatierePremiere() {
        return typeMatierePremiere;
    }

    public void setTypeMatierePremiere(TypeMatierePremiereModel typeMatierePremiere) {
        this.typeMatierePremiere = typeMatierePremiere;
    }

    public ProduitModel getProduit() {
        return produit;
    }

    public void setProduit(ProduitModel produit) {
        this.produit = produit;
    }

    public BigDecimal getQuantiteMatiereUtilisee() {
        return quantiteMatiereUtilisee;
    }

    public void setQuantiteMatiereUtilisee(BigDecimal quantiteMatiereUtilisee) {
        this.quantiteMatiereUtilisee = quantiteMatiereUtilisee;
    }

    public Integer getQuantiteProduitPrevues() {
        return quantiteProduitPrevues;
    }

    public void setQuantiteProduitPrevues(Integer quantiteProduitPrevues) {
        this.quantiteProduitPrevues = quantiteProduitPrevues;
    }

    public Integer getQuantiteProduitReelle() {
        return quantiteProduitReelle;
    }

    public void setQuantiteProduitReelle(Integer quantiteProduitReelle) {
        this.quantiteProduitReelle = quantiteProduitReelle;
    }

    public LocalDateTime getDateFinReelle() {
        return dateFinReelle;
    }

    public void setDateFinReelle(LocalDateTime dateFinReelle) {
        this.dateFinReelle = dateFinReelle;
    }

    public String getRemarques() {
        return remarques;
    }

    public void setRemarques(String remarques) {
        this.remarques = remarques;
    }

    public LocalDateTime getDateEntreeLot() {
        return dateEntreeLot;
    }

    public void setDateEntreeLot(LocalDateTime dateEntreeLot) {
        this.dateEntreeLot = dateEntreeLot;
    }
}