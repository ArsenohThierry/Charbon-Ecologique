package com.example.charbonecolo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "mouvement_stock")
@SQLDelete(sql = "UPDATE mouvement_stock SET date_suppression = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("date_suppression IS NULL")
public class MouvementStockModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_lot_production")
    private LotProductionModel lotProduction; // NULL si c'est une sortie

    @Column(nullable = false)
    private Integer quantite;

    @Column(name = "date_mouvement", nullable = false)
    private LocalDateTime dateMouvement;

    @ManyToOne
    @JoinColumn(name = "id_type_mouvement", nullable = false)
    private TypeMouvementStockModel typeMouvement; // Entree ou Sortie

    @ManyToOne
    @JoinColumn(name = "id_motif_sortie")
    private MotifSortieModel motifSortie; // commande, suppression, perte, etc. (requis si sortie)

    @Column(name = "date_suppression")
    private LocalDateTime dateSuppression;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LotProductionModel getLotProduction() {
        return lotProduction;
    }

    public void setLotProduction(LotProductionModel lotProduction) {
        this.lotProduction = lotProduction;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public LocalDateTime getDateMouvement() {
        return dateMouvement;
    }

    public void setDateMouvement(LocalDateTime dateMouvement) {
        this.dateMouvement = dateMouvement;
    }

    public TypeMouvementStockModel getTypeMouvement() {
        return typeMouvement;
    }

    public void setTypeMouvement(TypeMouvementStockModel typeMouvement) {
        this.typeMouvement = typeMouvement;
    }

    public MotifSortieModel getMotifSortie() {
        return motifSortie;
    }

    public void setMotifSortie(MotifSortieModel motifSortie) {
        this.motifSortie = motifSortie;
    }

    public LocalDateTime getDateSuppression() { return dateSuppression; }
    public void setDateSuppression(LocalDateTime dateSuppression) { this.dateSuppression = dateSuppression; }
}