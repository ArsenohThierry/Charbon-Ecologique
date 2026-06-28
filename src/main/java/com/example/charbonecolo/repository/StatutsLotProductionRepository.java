package com.example.charbonecolo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.model.StatutsLotProductionModel;

@Repository
public interface StatutsLotProductionRepository extends JpaRepository<StatutsLotProductionModel, Integer> {}