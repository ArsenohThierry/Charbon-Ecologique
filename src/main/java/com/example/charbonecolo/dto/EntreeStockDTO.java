package com.example.charbonecolo.dto;

import java.time.LocalDate;

public class EntreeStockDTO {
    private Integer id;
    private Integer idLot;
    private LocalDate dateEntree;
    private Integer quantite;

    // 1. Le constructeur EXACT pour le @SqlResultSetMapping (without id)
    public EntreeStockDTO(Integer idLot, LocalDate dateEntree, Integer quantite) {
        this.idLot = idLot;
        this.dateEntree = dateEntree;
        this.quantite = quantite;
    }

    // Constructor for update (with id)
    public EntreeStockDTO(Integer id, Integer idLot, LocalDate dateEntree, Integer quantite) {
        this.id = id;
        this.idLot = idLot;
        this.dateEntree = dateEntree;
        this.quantite = quantite;
    }

    // 2. Constructeur par défaut (recommandé)
    public EntreeStockDTO() {
    }

    // 3. Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdLot() {
        return idLot;
    }

    public void setIdLot(Integer idLot) {
        this.idLot = idLot;
    }

    public LocalDate getDateEntree() {
        return dateEntree;
    }

    public void setDateEntree(LocalDate dateEntree) {
        this.dateEntree = dateEntree;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }
}