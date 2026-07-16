package com.example.charbonecolo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "produit")
@SQLDelete(sql = "UPDATE produit SET date_suppression = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("date_suppression IS NULL")
public class ProduitModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nom;

    @Column(columnDefinition = "NUMERIC")
    private Double pu;

    @Column(name = "date_suppression")
    private LocalDateTime dateSuppression;

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

    public LocalDateTime getDateSuppression() { return dateSuppression; }
    public void setDateSuppression(LocalDateTime dateSuppression) { this.dateSuppression = dateSuppression; }

}
