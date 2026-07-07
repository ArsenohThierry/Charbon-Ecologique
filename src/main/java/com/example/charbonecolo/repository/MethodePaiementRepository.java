package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.MethodePaiementModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MethodePaiementRepository extends JpaRepository<MethodePaiementModel, Integer> {
}