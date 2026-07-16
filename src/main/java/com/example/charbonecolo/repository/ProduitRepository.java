package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.ProduitModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<ProduitModel, Integer> {
    @Query("SELECT p FROM ProduitModel p WHERE "
            + "(:nom IS NULL OR LOWER(p.nom) LIKE LOWER(CONCAT('%', :nom, '%'))) "
            + "AND (:puMin IS NULL OR p.pu >= :puMin) "
            + "AND (:puMax IS NULL OR p.pu <= :puMax) "
            + "ORDER BY p.id ASC")
    List<ProduitModel> rechercher(@Param("nom") String nom, @Param("puMin") Double puMin, @Param("puMax") Double puMax);

}