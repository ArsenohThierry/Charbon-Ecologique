package com.example.charbonecolo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.charbonecolo.model.ClientModel;

public interface ClientRepository extends JpaRepository<ClientModel, Integer> {
    public List<ClientModel> findByNomContainingIgnoreCase(@Param("kw") String keyword);
}
