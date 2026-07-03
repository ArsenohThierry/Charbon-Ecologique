package com.example.charbonecolo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mouvement_sortie_detail")
public class MouvementSortieDetailModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_mouvement_sortie", nullable = false)
    private MouvementStockModel mouvementSortie;

    @ManyToOne
    @JoinColumn(name = "id_lot_production", nullable = false)
    private LotProductionModel lotProduction;

    @Column(nullable = false)
    private Integer quantite;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public MouvementStockModel getMouvementSortie() {
        return mouvementSortie;
    }

    public void setMouvementSortie(MouvementStockModel mouvementSortie) {
        this.mouvementSortie = mouvementSortie;
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
}
