package com.example.charbonecolo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "import_excel")
public class ImportExcelModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nom_fichier", nullable = false, length = 100)
    private String nomFichier;

    @Column(name = "date_import", nullable = false)
    private LocalDateTime dateImport;

    @Column(nullable = false, length = 20)
    private String statut; // EN_COURS / TERMINE / ERREUR

    @Column(name = "nb_lignes")
    private Integer nbLignes;

    @Column(name = "message_erreur", columnDefinition = "TEXT")
    private String messageErreur;

    public ImportExcelModel() {}

    public ImportExcelModel(Integer id, String nomFichier, LocalDateTime dateImport, String statut, Integer nbLignes, String messageErreur) {
        this.id = id;
        this.nomFichier = nomFichier;
        this.dateImport = dateImport;
        this.statut = statut;
        this.nbLignes = nbLignes;
        this.messageErreur = messageErreur;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public LocalDateTime getDateImport() {
        return dateImport;
    }

    public void setDateImport(LocalDateTime dateImport) {
        this.dateImport = dateImport;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Integer getNbLignes() {
        return nbLignes;
    }

    public void setNbLignes(Integer nbLignes) {
        this.nbLignes = nbLignes;
    }

    public String getMessageErreur() {
        return messageErreur;
    }

    public void setMessageErreur(String messageErreur) {
        this.messageErreur = messageErreur;
    }
}