package com.example.charbonecolo.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "statuts_commandes")
public class StatutCommandeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_commandes", referencedColumnName = "id", nullable = false)
    private CommandeModel commande;

    @ManyToOne
    @JoinColumn(name = "id_commande_statuts", referencedColumnName = "id", nullable = false)
    private CommandeStatutModel statut;

    @Column(name = "date_statut_commande", nullable = false, insertable = false, updatable = false)
    private LocalDateTime dateStatutCommande;

    // Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CommandeModel getCommande() {
        return commande;
    }

    public void setCommande(CommandeModel commande) {
        this.commande = commande;
    }

    public CommandeStatutModel getStatut() {
        return statut;
    }

    public void setStatut(CommandeStatutModel statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateStatutCommande() {
        return dateStatutCommande;
    }

    public void setDateStatutCommande(LocalDateTime dateStatutCommande) {
        this.dateStatutCommande = dateStatutCommande;
    }
}