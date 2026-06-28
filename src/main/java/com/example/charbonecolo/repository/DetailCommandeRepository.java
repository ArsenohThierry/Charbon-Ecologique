package com.example.charbonecolo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.charbonecolo.model.DetailCommandeModel;

public interface DetailCommandeRepository extends JpaRepository<DetailCommandeModel, Integer> {
    public List<DetailCommandeModel> findByCommandeId(Integer id);

    public List<DetailCommandeModel> findAllByCommandeIdIn(List<Integer> ids);
}
