package com.example.charbonecolo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FactureDto {

    private Integer id;
    private String reference;
    private LocalDateTime dateCommande;
    private String clientNom;
    private BigDecimal montantTotal;
    private String statutPaiement;

    public FactureDto(Integer id, String reference, LocalDateTime dateCommande,
                      String clientNom, BigDecimal montantTotal, String statutPaiement) {
        this.id = id;
        this.reference = reference;
        this.dateCommande = dateCommande;
        this.clientNom = clientNom;
        this.montantTotal = montantTotal;
        this.statutPaiement = statutPaiement;
    }

    public Integer getId() { return id; }
    public String getReference() { return reference; }
    public LocalDateTime getDateCommande() { return dateCommande; }
    public String getClientNom() { return clientNom; }
    public BigDecimal getMontantTotal() { return montantTotal; }
    public String getStatutPaiement() { return statutPaiement; }
}
