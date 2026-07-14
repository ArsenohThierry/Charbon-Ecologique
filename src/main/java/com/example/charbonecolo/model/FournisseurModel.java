package com.example.charbonecolo.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "fournisseur")
public class FournisseurModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 150, nullable = false)
    @NotBlank(message = "Le nom du fournisseur est obligatoire.")
    @Size(max = 150, message = "Le nom ne doit pas dépasser 150 caractères.")
    private String nom;

    @Column(length = 150)
    @Email(message = "Le format de l'adresse email est invalide.")
    @Size(max = 150, message = "L'email ne doit pas dépasser 150 caractères.")
    private String email;

    @Column(length = 20)
    @Size(max = 20, message = "Le numéro de téléphone ne doit pas dépasser 20 caractères.")
    private String telephone;

    @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères.")
    private String adresse;

    @Column(name = "date_creation")
    private LocalDateTime date_creation;

    @Column(name = "actif", nullable = false)
    @NotNull(message = "Le statut est obligatoire.")
    private boolean actif;

    @Column(name = "delete_at")
    private LocalDateTime delete_at;

    // --- Gardez vos getters, setters et méthodes utilitaires existants ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public void setActif(String s){ this.actif = "true".equals(s); }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public LocalDateTime getDate_creation() { return date_creation; }
    public void setDate_creation(LocalDateTime date_creation) { this.date_creation = date_creation; }
    public LocalDateTime getDelete_at() {return delete_at;}
    public void setDelete_at(LocalDateTime delete_at) {this.delete_at = delete_at;}
}