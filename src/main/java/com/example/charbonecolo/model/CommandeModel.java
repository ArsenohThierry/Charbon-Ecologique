package com.example.charbonecolo.model;


import java.time.LocalDateTime;

import org.hibernate.annotations.SQLRestriction;

import com.example.charbonecolo.dto.CommandeDto;

import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;

@Entity
@Table(name = "commandes")
@SQLRestriction("deleted_at IS NULL")
@SqlResultSetMapping(
    name = "CommandeDtoMapping",
    classes = @ConstructorResult(
        targetClass = CommandeDto.class,
        columns = {
            @ColumnResult(name = "id", type = Integer.class),
            @ColumnResult(name = "reference", type = String.class),
            @ColumnResult(name = "date_commande", type = LocalDateTime.class),
            @ColumnResult(name = "client_nom", type = String.class),
            @ColumnResult(name = "montant_total", type = Double.class),
            @ColumnResult(name = "id_commande_statuts", type = Integer.class),
            @ColumnResult(name = "statut_libelle", type = String.class),
        }
    )
)
public class CommandeModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reference")
    private String reference;

    @ManyToOne
    @JoinColumn(name = "id_client", referencedColumnName = "id")
    private ClientModel client;

    public ClientModel getClient() {
        return client;
    }

    public void setClient(ClientModel client) {
        this.client = client;
    }

    @Column(name = "date_commande")
    private LocalDateTime dateCommande;

    public LocalDateTime getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDateTime dateCommande) {
        this.dateCommande = dateCommande;
    }

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    
}
