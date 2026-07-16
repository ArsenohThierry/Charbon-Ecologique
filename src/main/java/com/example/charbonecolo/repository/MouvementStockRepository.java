package com.example.charbonecolo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.dto.EntreeCriteriaWrapper;
import com.example.charbonecolo.dto.EtatStockCriteriaWrapper;
import com.example.charbonecolo.dto.SortieCriteriaWrapper;
import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.model.MouvementStockModel;
import com.example.charbonecolo.model.TypeMouvementStockModel;

@Repository
public interface MouvementStockRepository extends JpaRepository<MouvementStockModel, Integer> {
    // Find all entries (where lotProduction is not null)
    List<MouvementStockModel> findByLotProductionIsNotNull();

    // Find all exits (where lotProduction is null)
    List<MouvementStockModel> findByLotProductionIsNull();

    // Find movements by type (entree/sortie)
    List<MouvementStockModel> findByTypeMouvement(TypeMouvementStockModel typeMouvement);

    // Find movements by lot production
    List<MouvementStockModel> findByLotProduction(LotProductionModel lotProduction);

    Optional<MouvementStockModel> findById(Integer id);

    Boolean existsEntreeByLotProduction(LotProductionModel lotProduction);

    @Query("""
            SELECT COALESCE(SUM(m.quantite),0)
            FROM MouvementStockModel m
            WHERE m.lotProduction.id = :idLot
            AND m.typeMouvement.libelle = 'Entree'
            """)
    Integer sumEntreesByLot(Integer idLot);

    @Query("""
            SELECT COALESCE(SUM(m.quantite), 0)
            FROM MouvementStockModel m
            WHERE m.lotProduction.produit.id = :idProduit
            AND m.typeMouvement.libelle = 'Entree'
            """)
    Integer sumEntreesByProduit(Integer idProduit);

    @Query("""
            SELECT m.dateMouvement
            FROM MouvementStockModel m
            WHERE m.lotProduction = :lotProduction
            AND m.typeMouvement.libelle = 'Entree'
            """)
    LocalDateTime getDateEntreeByLotProduction(LotProductionModel lotProduction);

    @Query(value = """
            SELECT
                m.id,
                p.nom AS produit_nom,
                m.quantite,
                mo.libelle AS motif_libelle,
                m.date_mouvement,
                STRING_AGG(lp.reference || ': ' || d.quantite || ' u.', '; ') AS lots_consommes
            FROM mouvement_stock m
            JOIN type_mouvement_stock t ON t.id = m.id_type_mouvement
            LEFT JOIN motif_sortie mo ON mo.id = m.id_motif_sortie
            JOIN mouvement_sortie_detail d ON d.id_mouvement_sortie = m.id
            JOIN lot_production lp ON lp.id = d.id_lot_production
            JOIN produit p ON p.id = lp.id_produit
            WHERE t.libelle = 'Sortie'
              AND m.date_suppression IS NULL
              AND (CAST(:#{#cri.idProduit} AS integer) IS NULL OR p.id = CAST(:#{#cri.idProduit} AS integer))
              AND (CAST(:#{#cri.idMotif} AS integer) IS NULL OR mo.id = CAST(:#{#cri.idMotif} AS integer))
              AND (CAST(:#{#cri.dateMin} AS date) IS NULL OR CAST(m.date_mouvement AS date) >= CAST(:#{#cri.dateMin} AS date))
              AND (CAST(:#{#cri.dateMax} AS date) IS NULL OR CAST(m.date_mouvement AS date) <= CAST(:#{#cri.dateMax} AS date))
            GROUP BY m.id, p.nom, m.quantite, mo.libelle, m.date_mouvement
            """, nativeQuery = true)
    Slice<Object[]> findCustomSorties(Pageable pageable, @Param("cri") SortieCriteriaWrapper wrapper);

    @Query(value = """
            SELECT
                m.id,
                m.date_mouvement,
                lp.reference AS lot_reference,
                tmp.libelle AS matiere_libelle,
                m.quantite,
                f.nom AS fournisseur_nom
            FROM mouvement_stock m
            JOIN type_mouvement_stock t ON t.id = m.id_type_mouvement
            JOIN lot_production lp ON lp.id = m.id_lot_production
            JOIN type_matiere_premiere tmp ON tmp.id = lp.id_type_matiere_premiere
            JOIN fournisseur f ON f.id = tmp.id_fournisseur
            WHERE t.libelle = 'Entree'
              AND m.date_suppression IS NULL
              AND (CAST(:#{#cri.idProduit} AS integer) IS NULL OR lp.id_produit = CAST(:#{#cri.idProduit} AS integer))
              AND (CAST(:#{#cri.dateMin} AS date) IS NULL OR CAST(m.date_mouvement AS date) >= CAST(:#{#cri.dateMin} AS date))
              AND (CAST(:#{#cri.dateMax} AS date) IS NULL OR CAST(m.date_mouvement AS date) <= CAST(:#{#cri.dateMax} AS date))
            """, nativeQuery = true)
    Slice<Object[]> findCustomEntrees(Pageable pageable, @Param("cri") EntreeCriteriaWrapper wrapper);

