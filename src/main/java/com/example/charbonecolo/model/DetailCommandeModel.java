package com.example.charbonecolo.model;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "detail_commande")
public class DetailCommandeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_commande", referencedColumnName = "id", nullable = false)
    private CommandeModel commande;

    @ManyToOne
    @JoinColumn(name = "id_produit", referencedColumnName = "id", nullable = false)
    private ProduitModel produit;

    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    @Column(name = "montant", nullable = false)
    private BigDecimal montant;

    // Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CommandeModel getCommande() {
        return commande;
    }

    public void setCommande(CommandeModel commande) {
        this.commande = commande;
    }

    public ProduitModel getProduit() {
        return produit;
    }

    public void setProduit(ProduitModel produit) {
        this.produit = produit;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public Double findMontant() {
        return produit.getPu().doubleValue() * quantite;
    }
}