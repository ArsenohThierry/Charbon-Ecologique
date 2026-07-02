package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.AlerteSeuilModel;
import com.example.charbonecolo.model.ProduitModel;
import com.example.charbonecolo.model.SeuilModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeuilRepository extends JpaRepository<SeuilModel, Integer> {
    List<SeuilModel> findByAlerteSeuil(AlerteSeuilModel alerteSeuil);
    
}