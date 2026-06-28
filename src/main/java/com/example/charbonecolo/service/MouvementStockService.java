package com.example.charbonecolo.service;

import com.example.charbonecolo.model.*;
import com.example.charbonecolo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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
        return lotProductionRepository.findAll();
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
}