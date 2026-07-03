package com.example.charbonecolo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "livraison_commandes")
public class LivraisonCommandeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_livraison", nullable = false)
    private Integer idLivraison;

    @Column(name = "id_commande", nullable = false)
    private Integer idCommande;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getIdLivraison() { return idLivraison; }
    public void setIdLivraison(Integer idLivraison) { this.idLivraison = idLivraison; }

    public Integer getIdCommande() { return idCommande; }
    public void setIdCommande(Integer idCommande) { this.idCommande = idCommande; }
}
