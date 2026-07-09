package com.example.charbonecolo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.charbonecolo.dto.FactureCriteriaWrapper;
import com.example.charbonecolo.dto.FactureDto;
import com.example.charbonecolo.dto.FactureErrorWrapper;
import com.example.charbonecolo.model.*;
import com.example.charbonecolo.repository.*;

import jakarta.annotation.PostConstruct;

@Service
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final CommandeRepository commandeRepository;
    private final DetailCommandeRepository detailCommandeRepository;
    private final StatutCommandeRepository statutCommandeRepository;
    private final JdbcTemplate jdbcTemplate;
    private final FactureRepository factureRepository;
    private final FactureDetailRepository factureDetailRepository;

    public PaiementService(PaiementRepository paiementRepository,
            CommandeRepository commandeRepository,
            DetailCommandeRepository detailCommandeRepository,
            StatutCommandeRepository statutCommandeRepository,
            JdbcTemplate jdbcTemplate, FactureDetailRepository factureDetailRepository,
            FactureRepository factureRepository) {
        this.paiementRepository = paiementRepository;
        this.commandeRepository = commandeRepository;
        this.detailCommandeRepository = detailCommandeRepository;
        this.statutCommandeRepository = statutCommandeRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.factureRepository = factureRepository;
        this.factureDetailRepository = factureDetailRepository;
    }

    @PostConstruct
    public void initDonnees() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM paiement_statuts", Integer.class);
        if (count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO paiement_statuts (libelle) VALUES ('Non payée'), ('Payée'), ('Partiellement payée')");
        }
        count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM methode_paiement", Integer.class);
        if (count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO methode_paiement (libelle) VALUES ('Espèces'), ('Mobile money'), ('Carte bancaire'), ('Virement')");
        }
        Integer existe = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM commande_statuts WHERE id = 6",
                Integer.class);
        if (existe == 0) {
            jdbcTemplate.update("INSERT INTO commande_statuts (id, libelle) VALUES (6, 'payee')");
        }
    }

    // 1. Liste paginée avec filtre (style Nomena)
    public Slice<FactureDto> listCommandesFiltrees(Pageable pageable, FactureCriteriaWrapper cri) {
        Slice<Object[]> sliceBrut = paiementRepository.findCommandesFiltrees(pageable, cri);
        return sliceBrut.map(ligne -> new FactureDto(
                (Integer) ligne[0],
                (String) ligne[1],
                (LocalDateTime) ligne[2],
                (String) ligne[3],
                (BigDecimal) ligne[4],
                (String) ligne[5]));
    }

    // 2. Calcule le montant d'une commande (somme des détails)
    public BigDecimal calculerMontantCommande(Integer commandeId) {
        List<DetailCommandeModel> details = detailCommandeRepository.findByCommandeId(commandeId);
        return details.stream()
                .map(DetailCommandeModel::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 3. Validation du formulaire facture
    public FactureErrorWrapper validerFacture(BigDecimal fraisLivraison, Integer methodePaiementId) {
        FactureErrorWrapper errors = new FactureErrorWrapper();
        boolean hasError = false;

        if (fraisLivraison == null || fraisLivraison.compareTo(BigDecimal.ZERO) < 0) {
            errors.setFraisLivraisonError("Veuillez entrer un montant valide pour les frais de livraison.");
            hasError = true;
        }

        if (methodePaiementId == null || methodePaiementId <= 0) {
            errors.setMethodePaiementError("Veuillez sélectionner une méthode de paiement.");
            hasError = true;
        }

        return hasError ? errors : null;
    }

    // 4. Crée la facture complète
    @Transactional
    public Map<String, Object> creerFacture(Integer commandeId, BigDecimal fraisLivraison, Integer methodePaiementId) {
        BigDecimal montantCmd = calculerMontantCommande(commandeId);
        List<DetailCommandeModel> details = detailCommandeRepository.findByCommandeId(commandeId);
        String refPaiement = "PAI-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String refFacture = "FACT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        CommandeModel commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + commandeId));

        PaiementModel paiement = new PaiementModel();
        paiement.setReference(refPaiement);
        paiement.setCommande(commande);
        paiement.setMontantTotal(montantCmd);
        paiement = paiementRepository.save(paiement);

        Integer idStatutPayee;
        try {
            idStatutPayee = jdbcTemplate.queryForObject(
                    "SELECT id FROM paiement_statuts WHERE libelle = 'Payée'", Integer.class);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            jdbcTemplate.update("INSERT INTO paiement_statuts (libelle) VALUES ('Payée')");
            idStatutPayee = jdbcTemplate.queryForObject(
                    "SELECT id FROM paiement_statuts WHERE libelle = 'Payée'", Integer.class);
        }

        jdbcTemplate.update("""
                    INSERT INTO statuts_paiements (id_paiement, id_statut_paiement, id_methode_paiement, date_statut)
                    VALUES (?, ?, ?, NOW())
                """, paiement.getId(), idStatutPayee, methodePaiementId);

        // jdbcTemplate.update("""
        // INSERT INTO facture (reference, id_paiement)
        // VALUES (?, ?)
        // """, refFacture, paiement.getId());

        // jdbcTemplate.update("""
        // INSERT INTO facture_detail (id_facture, montant, libelle)
        // VALUES ((SELECT id FROM facture WHERE reference = ?), ?, 'Montant commande')
        // """, refFacture, montantCmd);

        // jdbcTemplate.update("""
        // INSERT INTO facture_detail (id_facture, montant, libelle)
        // VALUES ((SELECT id FROM facture WHERE reference = ?), ?, 'Frais de
        // livraison')
        // """, refFacture, fraisLivraison);
        FactureModel toSave = new FactureModel();
        toSave.setPaiement(paiement);
        toSave.setReference(refFacture);
        FactureModel saved = factureRepository.save(toSave);
        List<FactureDetailModel> factureDetails = new ArrayList<>();
        for (DetailCommandeModel detail : details) {
            FactureDetailModel factDetail = new FactureDetailModel();
            factDetail.setFacture(saved);
            factDetail.setLibelle(detail.getProduit().getNom());
            factDetail.setMontant(detail.getMontant().intValue());
            factDetail.setPu(detail.getProduit().getPu().intValue());
            factDetail.setQuantite(detail.getQuantite());
            factureDetails.add(factDetail);
        }
        FactureDetailModel factDetail = new FactureDetailModel();
        factDetail.setFacture(saved);
        factDetail.setLibelle("Frais de livraison");
        factDetail.setMontant(fraisLivraison.intValue());
        factDetail.setPu(fraisLivraison.intValue());
        factDetail.setQuantite(1);
        factureDetails.add(factDetail);
        factureDetailRepository.saveAll(factureDetails);

        Integer existeStatut = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM commande_statuts WHERE id = 6",
                Integer.class);
        if (existeStatut == 0) {
            jdbcTemplate.update("INSERT INTO commande_statuts (id, libelle) VALUES (6, 'payee')");
        }
        CommandeStatutModel statutPayee = new CommandeStatutModel();
        statutPayee.setId(6);
        StatutCommandeModel sc = new StatutCommandeModel();
        sc.setCommande(commande);
        sc.setStatut(statutPayee);
        statutCommandeRepository.save(sc);

        Map<String, Object> result = new HashMap<>();
        result.put("refFacture", refFacture);
        result.put("idFacture", jdbcTemplate.queryForObject(
                "SELECT id FROM facture WHERE reference = ?", Integer.class, refFacture));
        return result;
    }

    // 5. Récupère les détails d'une facture
    public Map<String, Object> getFactureDetail(Integer factureId) {
        String sql = """
                    SELECT f.id AS facture_id, f.reference AS facture_reference,
                           p.reference AS paiement_reference,
                           c.reference AS commande_reference,
                           cli.nom AS client_nom,
                           p.montant_total AS montant_commande
                    FROM facture f
                    JOIN paiement p ON p.id = f.id_paiement
                    JOIN commandes c ON c.id = p.id_commande
                    JOIN clients cli ON cli.id = c.id_client
                    WHERE f.id = ?
                """;
        Map<String, Object> facture = jdbcTemplate.queryForMap(sql, factureId);

        List<Map<String, Object>> details = jdbcTemplate.queryForList(
                "SELECT libelle, montant FROM facture_detail WHERE id_facture = ?", factureId);

        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> d : details) {
            total = total.add((BigDecimal) d.get("montant"));
        }

        facture.put("details", details);
        facture.put("total", total);
        return facture;
    }
}
