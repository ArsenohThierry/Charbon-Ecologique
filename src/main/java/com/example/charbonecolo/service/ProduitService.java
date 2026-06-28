package com.example.charbonecolo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.charbonecolo.model.ProduitModel;
import com.example.charbonecolo.repository.ProduitRepository;

@Service
public class ProduitService {
    private final ProduitRepository produitRepository;

    public ProduitService(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    public List<ProduitModel> findAll() {
        return produitRepository.findAll();
    }

    public ProduitModel findById(Integer id) {
        return produitRepository.findById(id).orElse(null);
    }
}
