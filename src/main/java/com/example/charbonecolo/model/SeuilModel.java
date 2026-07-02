package com.example.charbonecolo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "seuil")
public class SeuilModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "id_produit")
   private ProduitModel produit;

   @Column(name = "valeur", nullable = false)
   private double valeur;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "id_alerte_seuil")
   private AlerteSeuilModel alerteSeuil;

   public Integer getId() {
       return id;
   }

    public void setId(Integer id) {
         this.id = id;
    }

    public ProduitModel getProduit() {
        return produit;
    }

    public void setProduit(ProduitModel produit) {
        this.produit = produit;
    }

    public double getValeur() {
        return valeur;
    }

    public void setValeur(double valeur) {
        this.valeur = valeur;
    }

    public AlerteSeuilModel getAlerteSeuil() {
        return alerteSeuil;
    }

    public void setAlerteSeuil(AlerteSeuilModel alerteSeuil) {
        this.alerteSeuil = alerteSeuil;
    }

    
}