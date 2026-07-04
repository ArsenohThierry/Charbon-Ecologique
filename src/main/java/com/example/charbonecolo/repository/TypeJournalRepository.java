package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.TypeJournalModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypeJournalRepository extends JpaRepository<TypeJournalModel, Integer> {
    Optional<TypeJournalModel> findByCode(String code);
}
