package com.example.charbonecolo.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.charbonecolo.model.CommandeModel;

@Repository
public interface CommandeRepository extends JpaRepository<CommandeModel, Integer> {
    // Maka anle liste par pagination
    public List<CommandeModel> findBy(Pageable pageable);
}
