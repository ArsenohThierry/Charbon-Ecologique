package com.example.charbonecolo.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.charbonecolo.model.TypeMatierePremiereModel;
import com.example.charbonecolo.model.FournisseurModel;
import com.example.charbonecolo.repository.TypeMatierePremiereRepository;

@Service
public class TypeMatierePremiereService {

    @Autowired
    private TypeMatierePremiereRepository typeMatierePremiereRepository;

    @Autowired
    private FournisseurService fournisseurService;

    public List<TypeMatierePremiereModel> getAll() {
        return typeMatierePremiereRepository.findAll();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveMatiere(TypeMatierePremiereModel model, Integer idFournisseur) {
        FournisseurModel fournisseur = fournisseurService.getById(idFournisseur);
        model.setFournisseur(fournisseur);

        if (model.getId() == null) {
            model.setDateAjout(LocalDateTime.now());
            model.setActif(true);
        }

        typeMatierePremiereRepository.save(model);
    }

    public Page<TypeMatierePremiereModel> findAllPaginated(Pageable pageable) {
        return typeMatierePremiereRepository.findAllWithFournisseur(pageable);
    }

    public TypeMatierePremiereModel getById(Integer id){
        return typeMatierePremiereRepository.findById(id).get();
    }

    @Transactional
    public void deleteById(Integer id) {
        typeMatierePremiereRepository.deleteById(id);
    }
}