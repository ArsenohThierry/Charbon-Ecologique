package com.example.charbonecolo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.charbonecolo.model.ClientModel;
import com.example.charbonecolo.repository.ClientRepository;

@Service
public class ClientService {
    private final ClientRepository clientRepository;
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<ClientModel> findAll() {
        return clientRepository.findAll();
    }

    public ClientModel findById(Integer id) {
        return clientRepository.findById(id).orElse(null);
    }

    public List<ClientModel> findByName(String nom) {
        return clientRepository.findByNomContainingIgnoreCase(nom);
    }
}
