package com.example.charbonecolo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.dto.CommandeDto;
import com.example.charbonecolo.model.CommandeModel;

import jakarta.persistence.NamedNativeQuery;

@Repository
public interface CommandeRepository extends JpaRepository<CommandeModel, Integer> {
    // Maka anle liste par pagination
    @Query(value = """
                           SELECT
                c.id,
                c.reference,
                c.date_commande,
                c.id_client,
                cli.nom AS client_nom,
                total_cmd.montant_total,
                dernier_statut.id_commande_statuts,
                dernier_statut.libelle AS statut_libelle
            FROM commandes AS c
            JOIN clients AS cli ON cli.id = c.id_client
            JOIN (
                SELECT id_commande, SUM(p.pu * dc.quantite) AS montant_total
                FROM detail_commande dc
                JOIN produit p ON p.id = dc.id_produit
                GROUP BY id_commande
            ) total_cmd ON total_cmd.id_commande = c.id
            JOIN (
                SELECT DISTINCT ON (id_commandes) id_commandes, id_commande_statuts, cs.libelle
                FROM statuts_commandes sc
                JOIN commande_statuts cs ON cs.id = sc.id_commande_statuts
                ORDER BY id_commandes, date_statut_commande DESC
            ) dernier_statut ON dernier_statut.id_commandes = c.id
                        """, countQuery = "SELECT COUNT(*) FROM commandes", 
                    nativeQuery = true)
    Page<Object[]> findCustomCommandes(Pageable pageable);
}
