package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.LivreurModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LivreurRepository extends JpaRepository<LivreurModel, Integer> {
}
