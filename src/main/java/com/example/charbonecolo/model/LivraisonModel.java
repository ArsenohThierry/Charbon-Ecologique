package com.example.charbonecolo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "livraison")
public class LivraisonModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reference", nullable = false, unique = true)
    private String reference;

    @Column(name = "date_livraison", nullable = false)
    private LocalDateTime dateLivraison;

    @Column(name = "date_reportage_livraison")
    private LocalDateTime dateReportageLivraison;

    @Column(name = "date_livraison_reel")
    private LocalDateTime dateLivraisonReel;

    @Column(name = "lieu")
    private String lieu;

    @ManyToOne
    @JoinColumn(name = "id_livreur", referencedColumnName = "id")
    private LivreurModel livreur;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public LocalDateTime getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(LocalDateTime dateLivraison) { this.dateLivraison = dateLivraison; }

    public LocalDateTime getDateReportageLivraison() { return dateReportageLivraison; }
    public void setDateReportageLivraison(LocalDateTime dateReportageLivraison) { this.dateReportageLivraison = dateReportageLivraison; }

    public LocalDateTime getDateLivraisonReel() { return dateLivraisonReel; }
    public void setDateLivraisonReel(LocalDateTime dateLivraisonReel) { this.dateLivraisonReel = dateLivraisonReel; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public LivreurModel getLivreur() { return livreur; }
    public void setLivreur(LivreurModel livreur) { this.livreur = livreur; }
}
