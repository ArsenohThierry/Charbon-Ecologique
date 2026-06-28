package com.example.charbonecolo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.charbonecolo.model.ProduitModel;

public interface ProduitRepository extends JpaRepository<ProduitModel, Integer> {
    
}
