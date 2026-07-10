package com.example.charbonecolo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "salaire_historique")
public class SalaireHistoriqueModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe", nullable = false)
    private EmployeModel employe;

    @Column(name = "salaire_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal salaireBase;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prime;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal indemnite;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "date_effet", nullable = false)
    private LocalDate dateEffet;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    public SalaireHistoriqueModel() {}

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public EmployeModel getEmploye() { return employe; }
    public void setEmploye(EmployeModel employe) { this.employe = employe; }
    public BigDecimal getSalaireBase() { return salaireBase; }
    public void setSalaireBase(BigDecimal salaireBase) { this.salaireBase = salaireBase; }
    public BigDecimal getPrime() { return prime; }
    public void setPrime(BigDecimal prime) { this.prime = prime; }
    public BigDecimal getIndemnite() { return indemnite; }
    public void setIndemnite(BigDecimal indemnite) { this.indemnite = indemnite; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public LocalDate getDateEffet() { return dateEffet; }
    public void setDateEffet(LocalDate dateEffet) { this.dateEffet = dateEffet; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
