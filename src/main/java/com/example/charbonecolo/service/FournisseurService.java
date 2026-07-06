package com.example.charbonecolo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.charbonecolo.model.FournisseurModel;
import com.example.charbonecolo.repository.FournisseurRepository;

@Service
public class FournisseurService {

    @Autowired
    private FournisseurRepository fournisseurRepository;

    public List<FournisseurModel> getAll() {
        return fournisseurRepository.findAll();
    }

    public Page<FournisseurModel> searchFournisseurs(
            String nom, String email, String telephone,
            String adresse, Boolean actif, Pageable pageable) {
        return fournisseurRepository.findByCriteria(
                nom, email, telephone, adresse, actif, pageable);
    }

    @Transactional
    public void persistFournisseur(FournisseurModel fournisseurModel) {
        fournisseurRepository.save(fournisseurModel);
    }

    public FournisseurModel getById(Integer id) {
        return fournisseurRepository.findById(id).get();
    }

    @Transactional
    public void deleteById(Integer id) {
        fournisseurRepository.deleteById(id);
    }

    public long getNombreFournisseursActifs() {
        return fournisseurRepository.countByActifTrue();
    }
}
