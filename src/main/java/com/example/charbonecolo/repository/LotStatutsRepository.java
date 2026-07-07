package com.example.charbonecolo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.model.LotStatutsModel;
import java.util.List;
import java.util.Optional;

@Repository
public interface LotStatutsRepository extends JpaRepository<LotStatutsModel, Integer> {
    Optional<LotStatutsModel> findByLibelle(String libelle);

    // Liste ordonnée des statuts possibles (utilisée pour la page de suivi de production)
    List<LotStatutsModel> findAllByOrderByOrdreAsc();
}
