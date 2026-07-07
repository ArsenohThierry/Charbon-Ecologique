package com.example.charbonecolo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "methode_paiement")
public class MethodePaiementModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}