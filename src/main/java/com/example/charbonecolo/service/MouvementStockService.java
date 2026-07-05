package com.example.charbonecolo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.charbonecolo.dto.AlerteProduitDTO;
import com.example.charbonecolo.dto.EntreeCriteriaWrapper;
import com.example.charbonecolo.dto.EntreeDto;
import com.example.charbonecolo.dto.EntreeStockDTO;
import com.example.charbonecolo.dto.EtatStockCriteriaWrapper;
import com.example.charbonecolo.dto.EtatStockDto;
import com.example.charbonecolo.dto.LotStockSummaryDTO;
import com.example.charbonecolo.dto.MouvementMensuelDTO;
import com.example.charbonecolo.dto.SortieCriteriaWrapper;
import com.example.charbonecolo.dto.SortieDto;
import com.example.charbonecolo.dto.SortieStockDTO;
import com.example.charbonecolo.exception.BusinessException;
import com.example.charbonecolo.exception.FieldBusinessException;
import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.model.LotStatutsModel;
import com.example.charbonecolo.model.MotifSortieModel;
import com.example.charbonecolo.model.MouvementSortieDetailModel;
import com.example.charbonecolo.model.MouvementStockModel;
import com.example.charbonecolo.model.ProduitModel;
import com.example.charbonecolo.model.SeuilModel;
import com.example.charbonecolo.model.TypeMouvementStockModel;
import com.example.charbonecolo.repository.AlerteSeuilRepository;
import com.example.charbonecolo.repository.LotProductionRepository;
import com.example.charbonecolo.repository.LotStatutsRepository;
import com.example.charbonecolo.repository.MotifSortieRepository;
import com.example.charbonecolo.repository.MouvementSortieDetailRepository;
import com.example.charbonecolo.repository.MouvementStockRepository;
import com.example.charbonecolo.repository.ProduitRepository;
import com.example.charbonecolo.repository.SeuilRepository;
import com.example.charbonecolo.repository.StatutsLotProductionRepository;
import com.example.charbonecolo.repository.TypeMouvementStockRepository;

@Service
public class MouvementStockService {

    @Autowired
    private MouvementStockRepository mouvementStockRepository;
    @Autowired
    private TypeMouvementStockRepository typeMouvementStockRepository;
    @Autowired
    private MotifSortieRepository motifSortieRepository;
    @Autowired
    private LotProductionRepository lotProductionRepository;
    @Autowired
    private ProduitRepository produitRepository;
    @Autowired
    private MouvementSortieDetailRepository mouvementSortieDetailRepository;
    @Autowired
    private StatutsLotProductionRepository statutsLotProductionRepository;
    @Autowired
    private LotStatutsRepository lotStatutsRepository;

    @Autowired
    private SeuilRepository seuilRepository;
    @Autowired
    private AlerteSeuilRepository alerteSeuilRepository;

    private TypeMouvementStockModel sortieType;
    private List<MouvementStockModel> sorties;
    private TypeMouvementStockModel entreeType;
    private List<MouvementStockModel> entrees;
    private List<SeuilModel> ruptures;
    private List<SeuilModel> faibles;

    // ── Méthodes existantes ──────────────────────────────────────

    public Optional<MouvementStockModel> getMouvementStockById(Integer id) {
        return mouvementStockRepository.findById(id);
    }

    public List<MouvementStockModel> getAllMouvementsStock() {
        return mouvementStockRepository.findAll();
    }

    public List<LotProductionModel> getLotsTermines() {
        Optional<LotStatutsModel> termineOpt = lotStatutsRepository.findByLibelle("Termine");
        if (termineOpt.isEmpty())
            return List.of();
        LotStatutsModel termine = termineOpt.get();
        return lotProductionRepository.findAll().stream()
                .filter(lot -> statutsLotProductionRepository
                        .findTopByLotProductionOrderByDateStatutDesc(lot)
                        .map(s -> s.getLotStatuts() != null && termine.getId().equals(s.getLotStatuts().getId()))
                        .orElse(false))
                .collect(Collectors.toList());
    }

    public List<TypeMouvementStockModel> getAllTypesMouvement() {
        return typeMouvementStockRepository.findAll();
    }

    public List<MotifSortieModel> getAllMotifsSortie() {
        return motifSortieRepository.findAll();
    }

    public List<ProduitModel> getAllProduits() {
        return produitRepository.findAll();
    }

    private LocalDateTime toDateTimeOuMaintenant(LocalDate date) {
        return date != null ? LocalDateTime.of(date, LocalTime.now()) : LocalDateTime.now();
    }

