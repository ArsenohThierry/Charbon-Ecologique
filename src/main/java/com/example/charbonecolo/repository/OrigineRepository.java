package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.OrigineModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrigineRepository extends JpaRepository<OrigineModel, Integer> {
    Optional<OrigineModel> findByCode(String code);
}
