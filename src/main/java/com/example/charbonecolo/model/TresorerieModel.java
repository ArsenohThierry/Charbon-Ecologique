package com.example.charbonecolo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tresorerie")
public class TresorerieModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_mouvement", nullable = false)
    private LocalDateTime dateMouvement;

    @Column(nullable = false, length = 10)
    private String type; // ENTREE ou SORTIE

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(precision = 15, scale = 2)
    private BigDecimal solde;

    @Column(length = 100)
    private String libelle;

    @Column(name = "journal_id")
    private Long journalId;

    public TresorerieModel() {}

    public TresorerieModel(LocalDateTime dateMouvement, String type, BigDecimal montant,
                            BigDecimal solde, String libelle, Long journalId) {
        this.dateMouvement = dateMouvement;
        this.type = type;
        this.montant = montant;
        this.solde = solde;
        this.libelle = libelle;
        this.journalId = journalId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDateMouvement() { return dateMouvement; }
    public void setDateMouvement(LocalDateTime dateMouvement) { this.dateMouvement = dateMouvement; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public BigDecimal getSolde() { return solde; }
    public void setSolde(BigDecimal solde) { this.solde = solde; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public Long getJournalId() { return journalId; }
    public void setJournalId(Long journalId) { this.journalId = journalId; }
}