    public boolean lotEstDejaConsomme(LotProductionModel lot) {
        return mouvementSortieDetailRepository.isLotUsed(lot.getId());
    }

    public int getNombreLotsFinis() {
        return getLotsTermines().size();
    }

    // ── ENTRÉE ───────────────────────────────────────────────────

    @Transactional
    public void saveEntreeStock(EntreeStockDTO entry) {

        LotProductionModel lot = lotProductionRepository.findById(entry.getIdLot())
                .orElseThrow();

        if (mouvementStockRepository.existsEntreeByLotProduction(lot)) {
            throw new FieldBusinessException("idLot", "Ce lot est déjà en stock.");
        }

        LocalDateTime dateEntree = toDateTimeOuMaintenant(entry.getDateEntree());
        LocalDateTime dateTermine = statutsLotProductionRepository.findDateTermineByLotProductionId(lot.getId())
                .orElseThrow(() -> new BusinessException("Le lot n'a pas encore été terminé."));
        lot.setQuantiteProduitReelle(entry.getQuantite());
        if (dateEntree.isBefore(lot.getDateEntreeLot())) {
            throw new BusinessException("Le lot n'a pas encore ete produit a la date d'entree en stock.");
        }
        if (dateEntree.isBefore(dateTermine)) {
            throw new BusinessException("Le lot n'a pas encore été terminé à la date d'entrée choisie.");
        }
        lot.setDateFinReelle(dateEntree);
        lotProductionRepository.save(lot);

        MouvementStockModel mouvement = new MouvementStockModel();
        mouvement.setLotProduction(lot);
        mouvement.setQuantite(entry.getQuantite());
        mouvement.setDateMouvement(dateEntree);
        mouvement.setTypeMouvement(typeMouvementStockRepository.findById(1).orElseThrow());
        mouvement.setMotifSortie(null);

        mouvementStockRepository.save(mouvement);
    }

    @Transactional
    public void updateEntreeStock(EntreeStockDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("L'identifiant du mouvement est requis pour une mise à jour.");
        }

        MouvementStockModel mouvement = mouvementStockRepository.findById(dto.getId()).orElseThrow();

        LotProductionModel lot = mouvement.getLotProduction();
        if (lotEstDejaConsomme(lot)) {
            throw new BusinessException(
                    "Modification impossible : ce lot a déjà été utilisé dans des sorties.");
        }

        lot.setQuantiteProduitReelle(dto.getQuantite());
        lotProductionRepository.save(lot);

        mouvement.setQuantite(dto.getQuantite());
        mouvement.setDateMouvement(toDateTimeOuMaintenant(dto.getDateEntree()));

