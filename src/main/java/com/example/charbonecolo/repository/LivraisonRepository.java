package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.LivraisonModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LivraisonRepository extends JpaRepository<LivraisonModel, Integer> {
}