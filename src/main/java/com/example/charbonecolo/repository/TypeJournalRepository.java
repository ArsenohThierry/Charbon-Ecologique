package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.TypeJournalModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TypeJournalRepository extends JpaRepository<TypeJournalModel, Integer> {
    Optional<TypeJournalModel> findByLibelle(String libelle);
}