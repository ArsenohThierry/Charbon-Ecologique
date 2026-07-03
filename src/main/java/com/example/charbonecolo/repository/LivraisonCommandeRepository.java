package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.LivraisonCommandeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivraisonCommandeRepository extends JpaRepository<LivraisonCommandeModel, Integer> {

    List<LivraisonCommandeModel> findByIdLivraison(Integer idLivraison);

    void deleteByIdLivraison(Integer idLivraison);
}
