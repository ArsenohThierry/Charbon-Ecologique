package com.example.charbonecolo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.model.SalaireHistoriqueModel;

@Repository
public interface SalaireHistoriqueRepository extends JpaRepository<SalaireHistoriqueModel, Integer> {

    Page<SalaireHistoriqueModel> findByEmployeIdOrderByDateEffetDesc(Integer employeId, Pageable pageable);
}
