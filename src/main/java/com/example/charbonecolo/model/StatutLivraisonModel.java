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
@Table(name = "statuts_livraisons")
public class StatutLivraisonModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_livraison", referencedColumnName = "id", nullable = false)
    private LivraisonModel livraison;

    @ManyToOne
    @JoinColumn(name = "id_livraisons_statuts", referencedColumnName = "id", nullable = false)
    private LivraisonStatutModel statut;

    @Column(name = "date_statuts_livraison", nullable = false, insertable = false, updatable = false)
    private LocalDateTime dateStatutsLivraison;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LivraisonModel getLivraison() { return livraison; }
    public void setLivraison(LivraisonModel livraison) { this.livraison = livraison; }

    public LivraisonStatutModel getStatut() { return statut; }
    public void setStatut(LivraisonStatutModel statut) { this.statut = statut; }

    public LocalDateTime getDateStatutsLivraison() { return dateStatutsLivraison; }
    public void setDateStatutsLivraison(LocalDateTime dateStatutsLivraison) { this.dateStatutsLivraison = dateStatutsLivraison; }
}
