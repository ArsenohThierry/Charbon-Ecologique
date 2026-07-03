package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.MotifSortieModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MotifSortieRepository extends JpaRepository<MotifSortieModel, Integer> {
    Optional<MotifSortieModel> findByLibelle(String libelle);
}
