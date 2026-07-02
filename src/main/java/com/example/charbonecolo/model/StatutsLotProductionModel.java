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
@Table(name = "statuts_lot_production")
public class StatutsLotProductionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_lot_production", nullable = false)
    private LotProductionModel lotProduction;

    @ManyToOne
    @JoinColumn(name = "id_lot_statuts", nullable = false)
    private LotStatutsModel lotStatuts;

    @Column(name = "date_statut", nullable = false)
    private LocalDateTime dateStatut;

    // getters/setters

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

    public LotStatutsModel getLotStatuts() {
        return lotStatuts;
    }

    public void setLotStatuts(LotStatutsModel lotStatuts) {
        this.lotStatuts = lotStatuts;
    }

    public LocalDateTime getDateStatut() {
        return dateStatut;
    }

    public void setDateStatut(LocalDateTime dateStatut) {
        this.dateStatut = dateStatut;
    }

    
}