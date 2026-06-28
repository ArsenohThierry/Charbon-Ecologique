package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.TresorerieModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TresorerieRepository extends JpaRepository<TresorerieModel, Long> {

    List<TresorerieModel> findAllByOrderByDateMouvementDesc();

    // Dernier solde enregistré
    @Query("SELECT t.solde FROM TresorerieModel t ORDER BY t.dateMouvement DESC LIMIT 1")
    Optional<BigDecimal> calculerSolde();

    List<TresorerieModel> findByTypeOrderByDateMouvementDesc(String type);
}
