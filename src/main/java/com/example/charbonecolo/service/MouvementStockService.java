package com.example.charbonecolo.service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.model.LotStatutsModel;
import com.example.charbonecolo.model.MotifSortieModel;
import com.example.charbonecolo.model.MouvementStockModel;
import com.example.charbonecolo.model.TypeMouvementStockModel;
import com.example.charbonecolo.repository.LotProductionRepository;
import com.example.charbonecolo.repository.LotStatutsRepository;
import com.example.charbonecolo.repository.MotifSortieRepository;
import com.example.charbonecolo.repository.MouvementStockRepository;
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
    private StatutsLotProductionRepository statutsLotProductionRepository;
    @Autowired
    private LotStatutsRepository lotStatutsRepository;
    @Autowired
    private SeuilRepository seuilRepository;

    private TypeMouvementStockModel sortieType = typeMouvementStockRepository.findByLibelle("Sortie");
    private List<MouvementStockModel> sorties = mouvementStockRepository.findByTypeMouvement(sortieType);
    private TypeMouvementStockModel entreeType = typeMouvementStockRepository.findByLibelle("Entree");
    private List<MouvementStockModel> entrees = mouvementStockRepository.findByTypeMouvement(entreeType);
    private List<SeuilModel> ruptures = seuilRepository.findByAlerteSeuil(alerteSeuilRepository.findByLibelle("Rupture"));
    private List<SeuilModel> faibles = seuilRepository.findByAlerteSeuil(alerteSeuilRepository.findByLibelle("Faible"));



    // ── Méthodes existantes ──────────────────────────────────────

    public Optional<MouvementStockModel> getMouvementStockById(Integer id) {
        return mouvementStockRepository.findById(id);
    }

    public void deleteMouvementStock(Integer id) {
        mouvementStockRepository.deleteById(id);
    }

    public List<MouvementStockModel> getAllMouvementsStock() {
        return mouvementStockRepository.findAll();
    }

    // ── Nouvelles méthodes ───────────────────────────────────────

    // Pour alimenter le <select> des lots dans le formulaire d'entrée
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

    // Enregistrer une entrée stock
    public MouvementStockModel saveEntreeStock(Integer idLot, Integer quantite, LocalDate date) {
        MouvementStockModel mouvement = new MouvementStockModel();
        mouvement.setLotProduction(lotProductionRepository.findById(idLot).orElseThrow());
        mouvement.setQuantite(quantite);
        mouvement.setDateMouvement(date != null ? LocalDateTime.of(date, LocalTime.now()) : LocalDateTime.now());
        mouvement.setTypeMouvement(typeMouvementStockRepository.findById(1).orElseThrow());
        mouvement.setMotifSortie(null);
        return mouvementStockRepository.save(mouvement);
    }

    // Modifier une entrée stock
    public MouvementStockModel updateEntreeStock(Integer id, Integer idLot, Integer quantite, LocalDate date) {
        MouvementStockModel mouvement = mouvementStockRepository.findById(id).orElseThrow();
        mouvement.setLotProduction(lotProductionRepository.findById(idLot).orElseThrow());
        mouvement.setQuantite(quantite);
        mouvement.setDateMouvement(date != null ? LocalDateTime.of(date, LocalTime.now()) : LocalDateTime.now());
        return mouvementStockRepository.save(mouvement);
    }

    // Enregistrer une sortie stock
    public MouvementStockModel saveSortieStock(Integer quantite, Integer idMotif, LocalDate date) {
        MouvementStockModel mouvement = new MouvementStockModel();
        mouvement.setLotProduction(null);
        mouvement.setQuantite(quantite);
        mouvement.setDateMouvement(date != null ? LocalDateTime.of(date, LocalTime.now()) : LocalDateTime.now());
        mouvement.setTypeMouvement(typeMouvementStockRepository.findById(2).orElseThrow());
        mouvement.setMotifSortie(motifSortieRepository.findById(idMotif).orElseThrow());
        return mouvementStockRepository.save(mouvement);
    }

    // Modifier une sortie stock
    public MouvementStockModel updateSortieStock(Integer id, Integer quantite, Integer idMotif, LocalDate date) {
        MouvementStockModel mouvement = mouvementStockRepository.findById(id).orElseThrow();
        mouvement.setQuantite(quantite);
        mouvement.setMotifSortie(motifSortieRepository.findById(idMotif).orElseThrow());
        mouvement.setDateMouvement(date != null ? LocalDateTime.of(date, LocalTime.now()) : LocalDateTime.now());
        return mouvementStockRepository.save(mouvement);
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