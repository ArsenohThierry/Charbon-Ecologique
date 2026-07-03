package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.LotProductionModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LotProductionRepository extends JpaRepository<LotProductionModel, Integer> {
    Optional<LotProductionModel> findByReference(String reference);

    @Query("""
            SELECT l
            FROM LotProductionModel l
            JOIN MouvementStockModel m
                ON m.lotProduction = l
            WHERE l.produit.id = :idProduit
              AND m.typeMouvement.id = 1
            ORDER BY m.dateMouvement ASC
            """)
    List<LotProductionModel> findLotsWithStockByProduitOrderByDateAsc(
            @Param("idProduit") Integer idProduit);
}