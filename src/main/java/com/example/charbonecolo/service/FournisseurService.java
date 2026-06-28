package com.example.charbonecolo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.charbonecolo.model.FournisseurModel;
import com.example.charbonecolo.repository.FournisseurRepository;

@Service
public class FournisseurService {

    @Autowired
    private FournisseurRepository fournisseurRepository;

    public List<FournisseurModel> getAll(){
        return fournisseurRepository.findAll();
    }

    @Transactional
    public void persistFournisseur(FournisseurModel fournisseurModel){
        fournisseurRepository.save(fournisseurModel);
    }

    public FournisseurModel getById(Integer id){
        return fournisseurRepository.findById(id).get();
    }
}
