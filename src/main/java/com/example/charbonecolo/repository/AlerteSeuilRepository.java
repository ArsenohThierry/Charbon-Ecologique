package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.AlerteSeuilModel;
import com.example.charbonecolo.model.ProduitModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlerteSeuilRepository extends JpaRepository<AlerteSeuilModel, Integer> {
    AlerteSeuilModel findByLibelle(String libelle);
}