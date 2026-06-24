package com.example.charbonecolo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "types_matieres_premieres")
public class TypeMatierePremiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String libelle;

    public TypeMatierePremiere() {}

    public TypeMatierePremiere(String libelle) {
        this.libelle = libelle;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}
