package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.LotProductionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LotProductionRepository extends JpaRepository<LotProductionModel, Integer> {
    Optional<LotProductionModel> findByReference(String reference);
}