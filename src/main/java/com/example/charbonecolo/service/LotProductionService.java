package com.example.charbonecolo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
