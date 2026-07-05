package com.example.charbonecolo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.model.StatutsLotProductionModel;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatutsLotProductionRepository extends JpaRepository<StatutsLotProductionModel, Integer> {
    Optional<StatutsLotProductionModel> findTopByLotProductionOrderByDateStatutDesc(LotProductionModel lotProduction);

    // Historique complet (utilisé pour afficher la date de fin de chaque statut sur la page de suivi)
    List<StatutsLotProductionModel> findByLotProductionOrderByDateStatutAsc(LotProductionModel lotProduction);
}