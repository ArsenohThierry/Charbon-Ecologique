package com.example.charbonecolo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.model.EmploiModel;

@Repository
public interface EmploiRepository extends JpaRepository<EmploiModel, Integer> {
}
