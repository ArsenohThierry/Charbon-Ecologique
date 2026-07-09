package com.example.charbonecolo.repository;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.dto.FactureCriteriaWrapper;
import com.example.charbonecolo.model.PaiementModel;

@Repository
public interface PaiementRepository extends JpaRepository<PaiementModel, Integer> {

    @Query(value = """
        SELECT
            c.id,
            c.reference,
            c.date_commande,
            cli.nom AS client_nom,
            COALESCE(total.montant_total, 0) AS montant_total,
            COALESCE(sp.libelle, 'Non payée') AS statut_paiement
        FROM commandes c
        JOIN clients cli ON cli.id = c.id_client
        LEFT JOIN (
            SELECT id_commande, SUM(montant) AS montant_total
            FROM detail_commande
            GROUP BY id_commande
        ) total ON total.id_commande = c.id
        LEFT JOIN (
            SELECT DISTINCT ON (p.id_commande)
                p.id_commande,
                ps.libelle
            FROM paiement p
            JOIN statuts_paiements sp ON sp.id_paiement = p.id
            JOIN paiement_statuts ps ON ps.id = sp.id_statut_paiement
            ORDER BY p.id_commande, sp.date_statut DESC
        ) sp ON sp.id_commande = c.id
        WHERE c.deleted_at IS NULL
        AND (CAST(:#{#cri.filtre} AS text) IS NULL OR CAST(:#{#cri.filtre} AS text) = 'Toutes' OR COALESCE(sp.libelle, 'Non payée') = CAST(:#{#cri.filtre} AS text))
        """, nativeQuery = true)
    Slice<Object[]> findCommandesFiltrees(
            Pageable pageable,
            @Param("cri") FactureCriteriaWrapper cri);

    public Optional<PaiementModel> findByCommandeId(Integer idCommande);
}
