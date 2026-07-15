package com.example.charbonecolo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.model.EmployeModel;

@Repository
public interface EmployeRepository extends JpaRepository<EmployeModel, Integer> {

    boolean existsByReference(String reference);
    long countByEmploiId(Integer emploiId);

    @Query(value = "SELECT e FROM EmployeModel e JOIN FETCH e.emploi p " +
           "WHERE ( :nom = '' OR LOWER(e.nom) LIKE CONCAT('%', LOWER(:nom), '%') ) " +
           "  AND ( :reference = '' OR LOWER(e.reference) LIKE CONCAT('%', LOWER(:reference), '%') ) " +
           "  AND ( :idEmploi IS NULL OR e.emploi.id = :idEmploi )",
           countQuery = "SELECT COUNT(e) FROM EmployeModel e " +
           "WHERE ( :nom = '' OR LOWER(e.nom) LIKE CONCAT('%', LOWER(:nom), '%') ) " +
           "  AND ( :reference = '' OR LOWER(e.reference) LIKE CONCAT('%', LOWER(:reference), '%') ) " +
           "  AND ( :idEmploi IS NULL OR e.emploi.id = :idEmploi )")
    Page<EmployeModel> findByCriteria(
            @Param("nom") String nom,
            @Param("reference") String reference,
            @Param("idEmploi") Integer idEmploi,
            Pageable pageable);
}
