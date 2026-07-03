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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LotProductionModel l " +
           "WHERE l.produit.id = :idProduit " +
           "AND (l.quantiteRestante IS NOT NULL AND l.quantiteRestante > 0) " +
           "ORDER BY l.dateEntreeLot ASC")
    List<LotProductionModel> findLotsWithStockByProduitOrderByDateAsc(@Param("idProduit") Integer idProduit);
}