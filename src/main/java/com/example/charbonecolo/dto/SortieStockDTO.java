package com.example.charbonecolo.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SortieStockDTO {
    private Integer id;

    @NotNull(message = "Veuillez sélectionner un produit.")
    private Integer idProduit;

    @NotNull(message = "La quantité est obligatoire.")
    @Positive(message = "La quantité doit être supérieure à zéro.")
    private Integer quantite;

    @NotNull(message = "Veuillez sélectionner un motif.")
    private Integer idMotif;

    private LocalDate dateSortie;

    // Constructor for potential SQL mapping (if needed)
    public SortieStockDTO(Integer idProduit, Integer quantite, Integer idMotif, LocalDate dateSortie) {
        this.idProduit = idProduit;
        this.quantite = quantite;
        this.idMotif = idMotif;
        this.dateSortie = dateSortie;
    }

    // Default constructor
    public SortieStockDTO() {
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(Integer idProduit) {
        this.idProduit = idProduit;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public Integer getIdMotif() {
        return idMotif;
    }

    public void setIdMotif(Integer idMotif) {
        this.idMotif = idMotif;
    }

    public LocalDate getDateSortie() {
        return dateSortie;
    }

    public void setDateSortie(LocalDate dateSortie) {
        this.dateSortie = dateSortie;
    }
}