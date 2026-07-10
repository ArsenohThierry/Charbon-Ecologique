package com.example.charbonecolo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employe")
public class EmployeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(name = "date_embauche", nullable = false)
    private LocalDate dateEmbauche;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_emploi", nullable = false)
    private EmploiModel emploi;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prime = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal indemnite = BigDecimal.ZERO;

    public EmployeModel() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public LocalDate getDateEmbauche() { return dateEmbauche; }
    public void setDateEmbauche(LocalDate dateEmbauche) { this.dateEmbauche = dateEmbauche; }
    public EmploiModel getEmploi() { return emploi; }
    public void setEmploi(EmploiModel emploi) { this.emploi = emploi; }
    public BigDecimal getPrime() { return prime; }
    public void setPrime(BigDecimal prime) { this.prime = prime; }
    public BigDecimal getIndemnite() { return indemnite; }
    public void setIndemnite(BigDecimal indemnite) { this.indemnite = indemnite; }

    public BigDecimal getSalaireBase() {
        return emploi != null ? emploi.getSalaire() : BigDecimal.ZERO;
    }

    public BigDecimal getTotalSalaire() {
        return getSalaireBase().add(prime).add(indemnite);
    }
}
