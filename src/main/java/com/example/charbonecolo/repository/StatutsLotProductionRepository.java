package com.example.charbonecolo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.model.StatutsLotProductionModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatutsLotProductionRepository extends JpaRepository<StatutsLotProductionModel, Integer> {
    @Query("SELECT s FROM StatutsLotProductionModel s WHERE s.lotProduction = ?1 AND s.dateFin IS NULL ORDER BY s.id DESC")
    List<StatutsLotProductionModel> findActiveByLotProduction(LotProductionModel lotProduction);

    Optional<StatutsLotProductionModel> findTopByLotProductionOrderByDateStatutDesc(LotProductionModel lotProduction);

    // Historique complet (utilisé pour afficher la date de fin de chaque statut sur la page de suivi)
    List<StatutsLotProductionModel> findByLotProductionOrderByDateStatutAsc(LotProductionModel lotProduction);

    @Query("""
                SELECT s.dateStatut
                FROM StatutsLotProductionModel s
                JOIN s.lotStatuts l
                WHERE l.libelle = 'Termine'
                  AND s.lotProduction.id = :lotProductionId
            """)
    Optional<LocalDateTime> findDateTermineByLotProductionId(
            @Param("lotProductionId") Integer lotProductionId);
}