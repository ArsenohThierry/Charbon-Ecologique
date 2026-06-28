package com.example.charbonecolo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "produit")
public class ProduitModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nom;

    @Column(columnDefinition = "NUMERIC")
    private Double pu;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Column(columnDefinition = "NUMERIC")
    public Double getPu() {
        return pu;
    }

    @Column(columnDefinition = "NUMERIC")
    public void setPu(Double pu) {
        this.pu = pu;
    }

}
