package com.example.charbonecolo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.model.TypeMatierePremiereModel;

@Repository
public interface TypeMatierePremiereRepository extends JpaRepository<TypeMatierePremiereModel, Integer> {
    @Query(value = "SELECT m FROM TypeMatierePremiereModel m LEFT JOIN FETCH m.fournisseur",
           countQuery = "SELECT COUNT(m) FROM TypeMatierePremiereModel m")
    Page<TypeMatierePremiereModel> findAllWithFournisseur(Pageable pageable);
}
