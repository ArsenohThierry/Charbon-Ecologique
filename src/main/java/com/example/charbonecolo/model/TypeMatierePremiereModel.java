package com.example.charbonecolo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "type_matiere_premiere")
public class TypeMatierePremiereModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reference")
    private String reference;

    @NotBlank(message = "Le libellé est obligatoire.")
    @Size(max = 150, message = "Le libellé ne doit pas dépasser 150 caractères.")
    @Column(name = "libelle", length = 150, nullable = false)
    private String libelle;

    @NotNull(message = "Le prix unitaire est obligatoire.")
    @DecimalMin(value = "0.01", message = "Le prix unitaire doit être supérieur à zéro.")
    @Digits(integer = 8, fraction = 2, message = "Format de prix incorrect (maximum 8 chiffres avant la virgule et 2 décimales).")
    @Column(name = "prix_unitaire", precision = 10, scale = 2, nullable = false)
    private BigDecimal prixUnitaire;

    // Pas besoin de @NotNull ici si on valide l'idFournisseur dans le contrôleur, 
    // mais utile pour la cohérence interne.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_fournisseur", nullable = false)
    private FournisseurModel fournisseur;

    @Column(name = "date_ajout", nullable = false, updatable = false)
    private LocalDateTime dateAjout;

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Integer getFournisseurId(){
        return getFournisseur().getId();
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public void setPrixUnitaire(String s){
        if(s != null && !s.isEmpty()){
            setPrixUnitaire(new BigDecimal(s));
        }
    }

    public FournisseurModel getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(FournisseurModel fournisseur) {
        this.fournisseur = fournisseur;
    }

    public LocalDateTime getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDateTime dateAjout) {
        this.dateAjout = dateAjout;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }
}