        mouvementStockRepository.save(mouvement);
    }

    // ── SORTIE (FIFO) ────────────────────────────────────────────

    @Transactional
    public void saveSortieStock(SortieStockDTO dto) {
        Integer idProduit = dto.getIdProduit();
        Integer quantite = dto.getQuantite();
        Integer idMotif = dto.getIdMotif();
        LocalDate date = dto.getDateSortie() != null ? dto.getDateSortie() : LocalDate.now();

        List<LotProductionModel> lotsDisponibles = lotProductionRepository
                .findLotsWithStockByProduitOrderByDateAsc(idProduit);

        int totalDisponible = lotsDisponibles.stream()
                .mapToInt(l -> getStockDisponible(l.getId()))
                .sum();

        if (totalDisponible < quantite) {
            throw new FieldBusinessException("quantite",
                    "Stock insuffisant. Disponible: " + totalDisponible + ", demandé: " + quantite);
        }

        MouvementStockModel mouvement = new MouvementStockModel();
        mouvement.setLotProduction(null);
        mouvement.setQuantite(quantite);
        mouvement.setDateMouvement(toDateTimeOuMaintenant(date));
        mouvement.setTypeMouvement(typeMouvementStockRepository.findById(2).orElseThrow());
        mouvement.setMotifSortie(motifSortieRepository.findById(idMotif).orElseThrow());
        mouvement = mouvementStockRepository.save(mouvement);

        int reste = quantite;
        List<MouvementSortieDetailModel> details = new ArrayList<>();
        for (LotProductionModel lot : lotsDisponibles) {
            System.out.println(
                    lot.getReference()
                            + " | stock = " + getStockDisponible(lot.getId())
                            + " | entrée = " + mouvementStockRepository.getDateEntreeByLotProduction(lot));
        }

        for (LotProductionModel lot : lotsDisponibles) {
            if (reste <= 0)
                break;

            int dispo = getStockDisponible(lot.getId());
            int pris = Math.min(dispo, reste);

            if (pris <= 0)
                continue;

            LocalDateTime dateEntreeLot = mouvementStockRepository.getDateEntreeByLotProduction(lot);
            if (dateEntreeLot != null && date.isBefore(dateEntreeLot.toLocalDate())) {
                throw new FieldBusinessException("dateSortie",
                        "La date de sortie ne peut pas être antérieure à la date d'entrée du lot " + lot.getReference()
                                + ".");
            }

            MouvementSortieDetailModel d = new MouvementSortieDetailModel();
            d.setMouvementSortie(mouvement);
            d.setLotProduction(lot);
            d.setQuantite(pris);
            details.add(d);

            reste -= pris;
        }

        mouvementSortieDetailRepository.saveAll(details);
    }

    @Transactional
    public void updateSortieStock(SortieStockDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("L'identifiant du mouvement est requis pour une mise à jour.");
        }
        MouvementStockModel mouvement = mouvementStockRepository.findById(dto.getId()).orElseThrow();

        List<MouvementSortieDetailModel> oldDetails = mouvementSortieDetailRepository.findByMouvementSortie(mouvement);

        Integer idProduit = oldDetails.isEmpty() ? null : oldDetails.get(0).getLotProduction().getProduit().getId();

        if (idProduit == null) {
            throw new IllegalStateException("Impossible de déterminer le produit de la sortie");
        }

        mouvementSortieDetailRepository.deleteByMouvementSortie(mouvement);

        LocalDate date = dto.getDateSortie() != null ? dto.getDateSortie() : LocalDate.now();

        mouvement.setQuantite(dto.getQuantite());
        mouvement.setMotifSortie(motifSortieRepository.findById(dto.getIdMotif()).orElseThrow());
        mouvement.setDateMouvement(toDateTimeOuMaintenant(date));
        mouvementStockRepository.save(mouvement);

        List<LotProductionModel> lotsDisponibles = lotProductionRepository
                .findLotsWithStockByProduitOrderByDateAsc(idProduit);

        int totalDisponible = lotsDisponibles.stream()
                .mapToInt(l -> getStockDisponible(l.getId()))
                .sum();

        if (totalDisponible < dto.getQuantite()) {
            throw new FieldBusinessException("quantite",
                    "Stock insuffisant après restauration. Disponible: " + totalDisponible + ", demandé: "
                            + dto.getQuantite());
        }

        int resteASortir = dto.getQuantite();
        List<MouvementSortieDetailModel> newDetails = new ArrayList<>();

        for (LotProductionModel lot : lotsDisponibles) {
            if (resteASortir <= 0)
                break;

            int dispo = getStockDisponible(lot.getId());
            int pris = Math.min(dispo, resteASortir);

            if (pris <= 0)
                continue;

            LocalDateTime dateEntreeLot = mouvementStockRepository.getDateEntreeByLotProduction(lot);
            if (dateEntreeLot != null && date.isBefore(dateEntreeLot.toLocalDate())) {
                throw new FieldBusinessException("dateSortie",
                        "La date de sortie ne peut pas être antérieure à la date d'entrée du lot " + lot.getReference()
                                + ".");
            }

            MouvementSortieDetailModel detail = new MouvementSortieDetailModel();
            detail.setMouvementSortie(mouvement);
            detail.setLotProduction(lot);
            detail.setQuantite(pris);
            newDetails.add(detail);

            resteASortir -= pris;
        }

        mouvementSortieDetailRepository.saveAll(newDetails);
    }

    // ── SUPPRESSION ──────────────────────────────────────────────
    @Transactional
    public void deleteMouvementStock(Integer id) {

        MouvementStockModel mouvement = mouvementStockRepository.findById(id)
                .orElseThrow();

        boolean isEntree = mouvement.getTypeMouvement().getId() == 1;

        if (isEntree) {

            LotProductionModel lot = mouvement.getLotProduction();

            int stock = getStockDisponible(lot.getId()) - mouvement.getQuantite();

            if (stock < 0) {
                throw new BusinessException(
                        "Impossible de supprimer cette entrée : une partie du stock a déjà été utilisée.");
            }

        } else {
            mouvementSortieDetailRepository.deleteByMouvementSortie(mouvement);
        }

        mouvementStockRepository.delete(mouvement);
    }

    public Integer getStockDisponible(Integer idLot) {

        Integer entree = mouvementStockRepository.sumEntreesByLot(idLot);
        Integer sortie = mouvementSortieDetailRepository.sumSortiesByLot(idLot);

        return entree - sortie;
    }

    // ── ÉTAT DU STOCK ────────────────────────────────────────────

    public List<LotProductionModel> getLotsWithStockByProduit(Integer idProduit) {
        return lotProductionRepository.findLotsWithStockByProduitOrderByDateAsc(idProduit);
    }

    public List<MouvementSortieDetailModel> getDetailsByMouvement(MouvementStockModel mouvement) {
        return mouvementSortieDetailRepository.findByMouvementSortie(mouvement);
    }

    public List<LotStockSummaryDTO> getStockParLot() {
        List<LotProductionModel> lots = lotProductionRepository.findAll();
        List<LotStockSummaryDTO> result = new ArrayList<>();

        for (LotProductionModel lot : lots) {
            int totalEntree = mouvementStockRepository.sumEntreesByLot(lot.getId());
            if (totalEntree == 0)
                continue;

            int totalSortie = mouvementSortieDetailRepository.sumSortiesByLot(lot.getId());

            result.add(new LotStockSummaryDTO(
                    lot.getProduit().getId(),
                    lot.getProduit().getNom(),
                    lot.getReference(),
                    totalEntree,
                    totalSortie,
                    totalEntree - totalSortie));
        }
        return result;
    }

    public int getTotalEntreeGlobal() {
        return mouvementStockRepository.sumTotalEntrees();
    }

    public int getTotalSortieGlobal() {
        return mouvementSortieDetailRepository.sumTotalSorties();
    }

    public int getStockRestantGlobal() {
        return getTotalEntreeGlobal() - getTotalSortieGlobal();
    }

    public List<AlerteProduitDTO> getAlertesActives() {
        List<LotStockSummaryDTO> stockParLot = getStockParLot();

        // Étape 1 : additionner le restant de tous les lots, par produit
        Map<Integer, Integer> stockParProduit = new HashMap<>();
        Map<Integer, String> nomParProduit = new HashMap<>();

        for (LotStockSummaryDTO l : stockParLot) {
            Integer idProduit = l.produitId();

            int stockActuel = stockParProduit.getOrDefault(idProduit, 0);
            stockParProduit.put(idProduit, stockActuel + l.restant());

            nomParProduit.put(idProduit, l.produitNom());
        }

        // Étape 2 : pour chaque produit, vérifier s'il y a une alerte
        List<AlerteProduitDTO> alertes = new ArrayList<>();

        for (Integer idProduit : stockParProduit.keySet()) {
            double stockActuel = stockParProduit.get(idProduit);
            String nomProduit = nomParProduit.get(idProduit);

            double seuilValeur;
            String niveau;

            if (stockActuel <= 0) {
                seuilValeur = 0;
                niveau = "Rupture";
            } else {
                SeuilModel seuilDepasse = trouverSeuilLePlusSevere(idProduit, stockActuel);
                if (seuilDepasse == null) {
                    continue; // aucun seuil dépassé, pas d'alerte pour ce produit
                }
                seuilValeur = seuilDepasse.getValeur();
                niveau = seuilDepasse.getAlerteSeuil().getLibelle();
            }
            // Calcul de la vente moyenne et de la date de rupture estimée
            Double venteMoyenne = calculerVenteMoyenneJour(idProduit);

            Double joursAvantRupture = (venteMoyenne != null && venteMoyenne > 0)
                    ? stockActuel / venteMoyenne
                    : null;

            LocalDate ruptureEstimee = (joursAvantRupture != null)
                    ? LocalDate.now().plusDays(Math.round(joursAvantRupture))
                    : null;

            alertes.add(new AlerteProduitDTO(
                    nomProduit, stockActuel, seuilValeur, niveau,
                    venteMoyenne, joursAvantRupture, ruptureEstimee));
        }

        return alertes;
    }

    // Cherche, parmi tous les seuils dépassés pour ce produit, celui qui a la
    // valeur la plus basse (le plus sévère)
    private SeuilModel trouverSeuilLePlusSevere(Integer idProduit, double stockActuel) {
        List<SeuilModel> tousLesSeuils = seuilRepository.findAll();
        SeuilModel seuilLePlusSevere = null;

        for (SeuilModel seuil : tousLesSeuils) {
            boolean memeProduit = seuil.getProduit().getId().equals(idProduit);
            boolean estDepasse = stockActuel <= seuil.getValeur();

            if (memeProduit && estDepasse) {
                if (seuilLePlusSevere == null || seuil.getValeur() < seuilLePlusSevere.getValeur()) {
                    seuilLePlusSevere = seuil;
                }
            }
        }
        return seuilLePlusSevere;
    }

    public int countAlertStock() {
        return getAlertesActives().size();
    }

    private static final int PERIODE_MAX_JOURS = 30;

    private Double calculerVenteMoyenneJour(Integer idProduit) {
        LocalDateTime premiereSorte = mouvementSortieDetailRepository.findFirstSortieDateByProduit(idProduit);
        if (premiereSorte == null) {
            return null; // aucune sortie enregistrée, impossible de calculer
        }

        long joursEcoules = java.time.temporal.ChronoUnit.DAYS.between(premiereSorte, LocalDateTime.now());
        long nombreJours = Math.max(1, Math.min(PERIODE_MAX_JOURS, joursEcoules));

        LocalDateTime depuis = LocalDateTime.now().minusDays(nombreJours);
        int totalSorties = mouvementSortieDetailRepository.sumSortiesByProduitDepuis(idProduit, depuis);

        return totalSorties / (double) nombreJours;
    }

    public int getStockRestantParProduit(Integer idProduit) {
        List<LotProductionModel> lots = getLotsWithStockByProduit(idProduit);
        int stockRestant = 0;

        for (LotProductionModel lot : lots) {
            stockRestant += getStockDisponible(lot.getId());
        }

        return stockRestant;
    }

    @Transactional(readOnly = true)
    public Slice<SortieDto> listSorties(Pageable pageable, SortieCriteriaWrapper wrapper) {
        Slice<Object[]> sliceBrut = mouvementStockRepository.findCustomSorties(pageable, wrapper);

        return sliceBrut.map(ligne -> new SortieDto(
                (Integer) ligne[0],
                (String) ligne[1],
                (Integer) ligne[2],
                (String) ligne[3],
                (LocalDateTime) ligne[4],
                (String) ligne[5]));
    }

    @Transactional(readOnly = true)
    public Slice<EntreeDto> listEntrees(Pageable pageable, EntreeCriteriaWrapper wrapper) {
        Slice<Object[]> sliceBrut = mouvementStockRepository.findCustomEntrees(pageable, wrapper);

        return sliceBrut.map(ligne -> new EntreeDto(
                (Integer) ligne[0],
                (LocalDateTime) ligne[1],
                (String) ligne[2],
                (String) ligne[3],
                (Integer) ligne[4],
                (String) ligne[5]));
    }

    @Transactional(readOnly = true)
    public Slice<EtatStockDto> listEtatStock(Pageable pageable, EtatStockCriteriaWrapper wrapper) {
        Slice<Object[]> sliceBrut = mouvementStockRepository.findEtatStock(pageable, wrapper);

        return sliceBrut.map(ligne -> new EtatStockDto(
                (Integer) ligne[0],
                (String) ligne[1],
                (String) ligne[2],
                ((Number) ligne[3]).intValue(),
                ((Number) ligne[4]).intValue(),
                ((Number) ligne[5]).intValue()));
    }

    public List<SortieDto> listSortiesPourExport(SortieCriteriaWrapper wrapper, Pageable pageable) {
        Slice<Object[]> sliceBrut = mouvementStockRepository.findCustomSorties(pageable, wrapper);

        List<SortieDto> result = new ArrayList<>();
        for (Object[] ligne : sliceBrut.getContent()) {
            result.add(new SortieDto(
                    (Integer) ligne[0],
                    (String) ligne[1],
                    (Integer) ligne[2],
                    (String) ligne[3],
                    (LocalDateTime) ligne[4],
                    (String) ligne[5]));
        }
        return result;
    }

    // Statistiques---------------------

    public List<MouvementMensuelDTO> getMouvementsParMois() {
        LocalDateTime depuis = LocalDateTime.now().minusMonths(12);

        List<Object[]> entreesBrutes = mouvementStockRepository.sumEntreesParMois(depuis);
        List<Object[]> sortiesBrutes = mouvementStockRepository.sumSortiesParMois(depuis);

        Map<String, Integer> entreesParMois = new HashMap<>();
        for (Object[] ligne : entreesBrutes) {
            entreesParMois.put((String) ligne[0], ((Number) ligne[1]).intValue());
        }

        Map<String, Integer> sortiesParMois = new HashMap<>();
        for (Object[] ligne : sortiesBrutes) {
            sortiesParMois.put((String) ligne[0], ((Number) ligne[1]).intValue());
        }

        // Génère les 12 derniers mois dans l'ordre, même ceux sans mouvement (affiche
        // 0)
        List<MouvementMensuelDTO> result = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            String mois = LocalDate.now().minusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            int entree = entreesParMois.getOrDefault(mois, 0);
            int sortie = sortiesParMois.getOrDefault(mois, 0);
            result.add(new MouvementMensuelDTO(mois, entree, sortie));
        }
        return result;
    }
}