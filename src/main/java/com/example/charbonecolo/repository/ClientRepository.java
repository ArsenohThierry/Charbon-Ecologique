package com.example.charbonecolo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.charbonecolo.model.ClientModel;

public interface ClientRepository extends JpaRepository<ClientModel, Integer> {
    
}
