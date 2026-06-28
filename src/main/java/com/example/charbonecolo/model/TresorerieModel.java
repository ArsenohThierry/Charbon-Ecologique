package com.example.charbonecolo.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "tresorerie")
public class TresorerieModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date_operation", nullable = false)
    private LocalDateTime dateOperation;

    @Column(name = "type_operation", nullable = false, length = 10)
    private String typeOperation; // ENTREE / SORTIE

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(length = 50)
    private String origine;

    @Column(name = "reference_origine", length = 50)
    private String referenceOrigine;

    @Column(columnDefinition = "TEXT")
    private String description;

    public TresorerieModel() {}

    public TresorerieModel(Integer id, LocalDateTime dateOperation, String typeOperation, BigDecimal montant, String origine, String referenceOrigine, String description) {
        this.id = id;
        this.dateOperation = dateOperation;
        this.typeOperation = typeOperation;
        this.montant = montant;
        this.origine = origine;
        this.referenceOrigine = referenceOrigine;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(LocalDateTime dateOperation) {
        this.dateOperation = dateOperation;
    }

    public String getTypeOperation() {
        return typeOperation;
    }

    public void setTypeOperation(String typeOperation) {
        this.typeOperation = typeOperation;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getOrigine() {
        return origine;
    }

    public void setOrigine(String origine) {
        this.origine = origine;
    }

    public String getReferenceOrigine() {
        return referenceOrigine;
    }

    public void setReferenceOrigine(String referenceOrigine) {
        this.referenceOrigine = referenceOrigine;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}