package com.example.charbonecolo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "emploi")
public class EmploiModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String libelle;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salaire;

    public EmploiModel() {}

    public EmploiModel(String libelle, BigDecimal salaire) {
        this.libelle = libelle;
        this.salaire = salaire;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public BigDecimal getSalaire() { return salaire; }
    public void setSalaire(BigDecimal salaire) { this.salaire = salaire; }
}
