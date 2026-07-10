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
@Table(name = "facture")
public class FactureModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reference")
    private String reference;

    @ManyToOne
    @JoinColumn(name = "id_paiement", referencedColumnName = "id")
    private PaiementModel paiement;

    public Integer getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public PaiementModel getPaiement() {
        return paiement;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setPaiement(PaiementModel paiement) {
        this.paiement = paiement;
    }
}
