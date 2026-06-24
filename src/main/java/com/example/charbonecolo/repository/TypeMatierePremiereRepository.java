package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.TypeMatierePremiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeMatierePremiereRepository extends JpaRepository<TypeMatierePremiere, Integer> {
}
