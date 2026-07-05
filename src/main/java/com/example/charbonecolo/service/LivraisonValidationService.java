package com.example.charbonecolo.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.charbonecolo.dto.LivraisonErrorWrapper;
import com.example.charbonecolo.model.LivraisonModel;

@Service
public class LivraisonValidationService {

    public LivraisonErrorWrapper valider(LivraisonModel livraison, Integer commandeId) {
        LivraisonErrorWrapper errors = new LivraisonErrorWrapper();
        boolean hasError = false;

        if (livraison.getDateLivraison() == null) {
            errors.setDateLivraisonError("Veuillez renseigner la date de livraison.");
            hasError = true;
        }

        if (livraison.getLieu() == null || livraison.getLieu().trim().isEmpty()) {
            errors.setLieuError("Veuillez renseigner le lieu de livraison.");
            hasError = true;
        } else if (livraison.getLieu().trim().length() < 3) {
            errors.setLieuError("Le lieu doit contenir au moins 3 caractères.");
            hasError = true;
        }

        if (livraison.getLivreur() == null || livraison.getLivreur().getId() == null) {
            errors.setLivreurError("Veuillez sélectionner un livreur.");
            hasError = true;
        }

        if (commandeId == null) {
            errors.setCommandeError("Veuillez sélectionner une commande.");
            hasError = true;
        }

        return hasError ? errors : null;
    }

    public LivraisonErrorWrapper validerModification(LivraisonModel livraison) {
        LivraisonErrorWrapper errors = new LivraisonErrorWrapper();
        boolean hasError = false;

        if (livraison.getDateLivraison() == null) {
            errors.setDateLivraisonError("Veuillez renseigner la date de livraison.");
            hasError = true;
        }

        if (livraison.getLieu() == null || livraison.getLieu().trim().isEmpty()) {
            errors.setLieuError("Veuillez renseigner le lieu de livraison.");
            hasError = true;
        } else if (livraison.getLieu().trim().length() < 3) {
            errors.setLieuError("Le lieu doit contenir au moins 3 caractères.");
            hasError = true;
        }

        if (livraison.getLivreur() == null || livraison.getLivreur().getId() == null) {
            errors.setLivreurError("Veuillez sélectionner un livreur.");
            hasError = true;
        }

        return hasError ? errors : null;
    }
}