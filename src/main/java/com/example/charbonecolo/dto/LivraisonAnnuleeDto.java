package com.example.charbonecolo.dto;

import java.util.List;

public class LivraisonAnnuleeDto {

    private Integer idLivraison;
    private String reference;
    private Integer commandeId;
    private String commandeReference;

    public LivraisonAnnuleeDto(Integer idLivraison, String reference,
                               Integer commandeId, String commandeReference) {
        this.idLivraison = idLivraison;
        this.reference = reference;
        this.commandeId = commandeId;
        this.commandeReference = commandeReference;
    }

    public LivraisonAnnuleeDto() {}

    public Integer getIdLivraison() { return idLivraison; }
    public void setIdLivraison(Integer idLivraison) { this.idLivraison = idLivraison; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Integer getCommandeId() { return commandeId; }
    public void setCommandeId(Integer commandeId) { this.commandeId = commandeId; }

    public String getCommandeReference() { return commandeReference; }
    public void setCommandeReference(String commandeReference) { this.commandeReference = commandeReference; }
}