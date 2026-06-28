package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.OrigineModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrigineRepository extends JpaRepository<OrigineModel, Integer> {
    Optional<OrigineModel> findByLibelle(String libelle);
}