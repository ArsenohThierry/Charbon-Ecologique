package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.model.MouvementStockModel;
import com.example.charbonecolo.model.TypeMouvementStockModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MouvementStockRepository extends JpaRepository<MouvementStockModel, Integer> {
        // Find all entries (where lotProduction is not null)
        List<MouvementStockModel> findByLotProductionIsNotNull();

        // Find all exits (where lotProduction is null)
        List<MouvementStockModel> findByLotProductionIsNull();

        // Find movements by type (entree/sortie)
        List<MouvementStockModel> findByTypeMouvement(TypeMouvementStockModel typeMouvement);

        // Find movements by lot production
        List<MouvementStockModel> findByLotProduction(LotProductionModel lotProduction);

        Optional<MouvementStockModel> findById(Integer id);

        Boolean existsEntreeByLotProduction(LotProductionModel lotProduction);

        @Query("""
                        SELECT COALESCE(SUM(m.quantite),0)
                        FROM MouvementStockModel m
                        WHERE m.lotProduction.id = :idLot
                        AND m.typeMouvement.libelle = 'Entree'
                        """)
        Integer sumEntreesByLot(Integer idLot);

        @Query("""
                        SELECT COALESCE(SUM(m.quantite), 0)
                        FROM MouvementStockModel m
                        WHERE m.lotProduction.produit.id = :idProduit
                        AND m.typeMouvement.libelle = 'Entree'
                        """)
        Integer sumEntreesByProduit(Integer idProduit);

        @Query("""
                        SELECT m.dateMouvement
                        FROM MouvementStockModel m
                        WHERE m.lotProduction = :lotProduction
                        AND m.typeMouvement.libelle = 'Entree'
                        """)
        LocalDateTime getDateEntreeByLotProduction(LotProductionModel lotProduction);
}