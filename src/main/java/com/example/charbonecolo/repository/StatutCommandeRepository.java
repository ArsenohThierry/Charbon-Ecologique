package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.StatutCommandeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatutCommandeRepository extends JpaRepository<StatutCommandeModel, Integer> {

    /**
     * Récupère tout l'historique des statuts d'une commande, du plus ancien au plus récent.
     */
    List<StatutCommandeModel> findByCommandeIdOrderByDateStatutCommandeAsc(Integer commandeId);

    /**
     * Récupère le tout dernier statut (actuel) d'une commande donnée.
     */
    @Query("SELECT s FROM StatutCommandeModel s WHERE s.commande.id = :commandeId ORDER BY s.dateStatutCommande DESC LIMIT 1")
    Optional<StatutCommandeModel> findCurrentStatutByCommandeId(@Param("commandeId") Integer commandeId);

    Optional<StatutCommandeModel> findFirstByCommandeIdOrderByDateStatutCommandeDesc(Integer idCommande);

    List<StatutCommandeModel> findAllByCommandeIdInOrderByDateStatutCommandeDesc(List<Integer> ids);

    @Query("""
            SELECT s FROM StatutCommandeModel s WHERE s.commande.id = :idCommande 
            ORDER BY s.dateStatutCommande DESC LIMIT 1
            """)
    StatutCommandeModel getLastStatutOf(@Param("idCommande") Integer idCommande);

}