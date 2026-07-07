package com.example.charbonecolo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.model.FournisseurModel;

@Repository
public interface FournisseurRepository extends JpaRepository<FournisseurModel, Integer> {

    @Query(value = "SELECT f FROM FournisseurModel f " +
           "WHERE ( :nom = '' OR LOWER(f.nom) LIKE CONCAT('%', LOWER(:nom), '%') ) " +
           "  AND ( :email = '' OR LOWER(f.email) LIKE CONCAT('%', LOWER(:email), '%') ) " +
           "  AND ( :telephone = '' OR LOWER(f.telephone) LIKE CONCAT('%', LOWER(:telephone), '%') ) " +
           "  AND ( :adresse = '' OR LOWER(f.adresse) LIKE CONCAT('%', LOWER(:adresse), '%') ) " +
           "  AND ( :actif IS NULL OR f.actif = :actif ) ",
           countQuery = "SELECT COUNT(f) FROM FournisseurModel f " +
           "WHERE ( :nom = '' OR LOWER(f.nom) LIKE CONCAT('%', LOWER(:nom), '%') ) " +
           "  AND ( :email = '' OR LOWER(f.email) LIKE CONCAT('%', LOWER(:email), '%') ) " +
           "  AND ( :telephone = '' OR LOWER(f.telephone) LIKE CONCAT('%', LOWER(:telephone), '%') ) " +
           "  AND ( :adresse = '' OR LOWER(f.adresse) LIKE CONCAT('%', LOWER(:adresse), '%') ) " +
           "  AND ( :actif IS NULL OR f.actif = :actif ) ")
    Page<FournisseurModel> findByCriteria(
            @Param("nom") String nom,
            @Param("email") String email,
            @Param("telephone") String telephone,
            @Param("adresse") String adresse,
            @Param("actif") Boolean actif,
            Pageable pageable);

       long countByActifTrue();
}