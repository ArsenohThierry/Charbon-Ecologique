package com.example.charbonecolo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.charbonecolo.exception.BusinessException;
import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.model.LotStatutsModel;
import com.example.charbonecolo.model.StatutsLotProductionModel;
import com.example.charbonecolo.repository.LotProductionRepository;
import com.example.charbonecolo.repository.LotStatutsRepository;
import com.example.charbonecolo.repository.StatutsLotProductionRepository;

@Service
public class LotProductionService {

    private final LotProductionRepository lotProductionRepository;
    private final StatutsLotProductionRepository statutsLotProductionRepository;
    private final LotStatutsRepository lotStatutsRepository;

    public LotProductionService(LotProductionRepository lotProductionRepository,
                                StatutsLotProductionRepository statutsLotProductionRepository,
                                LotStatutsRepository lotStatutsRepository) {
        this.lotProductionRepository = lotProductionRepository;
        this.statutsLotProductionRepository = statutsLotProductionRepository;
        this.lotStatutsRepository = lotStatutsRepository;
    }



    @Transactional
    public LotProductionModel saveLotProduction(LotProductionModel lotProduction) {
        lotProduction.setReference(genererReference());

        LotProductionModel savedLot = lotProductionRepository.save(lotProduction);

        LotStatutsModel statut = lotStatutsRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Statut 'En préparation' introuvable"));

        StatutsLotProductionModel statutLot = new StatutsLotProductionModel();
        statutLot.setLotProduction(savedLot);
        statutLot.setLotStatuts(statut);
        statutLot.setDateStatut(savedLot.getDateEntreeLot());

        statutsLotProductionRepository.save(statutLot);

        return savedLot;
    }


    public List<LotProductionModel> getAllLotProductions() {
        return lotProductionRepository.findAll();
    }

    public Map<Integer, String> getLatestStatutsForAllLots() {
        List<LotProductionModel> lots = lotProductionRepository.findAll();
        Map<Integer, String> statusMap = new HashMap<>();
        for (LotProductionModel lot : lots) {
            String status = statutsLotProductionRepository
                    .findTopByLotProductionOrderByDateStatutDesc(lot)
                    .map(s -> s.getLotStatuts() != null ? s.getLotStatuts().getLibelle() : "Inconnu")
                    .orElse("Inconnu");
            statusMap.put(lot.getId(), status);
        }
        return statusMap;
    }

    public Optional<LotProductionModel> getLotProductionById(Integer id) {
        return lotProductionRepository.findById(id);
    }

    public void deleteLotProduction(Integer id) {
        lotProductionRepository.deleteById(id);
    }

    private String genererReference() {
        // Compte le nombre de lots existants et génère le suivant
        long count = lotProductionRepository.count();
        return String.format("LOT-%03d", count + 1);
    }

    public LotProductionModel updateLotProduction(LotProductionModel lot) {
    return lotProductionRepository.save(lot);
}

    // ── Recherche (critères optionnels)
    public List<LotProductionModel> rechercherLots(String reference, Integer idProduit,
            Integer idTypeMatierePremiere, LocalDate dateDebut, LocalDate dateFin, Integer idLotStatut) {
        String refFiltre = (reference != null && !reference.isBlank()) ? reference.trim() : null;
        LocalDateTime debut = (dateDebut != null) ? dateDebut.atStartOfDay() : null;
        LocalDateTime fin = (dateFin != null) ? dateFin.atTime(23, 59, 59) : null;
        return lotProductionRepository.rechercher(refFiltre, idProduit, idTypeMatierePremiere, debut, fin, idLotStatut);
    }

    // ── Tri (colonnes cliquables sur la page liste des lots) ──────────────
    // "tri" est une clé whitelistée (pas une colonne SQL) : on trie en mémoire,
    // ce qui évite tout risque d'injection SQL via un nom de colonne dynamique.
    public void trier(List<LotProductionModel> lots, String tri, String direction, Map<Integer, String> statusMap) {
        Comparator<LotProductionModel> comparateur;
        switch (tri == null ? "" : tri) {
            case "reference":
                comparateur = Comparator.comparing(l -> l.getReference() == null ? "" : l.getReference(),
                        String.CASE_INSENSITIVE_ORDER);
                break;
            case "matiere":
                comparateur = Comparator.comparing(
                        l -> l.getTypeMatierePremiere() != null ? l.getTypeMatierePremiere().getLibelle() : "",
                        String.CASE_INSENSITIVE_ORDER);
                break;
            case "quantite":
                comparateur = Comparator.comparing(
                        l -> l.getQuantiteMatiereUtilisee() != null ? l.getQuantiteMatiereUtilisee() : BigDecimal.ZERO);
                break;
            case "produit":
                comparateur = Comparator.comparing(l -> l.getProduit() != null ? l.getProduit().getNom() : "",
                        String.CASE_INSENSITIVE_ORDER);
                break;
            case "statut":
                comparateur = Comparator.comparing(l -> statusMap.getOrDefault(l.getId(), ""),
                        String.CASE_INSENSITIVE_ORDER);
                break;
            case "date":
            default:
                comparateur = Comparator.comparing(
                        l -> l.getDateEntreeLot() != null ? l.getDateEntreeLot() : LocalDateTime.MIN);
                break;
        }
        if ("desc".equalsIgnoreCase(direction)) {
            comparateur = comparateur.reversed();
        }
        lots.sort(comparateur);
    }

