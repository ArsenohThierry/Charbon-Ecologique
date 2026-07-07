package com.example.charbonecolo.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.dto.LivraisonCriteriaWrapper;
import com.example.charbonecolo.model.LivraisonModel;

@Repository
public interface LivraisonRepository extends JpaRepository<LivraisonModel, Integer> {

    @Query(value = """
        SELECT
            l.id,
            l.reference,
            l.date_livraison,
            l.lieu,
            lvr.nom AS livreur_nom,
            dernier_statut.libelle AS statut_libelle,
            cmd.reference AS commande_reference,
            cmd.id AS commande_id
        FROM livraison l
        LEFT JOIN livreurs lvr ON lvr.id = l.id_livreur
        LEFT JOIN commandes cmd ON cmd.id = l.id_commande
        LEFT JOIN (
            SELECT DISTINCT ON (sl.id_livraison)
                sl.id_livraison,
                ls.libelle
            FROM statuts_livraisons sl
            JOIN livraison_statuts ls ON ls.id = sl.id_livraisons_statuts
            ORDER BY sl.id_livraison, sl.date_statuts_livraison DESC
        ) dernier_statut ON dernier_statut.id_livraison = l.id
        WHERE 1=1
        AND (CAST(:#{#cri.dateLivMin} AS date) IS NULL OR CAST(l.date_livraison AS date) >= CAST(:#{#cri.dateLivMin} AS date))
        AND (CAST(:#{#cri.dateLivMax} AS date) IS NULL OR CAST(l.date_livraison AS date) <= CAST(:#{#cri.dateLivMax} AS date))
        AND (CAST(:#{#cri.statut} AS integer) IS NULL OR dernier_statut.libelle = (SELECT ls2.libelle FROM livraison_statuts ls2 WHERE ls2.id = CAST(:#{#cri.statut} AS integer)))
        AND (CAST(:#{#cri.reference} AS text) IS NULL OR CAST(:#{#cri.reference} AS text) = '' OR l.reference ILIKE CONCAT('%', CAST(:#{#cri.reference} AS text), '%'))
        """, nativeQuery = true)
    Slice<Object[]> findLivraisonsFiltrees(
            Pageable pageable,
            @Param("cri") LivraisonCriteriaWrapper wrapper);

    @Query(value = """
        SELECT
            l.id,
            l.reference,
            cmd.id AS commande_id,
            cmd.reference AS commande_reference
        FROM livraison l
        LEFT JOIN commandes cmd ON cmd.id = l.id_commande
        LEFT JOIN (
            SELECT DISTINCT ON (sl.id_livraison)
                sl.id_livraison,
                ls.libelle
            FROM statuts_livraisons sl
            JOIN livraison_statuts ls ON ls.id = sl.id_livraisons_statuts
            ORDER BY sl.id_livraison, sl.date_statuts_livraison DESC
        ) dernier_statut ON dernier_statut.id_livraison = l.id
        WHERE dernier_statut.libelle = 'Annulé'
        ORDER BY l.date_livraison DESC
        """, nativeQuery = true)
    List<Object[]> findLivraisonsAnnulees();

    @Query("SELECT COUNT(l) > 0 FROM LivraisonModel l WHERE l.commande.id = :commandeId")
    boolean existsByCommandeId(@Param("commandeId") Integer commandeId);
}