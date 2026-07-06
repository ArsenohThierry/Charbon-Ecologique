package com.example.charbonecolo.dto;

public record MouvementMensuelDTO(
        String mois,        // ex. "2026-07"
        int totalEntree,
        int totalSortie
) {}