    // Suivi de production : statut courant + progression 
    // Statut actuellement en cours pour un lot (le plus récent par date_statut).
    public Optional<StatutsLotProductionModel> getStatutActuel(LotProductionModel lot) {
        return statutsLotProductionRepository.findTopByLotProductionOrderByDateStatutDesc(lot);
    }

    // Historique des statuts traversés par ce lot, indexé par id de statut (lot_statuts.id).
    // S'il y a eu plusieurs cycles (même statut traversé plusieurs fois), on garde la ligne
    // la plus récente pour chaque statut (utile pour afficher la date de fin sur la page de suivi).
    public Map<Integer, StatutsLotProductionModel> getHistoriqueStatuts(LotProductionModel lot) {
        Map<Integer, StatutsLotProductionModel> historique = new HashMap<>();
        for (StatutsLotProductionModel ligne : statutsLotProductionRepository.findByLotProductionOrderByDateStatutAsc(lot)) {
            historique.put(ligne.getLotStatuts().getId(), ligne);
        }
        return historique;
    }

    // Valide la date de fin du statut en cours pour un lot, et fait avancer le lot
    // au statut suivant (déterminé UNIQUEMENT par l'ordre stocké en base, jamais par
    // une valeur envoyée par le client : on ne fait confiance qu'à la date fournie).
    @Transactional
    public void validerFinStatutCourant(Integer idLotProduction, LocalDateTime dateFin) {
        LotProductionModel lot = lotProductionRepository.findById(idLotProduction)
                .orElseThrow(() -> new BusinessException("Lot de production introuvable."));

        StatutsLotProductionModel statutCourant = getStatutActuel(lot)
                .orElseThrow(() -> new BusinessException("Ce lot n'a aucun statut enregistré."));

        if (statutCourant.getDateFin() != null) {
            throw new BusinessException("Le statut en cours a déjà été clôturé.");
        }
        if (dateFin == null) {
            throw new BusinessException("La date de fin est obligatoire.");
        }
        if (!dateFin.isAfter(statutCourant.getDateStatut())) {
            throw new BusinessException("La date de fin doit être postérieure à la date de début du statut en cours ("
                    + statutCourant.getDateStatut().toLocalDate() + ").");
        }
        if (dateFin.isAfter(LocalDateTime.now())) {
            throw new BusinessException("La date de fin ne peut pas être dans le futur.");
        }
        List<LotStatutsModel> statutsOrdonnes = lotStatutsRepository.findAllByOrderByOrdreAsc();
        statutsOrdonnes.sort(Comparator.comparing(LotStatutsModel::getOrdre));

        int indexActuel = -1;
        for (int i = 0; i < statutsOrdonnes.size(); i++) {
            if (statutsOrdonnes.get(i).getId().equals(statutCourant.getLotStatuts().getId())) {
                indexActuel = i;
                break;
            }
        }
        if (indexActuel == -1) {
            throw new BusinessException("Statut courant inconnu de la liste ordonnée des statuts.");
        }

        // "Termine" marque la fin du suivi de production géré par cette page : le passage
        // en stock ("En stock") est déclenché par le module Entrée stock, pas ici.
        if ("Termine".equals(statutCourant.getLotStatuts().getLibelle())) {
            throw new BusinessException("Ce lot est déjà marqué comme terminé.");
        }

        statutCourant.setDateFin(dateFin);
        statutsLotProductionRepository.save(statutCourant);

        if (indexActuel + 1 < statutsOrdonnes.size()) {
            LotStatutsModel prochainStatut = statutsOrdonnes.get(indexActuel + 1);
            // Le passage en stock reste hors du périmètre de cette page de suivi.
            if (!"En stock".equals(prochainStatut.getLibelle())) {
                StatutsLotProductionModel nouveauStatut = new StatutsLotProductionModel();
                nouveauStatut.setLotProduction(lot);
                nouveauStatut.setLotStatuts(prochainStatut);
                nouveauStatut.setDateStatut(dateFin);
                statutsLotProductionRepository.save(nouveauStatut);

                if ("Termine".equals(prochainStatut.getLibelle())) {
                    lot.setDateFinReelle(dateFin);
                    lotProductionRepository.save(lot);
                }
            }
        }
    }
}
