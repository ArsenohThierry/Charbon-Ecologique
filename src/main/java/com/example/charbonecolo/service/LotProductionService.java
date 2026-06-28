package com.example.charbonecolo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.repository.LotProductionRepository;

@Service
public class LotProductionService {

    private final LotProductionRepository lotProductionRepository;

    public LotProductionService(LotProductionRepository lotProductionRepository) {
        this.lotProductionRepository = lotProductionRepository;
    }

    public LotProductionModel saveLotProduction(LotProductionModel lotProduction) {
        lotProduction.setReference(genererReference()); 
        return lotProductionRepository.save(lotProduction);
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
