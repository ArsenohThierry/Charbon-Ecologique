package com.example.charbonecolo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.model.FournisseurModel;

@Repository
public interface FournisseurRepository extends JpaRepository<FournisseurModel, Integer> {
    
}
