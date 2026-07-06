package com.example.charbonecolo.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class MouvementEditDTO {
    @NotNull
    private Integer id;
    private Integer idLot;

    @NotNull(message = "La quantité est obligatoire.")
    @Positive(message = "La quantité doit être supérieure à zéro.")
    private Integer quantite;

    private Integer idMotif;
    private LocalDate date;

    // getters/setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getIdLot() { return idLot; }
    public void setIdLot(Integer idLot) { this.idLot = idLot; }
    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }
    public Integer getIdMotif() { return idMotif; }
    public void setIdMotif(Integer idMotif) { this.idMotif = idMotif; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}   