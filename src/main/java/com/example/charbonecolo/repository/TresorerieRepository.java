package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.TresorerieModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;

public interface TresorerieRepository extends JpaRepository<TresorerieModel, Integer> {

    @Query("SELECT SUM(CASE WHEN t.typeOperation = 'ENTREE' " +
           "THEN t.montant ELSE -t.montant END) FROM TresorerieModel t")
    BigDecimal calculerSolde();
}