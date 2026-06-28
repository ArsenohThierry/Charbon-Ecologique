package com.example.charbonecolo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lot_production")
public class LotProductionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @ManyToOne
    @JoinColumn(name = "id_type_matiere_premiere", nullable = false)
    private TypeMatierePremiereModel typeMatierePremiere;

    @ManyToOne
    @JoinColumn(name = "id_produit", nullable = false)
    private ProduitModel produit;

    @Column(name = "quantite_matiere_utilisee", nullable = false)
    private Double quantiteMatiereUtilisee;

    @Column(name = "quantite_produit_prevue", nullable = false)
    private Double quantiteProduitPrevue;

    @Column(name = "quantite_produit_reelle")
    private Double quantiteProduitReelle;

    @Column(name = "date_fin_reelle")
    private LocalDateTime dateFinReelle;

    @Column(columnDefinition = "TEXT")
    private String remarques;

    @Column(name = "date_entree_lot", nullable = false)
    private LocalDateTime dateEntreeLot;


    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public TypeMatierePremiereModel getTypeMatierePremiere() { return typeMatierePremiere; }
    public void setTypeMatierePremiere(TypeMatierePremiereModel typeMatierePremiere) {
        this.typeMatierePremiere = typeMatierePremiere;
    }

    public ProduitModel getProduit() { return produit; }
    public void setProduit(ProduitModel produit) { this.produit = produit; }

    public Double getQuantiteMatiereUtilisee() { return quantiteMatiereUtilisee; }
    public void setQuantiteMatiereUtilisee(Double quantiteMatiereUtilisee) {
        this.quantiteMatiereUtilisee = quantiteMatiereUtilisee;
    }

    public Double getQuantiteProduitPrevue() { return quantiteProduitPrevue; }
    public void setQuantiteProduitPrevue(Double quantiteProduitPrevue) {
        this.quantiteProduitPrevue = quantiteProduitPrevue;
    }

    public Double getQuantiteProduitReelle() { return quantiteProduitReelle; }
    public void setQuantiteProduitReelle(Double quantiteProduitReelle) {
        this.quantiteProduitReelle = quantiteProduitReelle;
    }

    public LocalDateTime getDateFinReelle() { return dateFinReelle; }
    public void setDateFinReelle(LocalDateTime dateFinReelle) { this.dateFinReelle = dateFinReelle; }

    public String getRemarques() { return remarques; }
    public void setRemarques(String remarques) { this.remarques = remarques; }

    public LocalDateTime getDateEntreeLot() { return dateEntreeLot; }
    public void setDateEntreeLot(LocalDateTime dateEntreeLot) { this.dateEntreeLot = dateEntreeLot; }
}