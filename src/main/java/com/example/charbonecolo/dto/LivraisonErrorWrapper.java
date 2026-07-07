package com.example.charbonecolo.dto;

public class LivraisonErrorWrapper {

    private String dateLivraisonError;
    private String lieuError;
    private String livreurError;
    private String commandeError;

    public String getDateLivraisonError() { return dateLivraisonError; }
    public void setDateLivraisonError(String dateLivraisonError) { this.dateLivraisonError = dateLivraisonError; }

    public String getLieuError() { return lieuError; }
    public void setLieuError(String lieuError) { this.lieuError = lieuError; }

    public String getLivreurError() { return livreurError; }
    public void setLivreurError(String livreurError) { this.livreurError = livreurError; }

    public String getCommandeError() { return commandeError; }
    public void setCommandeError(String commandeError) { this.commandeError = commandeError; }
}
