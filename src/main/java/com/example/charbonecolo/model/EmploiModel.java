package com.example.charbonecolo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "emploi")
@SQLDelete(sql = "UPDATE emploi SET date_suppression = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("date_suppression IS NULL")
public class EmploiModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String libelle;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salaire;

    @Column(name = "date_suppression")
    private LocalDateTime dateSuppression;

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

    public LocalDateTime getDateSuppression() { return dateSuppression; }
    public void setDateSuppression(LocalDateTime dateSuppression) { this.dateSuppression = dateSuppression; }
}
