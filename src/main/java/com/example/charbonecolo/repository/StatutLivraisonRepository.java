package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.StatutLivraisonModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatutLivraisonRepository extends JpaRepository<StatutLivraisonModel, Integer> {

    List<StatutLivraisonModel> findByLivraisonIdOrderByDateStatutsLivraisonAsc(Integer livraisonId);
}
