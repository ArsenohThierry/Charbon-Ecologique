package com.example.charbonecolo.model;

import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Table(name = "paiement")
public class PaiementModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reference", nullable = false, unique = true)
    private String reference;

    @ManyToOne
    @JoinColumn(name = "id_commande", referencedColumnName = "id", nullable = false)
    private CommandeModel commande;

    @Column(name = "montant_total", nullable = false)
    private BigDecimal montantTotal;

    // + getters/setters
        public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public CommandeModel getCommande() { return commande; }
    public void setCommande(CommandeModel commande) { this.commande = commande; }

    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }
}