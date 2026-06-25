package com.example.charbonecolo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateur")
public class UtilisateurModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String username;

    @Column(length = 20)
    private String telephone;

    @Column(name = "mot_passe", nullable = false, length = 255)
    private String motPasse;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_role", nullable = false)
    private RoleModel role;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private Boolean actif;

    public UtilisateurModel() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getMotPasse() { return motPasse; }
    public void setMotPasse(String motPasse) { this.motPasse = motPasse; }
    public RoleModel getRole() { return role; }
    public void setRole(RoleModel role) { this.role = role; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
}
