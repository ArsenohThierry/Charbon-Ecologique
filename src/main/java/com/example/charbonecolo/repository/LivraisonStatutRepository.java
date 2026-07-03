package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.LivraisonStatutModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LivraisonStatutRepository extends JpaRepository<LivraisonStatutModel, Integer> {
}
