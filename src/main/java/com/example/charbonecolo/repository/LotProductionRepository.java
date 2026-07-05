package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.LotProductionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LotProductionRepository extends JpaRepository<LotProductionModel, Integer> {
    Optional<LotProductionModel> findByReference(String reference);

    // Recherche côté serveur pour la page "critères de recherche de lots".
    // Chaque critère est optionnel (NULL = ignoré). Le filtre sur le statut se base
    // sur le DERNIER statut connu du lot (le plus récent par date_statut), pour rester
    // cohérent avec LotProductionService.getLatestStatutsForAllLots().
    @Query(value = """
            SELECT lp.* FROM lot_production lp
            WHERE (CAST(:reference AS text) IS NULL OR lp.reference ILIKE CONCAT('%', CAST(:reference AS text), '%'))
              AND (CAST(:idProduit AS integer) IS NULL OR lp.id_produit = CAST(:idProduit AS integer))
              AND (CAST(:idTypeMatierePremiere AS integer) IS NULL OR lp.id_type_matiere_premiere = CAST(:idTypeMatierePremiere AS integer))
              AND (CAST(:dateDebut AS timestamp) IS NULL OR lp.date_entree_lot >= CAST(:dateDebut AS timestamp))
              AND (CAST(:dateFin AS timestamp) IS NULL OR lp.date_entree_lot <= CAST(:dateFin AS timestamp))
              AND (
                    CAST(:idLotStatut AS integer) IS NULL OR CAST(:idLotStatut AS integer) = (
                        SELECT slp.id_lot_statuts FROM statuts_lot_production slp
                        WHERE slp.id_lot_production = lp.id
                        ORDER BY slp.date_statut DESC
                        LIMIT 1
                    )
              )
            ORDER BY lp.date_entree_lot DESC
            """, nativeQuery = true)
    List<LotProductionModel> rechercher(
            @Param("reference") String reference,
            @Param("idProduit") Integer idProduit,
            @Param("idTypeMatierePremiere") Integer idTypeMatierePremiere,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin,
            @Param("idLotStatut") Integer idLotStatut
    );
}