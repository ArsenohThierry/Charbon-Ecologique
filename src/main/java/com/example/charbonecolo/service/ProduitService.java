package com.example.charbonecolo.service;

import com.example.charbonecolo.model.ProduitModel;
import com.example.charbonecolo.repository.ProduitRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return produitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit introuvable : id=" + id));
    }

    public void save(ProduitModel produit) {
        produitRepository.save(produit);
    }

    public void deleteById(Integer id) {
        if (!produitRepository.existsById(id)) {
            throw new EntityNotFoundException("Produit introuvable : id=" + id);
        }
        produitRepository.deleteById(id);
    }
}