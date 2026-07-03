package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.model.MouvementSortieDetailModel;
import com.example.charbonecolo.model.MouvementStockModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MouvementSortieDetailRepository extends JpaRepository<MouvementSortieDetailModel, Integer> {

        List<MouvementSortieDetailModel> findByMouvementSortie(MouvementStockModel mouvementSortie);

        void deleteByMouvementSortie(MouvementStockModel mouvementSortie);

        @Query("""
                        SELECT COALESCE(SUM(d.quantite),0)
                        FROM MouvementSortieDetailModel d
                        WHERE d.lotProduction.id = :idLot
                        """)
        Integer sumSortiesByLot(Integer idLot);

        @Query("""
                        SELECT COALESCE(SUM(d.quantite), 0)
                        FROM MouvementSortieDetailModel d
                        WHERE d.lotProduction.produit.id = :idProduit
                        """)
        Integer sumSortiesByProduit(Integer idProduit);

        @Query("""
                        SELECT COUNT(d) > 0
                        FROM MouvementSortieDetailModel d
                        WHERE d.lotProduction.id = :idLot
                        """)
        boolean isLotUsed(@Param("idLot") Integer idLot);

        @Query("""
                        SELECT MIN(m.dateMouvement)
                        FROM MouvementSortieDetailModel d
                        JOIN d.mouvementSortie m
                        WHERE d.lotProduction.produit.id = :idProduit
                        """)
        LocalDateTime findFirstSortieDateByProduit(Integer idProduit);

        @Query("""
                        SELECT COALESCE(SUM(d.quantite), 0)
                        FROM MouvementSortieDetailModel d
                        JOIN d.mouvementSortie m
                        WHERE d.lotProduction.produit.id = :idProduit
                        AND m.dateMouvement >= :depuis
                        """)
        Integer sumSortiesByProduitDepuis(Integer idProduit, LocalDateTime depuis);
}
