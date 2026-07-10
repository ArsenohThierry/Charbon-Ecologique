package com.example.charbonecolo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.charbonecolo.model.LivraisonResteModel;

public interface LivraisonResteRepository extends JpaRepository<LivraisonResteModel, Integer> {
    Optional<LivraisonResteModel> findByProduitIdAndLivraisonId(Integer idProduit, Integer idLivraison);
    List<LivraisonResteModel> findByLivraisonId(Integer id);
}
