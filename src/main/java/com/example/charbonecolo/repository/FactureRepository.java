package com.example.charbonecolo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.charbonecolo.model.FactureModel;

public interface FactureRepository extends JpaRepository<FactureModel, Integer> {
    public Optional<FactureModel> findByPaiementId(Integer idPaiement);
}
