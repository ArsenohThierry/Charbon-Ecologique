package com.example.charbonecolo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_excel")
public class ImportExcelModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_fichier", nullable = false)
    private String nomFichier;

    @Column(name = "date_import", nullable = false)
    private LocalDateTime dateImport;

    @Column(name = "nb_lignes")
    private Integer nbLignes;

    @Column(nullable = false, length = 20)
    private String statut; // SUCCES, PARTIEL, ECHEC

    @Column(name = "message_log", columnDefinition = "TEXT")
    private String messageLog;

    public ImportExcelModel() {}

    public ImportExcelModel(String nomFichier, LocalDateTime dateImport,
                             Integer nbLignes, Integer nbErreurs,
                             String statut, String messageLog) {
        this.nomFichier = nomFichier;
        this.dateImport = dateImport;
        this.nbLignes = nbLignes;
        this.statut = statut;
        this.messageLog = messageLog;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public LocalDateTime getDateImport() { return dateImport; }
    public void setDateImport(LocalDateTime dateImport) { this.dateImport = dateImport; }

    public Integer getNbLignes() { return nbLignes; }
    public void setNbLignes(Integer nbLignes) { this.nbLignes = nbLignes; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getMessageLog() { return messageLog; }
    public void setMessageLog(String messageLog) { this.messageLog = messageLog; }
}
