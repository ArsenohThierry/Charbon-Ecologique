package com.example.charbonecolo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "livraison_reste")
public class LivraisonResteModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "id_livraison", referencedColumnName = "id")
    LivraisonModel livraison;

    @ManyToOne
    @JoinColumn(name = "id_produit", referencedColumnName = "id")
    ProduitModel produit;

    @Column(name = "reste")
    Integer reste;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LivraisonModel getLivraison() {
        return livraison;
    }

    public void setLivraison(LivraisonModel livraison) {
        this.livraison = livraison;
    }

    public ProduitModel getProduit() {
        return produit;
    }

    public void setProduit(ProduitModel produit) {
        this.produit = produit;
    }

    public Integer getReste() {
        return reste;
    }

    public void setReste(Integer reste) {
        this.reste = reste;
    }
}
