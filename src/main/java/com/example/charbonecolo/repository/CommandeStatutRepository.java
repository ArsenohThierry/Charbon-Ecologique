package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.CommandeStatutModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CommandeStatutRepository extends JpaRepository<CommandeStatutModel, Integer> {

    /**
     * Permet de récupérer un statut spécifique par son libellé exact.
     * Utile pour les assignations lors de la création ou modification d'une commande.
     */
    Optional<CommandeStatutModel> findByLibelle(String libelle);
    
    /**
     * Permet de vérifier si un statut existe déjà en base.
     */
    boolean existsByLibelle(String libelle);
}