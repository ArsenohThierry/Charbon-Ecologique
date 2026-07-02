package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.TypeMouvementStockModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypeMouvementStockRepository extends JpaRepository<TypeMouvementStockModel, Integer> {
    Optional<TypeMouvementStockModel> findByLibelle(String libelle);
    TypeMouvementStockModel findByLibelleDirect(String libelle);
}
