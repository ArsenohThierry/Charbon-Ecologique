package com.example.charbonecolo.dto;

import java.time.LocalDate;

public record AlerteProduitDTO(
        String produitNom,
        double stockActuel,
        double seuil,
        String niveauAlerte,
        Double venteMoyenneJour,
        Double joursAvantRupture,
        LocalDate ruptureEstimee   // null si pas de données de vente
) {}