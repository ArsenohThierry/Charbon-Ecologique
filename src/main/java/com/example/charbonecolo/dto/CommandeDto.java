package com.example.charbonecolo.dto;

import java.util.List;

import com.example.charbonecolo.model.CommandeModel;
import com.example.charbonecolo.model.DetailCommandeModel;
import com.example.charbonecolo.model.StatutCommandeModel;

public class CommandeDto {
    private CommandeModel commande;
    private List<DetailCommandeModel> details;
    private Double montant;
    private StatutCommandeModel currentStatut;
    public CommandeModel getCommande() {
        return commande;
    }
    public void setCommande(CommandeModel commande) {
        this.commande = commande;
    }
    public List<DetailCommandeModel> getDetails() {
        return details;
    }
    public void setDetails(List<DetailCommandeModel> details) {
        this.details = details;
    }
    public Double getMontant() {
        return montant;
    }
    public void setMontant(Double montant) {
        this.montant = montant;
    }
    public StatutCommandeModel getCurrentStatut() {
        return currentStatut;
    }
    public void setCurrentStatut(StatutCommandeModel currentStatut) {
        this.currentStatut = currentStatut;
    }
}
