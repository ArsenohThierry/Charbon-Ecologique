package com.example.charbonecolo.service;

import com.example.charbonecolo.exception.BusinessException;
import com.example.charbonecolo.model.*;
import com.example.charbonecolo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;


import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.model.LotStatutsModel;
import com.example.charbonecolo.model.MotifSortieModel;
import com.example.charbonecolo.model.MouvementStockModel;
import com.example.charbonecolo.model.SeuilModel;
import com.example.charbonecolo.model.TypeMouvementStockModel;
import com.example.charbonecolo.repository.AlerteSeuilRepository;
import com.example.charbonecolo.repository.LotProductionRepository;
import com.example.charbonecolo.repository.LotStatutsRepository;
import com.example.charbonecolo.repository.MotifSortieRepository;
import com.example.charbonecolo.repository.MouvementStockRepository;
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

    private TypeMouvementStockModel sortieType = typeMouvementStockRepository.findByLibelleDirect("Sortie");
    private List<MouvementStockModel> sorties = mouvementStockRepository.findByTypeMouvement(sortieType);
    private TypeMouvementStockModel entreeType = typeMouvementStockRepository.findByLibelleDirect("Entree");
    private List<MouvementStockModel> entrees = mouvementStockRepository.findByTypeMouvement(entreeType);
    private List<SeuilModel> ruptures = seuilRepository.findByAlerteSeuil(alerteSeuilRepository.findByLibelle("Rupture"));
    private List<SeuilModel> faibles = seuilRepository.findByAlerteSeuil(alerteSeuilRepository.findByLibelle("Faible"));


    // ── Méthodes existantes ──────────────────────────────────────

    public Optional<MouvementStockModel> getMouvementStockById(Integer id) {
        return mouvementStockRepository.findById(id);
    }

    public List<MouvementStockModel> getAllMouvementsStock() {
        return mouvementStockRepository.findAll();
    }

    public List<LotProductionModel> getLotsTermines() {
        Optional<LotStatutsModel> termineOpt = lotStatutsRepository.findByLibelle("Termine");
        if (termineOpt.isEmpty()) return List.of();
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

    // ── ENTRÉE ───────────────────────────────────────────────────

    @Transactional
    public MouvementStockModel saveEntreeStock(Integer idLot, Integer quantite, LocalDate date) {
        LotProductionModel lot = lotProductionRepository.findById(idLot).orElseThrow();

        lot.setQuantiteRestante(
            (lot.getQuantiteRestante() != null ? lot.getQuantiteRestante() : 0) + quantite
        );
        lotProductionRepository.save(lot);

        MouvementStockModel mouvement = new MouvementStockModel();
        mouvement.setLotProduction(lot);
        mouvement.setQuantite(quantite);
        mouvement.setDateMouvement(date != null ? LocalDateTime.of(date, LocalTime.now()) : LocalDateTime.now());
        mouvement.setTypeMouvement(typeMouvementStockRepository.findById(1).orElseThrow());
        mouvement.setMotifSortie(null);
        return mouvementStockRepository.save(mouvement);
    }

    @Transactional
    public MouvementStockModel updateEntreeStock(Integer id, Integer idLot, Integer quantite, LocalDate date) {
        MouvementStockModel mouvement = mouvementStockRepository.findById(id).orElseThrow();
        LotProductionModel oldLot = mouvement.getLotProduction();
        LotProductionModel newLot = lotProductionRepository.findById(idLot).orElseThrow();

        int oldQte = mouvement.getQuantite();

        if (!oldLot.getId().equals(newLot.getId())) {
            oldLot.setQuantiteRestante(oldLot.getQuantiteRestante() - oldQte);
            lotProductionRepository.save(oldLot);
            newLot.setQuantiteRestante(
                (newLot.getQuantiteRestante() != null ? newLot.getQuantiteRestante() : 0) + quantite
            );
            lotProductionRepository.save(newLot);
        } else {
            int diff = quantite - oldQte;
            newLot.setQuantiteRestante(newLot.getQuantiteRestante() + diff);
            lotProductionRepository.save(newLot);
        }

        mouvement.setLotProduction(newLot);
        mouvement.setQuantite(quantite);
        mouvement.setDateMouvement(date != null ? LocalDateTime.of(date, LocalTime.now()) : LocalDateTime.now());
        return mouvementStockRepository.save(mouvement);
    }

    // ── SORTIE (FIFO) ────────────────────────────────────────────

    @Transactional
    public MouvementStockModel saveSortieStock(Integer idProduit, Integer quantite, Integer idMotif, LocalDate date) {
        List<LotProductionModel> lotsDisponibles = lotProductionRepository
                .findLotsWithStockByProduitOrderByDateAsc(idProduit);

        int totalDisponible = lotsDisponibles.stream()
                .mapToInt(l -> l.getQuantiteRestante() != null ? l.getQuantiteRestante() : 0)
                .sum();

        if (totalDisponible < quantite) {
            throw new BusinessException(
                "Stock insuffisant pour ce produit. Disponible: " + totalDisponible + ", demandé: " + quantite
            );
        }

        MouvementStockModel mouvement = new MouvementStockModel();
        mouvement.setLotProduction(null);
        mouvement.setQuantite(quantite);
        mouvement.setDateMouvement(date != null ? LocalDateTime.of(date, LocalTime.now()) : LocalDateTime.now());
        mouvement.setTypeMouvement(typeMouvementStockRepository.findById(2).orElseThrow());
        mouvement.setMotifSortie(motifSortieRepository.findById(idMotif).orElseThrow());
        mouvement = mouvementStockRepository.save(mouvement);

        int resteASortir = quantite;
        List<MouvementSortieDetailModel> details = new ArrayList<>();

        for (LotProductionModel lot : lotsDisponibles) {
            if (resteASortir <= 0) break;

            int dispo = lot.getQuantiteRestante();
            int pris = Math.min(dispo, resteASortir);

            lot.setQuantiteRestante(dispo - pris);
            lotProductionRepository.save(lot);

            MouvementSortieDetailModel detail = new MouvementSortieDetailModel();
            detail.setMouvementSortie(mouvement);
            detail.setLotProduction(lot);
            detail.setQuantite(pris);
            details.add(detail);

            resteASortir -= pris;
        }

        mouvementSortieDetailRepository.saveAll(details);

        return mouvement;
    }

    @Transactional
    public MouvementStockModel updateSortieStock(Integer id, Integer quantite, Integer idMotif, LocalDate date) {
        MouvementStockModel mouvement = mouvementStockRepository.findById(id).orElseThrow();

        List<MouvementSortieDetailModel> oldDetails = mouvementSortieDetailRepository.findByMouvementSortie(mouvement);

        for (MouvementSortieDetailModel detail : oldDetails) {
            LotProductionModel lot = detail.getLotProduction();
            lot.setQuantiteRestante(lot.getQuantiteRestante() + detail.getQuantite());
            lotProductionRepository.save(lot);
        }

        mouvementSortieDetailRepository.deleteByMouvementSortie(mouvement);

        Integer idProduit = oldDetails.isEmpty() ? null : oldDetails.get(0).getLotProduction().getProduit().getId();

        if (idProduit == null) {
            throw new IllegalStateException("Impossible de déterminer le produit de la sortie");
        }

        mouvement.setQuantite(quantite);
        mouvement.setMotifSortie(motifSortieRepository.findById(idMotif).orElseThrow());
        mouvement.setDateMouvement(date != null ? LocalDateTime.of(date, LocalTime.now()) : LocalDateTime.now());
        mouvementStockRepository.save(mouvement);

        List<LotProductionModel> lotsDisponibles = lotProductionRepository
                .findLotsWithStockByProduitOrderByDateAsc(idProduit);

        int totalDisponible = lotsDisponibles.stream()
                .mapToInt(l -> l.getQuantiteRestante() != null ? l.getQuantiteRestante() : 0)
                .sum();

        if (totalDisponible < quantite) {
            throw new BusinessException(
                "Stock insuffisant après restauration. Disponible: " + totalDisponible + ", demandé: " + quantite
            );
        }

        int resteASortir = quantite;
        List<MouvementSortieDetailModel> newDetails = new ArrayList<>();

        for (LotProductionModel lot : lotsDisponibles) {
            if (resteASortir <= 0) break;

            int dispo = lot.getQuantiteRestante();
            int pris = Math.min(dispo, resteASortir);

            lot.setQuantiteRestante(dispo - pris);
            lotProductionRepository.save(lot);

            MouvementSortieDetailModel detail = new MouvementSortieDetailModel();
            detail.setMouvementSortie(mouvement);
            detail.setLotProduction(lot);
            detail.setQuantite(pris);
            newDetails.add(detail);

            resteASortir -= pris;
        }

        mouvementSortieDetailRepository.saveAll(newDetails);

        return mouvement;
    }

    // ── SUPPRESSION ──────────────────────────────────────────────

    @Transactional
    public void deleteMouvementStock(Integer id) {
        MouvementStockModel mouvement = mouvementStockRepository.findById(id).orElseThrow();
        boolean isEntree = mouvement.getTypeMouvement().getId() == 1;

        if (isEntree) {
            LotProductionModel lot = mouvement.getLotProduction();
            if (lot != null) {
                int nouveauStock = lot.getQuantiteRestante() - mouvement.getQuantite();
                if (nouveauStock < 0) {
                    throw new BusinessException(
                        "Impossible de supprimer cette entrée : le stock du lot " + lot.getReference()
                        + " a déjà été consommé par des sorties. Supprimez d'abord les sorties concernées."
                    );
                }
                lot.setQuantiteRestante(nouveauStock);
                lotProductionRepository.save(lot);
            }
        } else {
            List<MouvementSortieDetailModel> details = mouvementSortieDetailRepository.findByMouvementSortie(mouvement);
            for (MouvementSortieDetailModel detail : details) {
                LotProductionModel lot = detail.getLotProduction();
                lot.setQuantiteRestante(lot.getQuantiteRestante() + detail.getQuantite());
                lotProductionRepository.save(lot);
            }
            mouvementSortieDetailRepository.deleteByMouvementSortie(mouvement);
        }

        mouvementStockRepository.deleteById(id);
    }

    // ── ÉTAT DU STOCK ────────────────────────────────────────────

    public List<LotProductionModel> getLotsWithStockByProduit(Integer idProduit) {
        return lotProductionRepository.findLotsWithStockByProduitOrderByDateAsc(idProduit);
    }

    public List<MouvementSortieDetailModel> getDetailsByMouvement(MouvementStockModel mouvement) {
        return mouvementSortieDetailRepository.findByMouvementSortie(mouvement);
    }



        //calcul total sortie stock
    public double getTotalSortieStock() {
        return sorties.stream().mapToDouble(MouvementStockModel::getQuantite).sum();
    }

    //calcul total entree stock
    public double getTotalEntreeStock() {
        return entrees.stream().mapToDouble(MouvementStockModel::getQuantite).sum();
    }

    //calcul total stock
    public double getTotalStock() {
        return getTotalEntreeStock() - getTotalSortieStock();
    }

    //calcul sortie par lot
    public double getTotalSortieStockByLot(Integer idLot) {
        return sorties.stream()
                .filter(m -> m.getLotProduction() != null && m.getLotProduction().getId().equals(idLot))
                .mapToDouble(MouvementStockModel::getQuantite)
                .sum();
    }

    //calcul entree par lot
    public double getTotalEntreeStockByLot(Integer idLot) {
        return entrees.stream()
                .filter(m -> m.getLotProduction() != null && m.getLotProduction().getId().equals(idLot))
                .mapToDouble(MouvementStockModel::getQuantite)
                .sum();
    }

    //calcul stock par lot
    public double getTotalStockByLot(Integer idLot) {
        return getTotalEntreeStockByLot(idLot) - getTotalSortieStockByLot(idLot);
    }

    //calcul total alerte rupture
    public Integer getTotalAlertRupture() {
        return ruptures.size();
    }

    //calcul total alerte faible
    public Integer getTotalAlertFaible() {
        return faibles.size();
    }







}