    @Query(value = """
            SELECT
                lp.id,
                p.nom AS produit_nom,
                lp.reference,
                COALESCE(entrees.total, 0) AS total_entree,
                COALESCE(sorties.total, 0) AS total_sortie,
                COALESCE(entrees.total, 0) - COALESCE(sorties.total, 0) AS restant
            FROM lot_production lp
            JOIN produit p ON p.id = lp.id_produit
            JOIN (
                SELECT m.id_lot_production, SUM(m.quantite) AS total
                FROM mouvement_stock m
                JOIN type_mouvement_stock t ON t.id = m.id_type_mouvement
                WHERE t.libelle = 'Entree'
                  AND m.date_suppression IS NULL
                GROUP BY m.id_lot_production
            ) entrees ON entrees.id_lot_production = lp.id
            LEFT JOIN (
                SELECT d.id_lot_production, SUM(d.quantite) AS total
                FROM mouvement_sortie_detail d
                GROUP BY d.id_lot_production
            ) sorties ON sorties.id_lot_production = lp.id
            WHERE lp.date_suppression IS NULL
              AND (CAST(:#{#cri.idProduit} AS integer) IS NULL OR p.id = CAST(:#{#cri.idProduit} AS integer))
              AND (CAST(:#{#cri.dateMin} AS date) IS NULL OR CAST(lp.date_entree_lot AS date) >= CAST(:#{#cri.dateMin} AS date))
              AND (CAST(:#{#cri.dateMax} AS date) IS NULL OR CAST(lp.date_entree_lot AS date) <= CAST(:#{#cri.dateMax} AS date))
            """, nativeQuery = true)
    Slice<Object[]> findEtatStock(Pageable pageable, @Param("cri") EtatStockCriteriaWrapper wrapper);

    @Query(value = """
            SELECT TO_CHAR(m.date_mouvement, 'YYYY-MM') AS mois, COALESCE(SUM(m.quantite), 0) AS total
            FROM mouvement_stock m
            JOIN type_mouvement_stock t ON t.id = m.id_type_mouvement
            WHERE t.libelle = 'Entree'
            AND m.date_suppression IS NULL
            AND m.date_mouvement >= :depuis
            GROUP BY TO_CHAR(m.date_mouvement, 'YYYY-MM')
            ORDER BY mois
            """, nativeQuery = true)
    List<Object[]> sumEntreesParMois(LocalDateTime depuis);

    @Query(value = """
            SELECT TO_CHAR(m.date_mouvement, 'IYYY-"W"IW') AS mois, COALESCE(SUM(m.quantite), 0) AS total
            FROM mouvement_stock m
            JOIN type_mouvement_stock t ON t.id = m.id_type_mouvement
            WHERE t.libelle = 'Entree'
            AND m.date_suppression IS NULL
            AND m.date_mouvement >= :depuis
            GROUP BY TO_CHAR(m.date_mouvement, 'IYYY-"W"IW')
            ORDER BY mois
            """, nativeQuery = true)
    List<Object[]> sumEntreesParSemaine(LocalDateTime depuis);

    @Query(value = """
            SELECT TO_CHAR(m.date_mouvement, 'YYYY') AS mois, COALESCE(SUM(m.quantite), 0) AS total
            FROM mouvement_stock m
            JOIN type_mouvement_stock t ON t.id = m.id_type_mouvement
            WHERE t.libelle = 'Entree'
            AND m.date_suppression IS NULL
            AND m.date_mouvement >= :depuis
            GROUP BY TO_CHAR(m.date_mouvement, 'YYYY')
            ORDER BY mois
            """, nativeQuery = true)
    List<Object[]> sumEntreesParAn(LocalDateTime depuis);

    @Query(value = """
            SELECT TO_CHAR(m.date_mouvement, 'YYYY-MM') AS mois, COALESCE(SUM(m.quantite), 0) AS total
            FROM mouvement_stock m
            JOIN type_mouvement_stock t ON t.id = m.id_type_mouvement
            WHERE t.libelle = 'Sortie'
            AND m.date_suppression IS NULL
            AND m.date_mouvement >= :depuis
            GROUP BY TO_CHAR(m.date_mouvement, 'YYYY-MM')
            ORDER BY mois
            """, nativeQuery = true)
    List<Object[]> sumSortiesParMois(LocalDateTime depuis);

    @Query(value = """
            SELECT TO_CHAR(m.date_mouvement, 'IYYY-"W"IW') AS mois, COALESCE(SUM(m.quantite), 0) AS total
            FROM mouvement_stock m
            JOIN type_mouvement_stock t ON t.id = m.id_type_mouvement
            WHERE t.libelle = 'Sortie'
            AND m.date_suppression IS NULL
            AND m.date_mouvement >= :depuis
            GROUP BY TO_CHAR(m.date_mouvement, 'IYYY-"W"IW')
            ORDER BY mois
            """, nativeQuery = true)
    List<Object[]> sumSortiesParSemaine(LocalDateTime depuis);

    @Query(value = """
            SELECT TO_CHAR(m.date_mouvement, 'YYYY') AS mois, COALESCE(SUM(m.quantite), 0) AS total
            FROM mouvement_stock m
            JOIN type_mouvement_stock t ON t.id = m.id_type_mouvement
            WHERE t.libelle = 'Sortie'
            AND m.date_suppression IS NULL
            AND m.date_mouvement >= :depuis
            GROUP BY TO_CHAR(m.date_mouvement, 'YYYY')
            ORDER BY mois
            """, nativeQuery = true)
    List<Object[]> sumSortiesParAn(LocalDateTime depuis);

    @Query(value = """
            SELECT COALESCE(SUM(m.quantite), 0)
            FROM mouvement_stock m
            JOIN type_mouvement_stock t ON t.id = m.id_type_mouvement
            WHERE t.libelle = 'Entree'
            AND m.date_suppression IS NULL
            """, nativeQuery = true)
    Integer sumTotalEntrees();
}