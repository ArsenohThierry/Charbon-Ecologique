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
@Table(name = "facture_detail")
public class FactureDetailModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_facture", referencedColumnName = "id")
    private FactureModel facture;

    @Column(name = "montant")
    private Integer montant;

    @Column(name = "libelle")
    private String libelle;

    @Column(name = "pu")
    private Integer pu;

    @Column(name = "quantite")
    private Integer quantite;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public FactureModel getFacture() {
        return facture;
    }

    public void setFacture(FactureModel facture) {
        this.facture = facture;
    }

    public Integer getMontant() {
        return montant;
    }

    public void setMontant(Integer montant) {
        this.montant = montant;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Integer getPu() {
        return pu;
    }

    public void setPu(Integer pu) {
        this.pu = pu;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }
}
