package com.example.charbonecolo.service;

import com.example.charbonecolo.model.MouvementStockModel;
import com.example.charbonecolo.repository.MouvementStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MouvementStockService {

    @Autowired
    private MouvementStockRepository mouvementStockRepository;

    public MouvementStockModel saveMouvementStock(MouvementStockModel mouvementStock) {
        return mouvementStockRepository.save(mouvementStock);
    }

    public List<MouvementStockModel> getAllMouvementsStock() {
        return mouvementStockRepository.findAll();
    }

    public Optional<MouvementStockModel> getMouvementStockById(Integer id) {
        return mouvementStockRepository.findById(id);
    }

    public void deleteMouvementStock(Integer id) {
        mouvementStockRepository.deleteById(id);
    }
}