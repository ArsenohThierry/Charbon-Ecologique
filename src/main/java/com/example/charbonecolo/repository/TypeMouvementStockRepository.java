package com.example.charbonecolo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.model.TypeMouvementStockModel;

@Repository
public interface TypeMouvementStockRepository extends JpaRepository<TypeMouvementStockModel, Integer> {
    TypeMouvementStockModel findByLibelle(String libelle);
}
