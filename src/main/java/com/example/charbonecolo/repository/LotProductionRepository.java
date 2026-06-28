package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.LotProductionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LotProductionRepository extends JpaRepository<LotProductionModel, Integer> {

    Optional<LotProductionModel> findByReference(String reference);

    @Query("SELECT COALESCE(MAX(l.id), 0) + 1 FROM LotProductionModel l")
    long getNextId();
}