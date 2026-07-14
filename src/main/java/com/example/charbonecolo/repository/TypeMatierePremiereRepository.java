package com.example.charbonecolo.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.model.TypeMatierePremiereModel;

@Repository
public interface TypeMatierePremiereRepository extends JpaRepository<TypeMatierePremiereModel, Integer> {
        @Query(value = "SELECT m FROM TypeMatierePremiereModel m JOIN m.fournisseur f " +
           "WHERE (CASE WHEN :libelleNull = true THEN true WHEN :libelleEmpty = true THEN true ELSE LOWER(m.libelle) LIKE CONCAT('%', LOWER(:libelle), '%') END) " +
           "  AND (CASE WHEN :prixMinNull = true THEN true ELSE m.prixUnitaire >= :prixMin END) " +
           "  AND (CASE WHEN :prixMaxNull = true THEN true ELSE m.prixUnitaire <= :prixMax END) " +
           "  AND (CASE WHEN :idFournisseurNull = true THEN true ELSE f.id = :idFournisseur END) " +
           "  AND (CASE WHEN :dateDebutNull = true THEN true ELSE m.dateAjout >= :dateDebut END) " +
           "  AND (CASE WHEN :dateFinNull = true THEN true ELSE m.dateAjout <= :dateFin END) " +
           "  AND (CASE WHEN :actifNull = true THEN true ELSE m.actif = :actif END)",
           
           countQuery = "SELECT COUNT(m) FROM TypeMatierePremiereModel m JOIN m.fournisseur f " +
           "WHERE (CASE WHEN :libelleNull = true THEN true WHEN :libelleEmpty = true THEN true ELSE LOWER(m.libelle) LIKE CONCAT('%', LOWER(:libelle), '%') END) " +
           "  AND (CASE WHEN :prixMinNull = true THEN true ELSE m.prixUnitaire >= :prixMin END) " +
           "  AND (CASE WHEN :prixMaxNull = true THEN true ELSE m.prixUnitaire <= :prixMax END) " +
           "  AND (CASE WHEN :idFournisseurNull = true THEN true ELSE f.id = :idFournisseur END) " +
           "  AND (CASE WHEN :dateDebutNull = true THEN true ELSE m.dateAjout >= :dateDebut END) " +
           "  AND (CASE WHEN :dateFinNull = true THEN true ELSE m.dateAjout <= :dateFin END) " +
           "  AND (CASE WHEN :actifNull = true THEN true ELSE m.actif = :actif END)")
           
    Page<TypeMatierePremiereModel> findByCriteria(
            @Param("libelle") String libelle,
            @Param("libelleNull") boolean libelleNull,
            @Param("libelleEmpty") boolean libelleEmpty,
            @Param("prixMin") BigDecimal prixMin,
            @Param("prixMinNull") boolean prixMinNull,
            @Param("prixMax") BigDecimal prixMax,
            @Param("prixMaxNull") boolean prixMaxNull,
            @Param("idFournisseur") Integer idFournisseur,
            @Param("idFournisseurNull") boolean idFournisseurNull,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateDebutNull") boolean dateDebutNull,
            @Param("dateFin") LocalDateTime dateFin,
            @Param("dateFinNull") boolean dateFinNull,
            @Param("actif") Boolean actif,
            @Param("actifNull") boolean actifNull,
            Pageable pageable);


}
