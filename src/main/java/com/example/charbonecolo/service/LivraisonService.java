package com.example.charbonecolo.service;

import org.springframework.stereotype.Service;
import com.example.charbonecolo.model.LivraisonModel;
import com.example.charbonecolo.repository.LivraisonRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;


@Service
public class LivraisonService {
     @Autowired
    private LivraisonRepository livraisonRepository;

    public List<LivraisonModel> findAll() {
        return livraisonRepository.findAll();
    }

    public Optional<LivraisonModel> findById(Long id) {
        return livraisonRepository.findById(id);
    }

    public LivraisonModel save(LivraisonModel livraison) {
        return livraisonRepository.save(livraison);
    }

    public void deleteById(Long id) {
        livraisonRepository.deleteById(id);
    }
}
