package com.example.charbonecolo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.model.FactureDetailModel;

@Repository
public interface FactureDetailRepository extends JpaRepository<FactureDetailModel, Integer> {
    public List<FactureDetailModel> findByFactureId(Integer id);
}
