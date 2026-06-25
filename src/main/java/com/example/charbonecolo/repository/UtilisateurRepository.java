package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.UtilisateurModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<UtilisateurModel, Integer> {
    Optional<UtilisateurModel> findByUsername(String username);
}
