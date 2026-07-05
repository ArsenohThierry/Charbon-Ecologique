package com.example.charbonecolo.dto;

public record LotStockSummaryDTO(
        Integer produitId,
        String produitNom,
        String reference,
        int totalEntree,
        int totalSortie,
        int restant
) {}