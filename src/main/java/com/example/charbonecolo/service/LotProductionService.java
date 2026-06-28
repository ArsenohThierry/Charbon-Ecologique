package com.example.charbonecolo.service;

import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.model.ProduitModel;
import com.example.charbonecolo.model.TypeMatierePremiereModel;
import com.example.charbonecolo.repository.LotProductionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LotProductionService {

    @Autowired
    private LotProductionRepository lotProductionRepository;

    @Autowired
    private TypeMatierePremiereService typeMatierePremiereService;

    @Autowired
    private ProduitService produitService;

    public List<LotProductionModel> getAll() {
        return lotProductionRepository.findAll();
    }

    public LotProductionModel getById(Integer id) {
        return lotProductionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lot introuvable : id=" + id));
    }

    @Transactional
    public void save(LotProductionModel lot, Integer idTypeMatiere, Integer idProduit) {

        TypeMatierePremiereModel matiere = typeMatierePremiereService.getById(idTypeMatiere);
        ProduitModel produit = produitService.findById(idProduit);

        lot.setTypeMatierePremiere(matiere);
        lot.setProduit(produit);

        if (lot.getId() == null) {
            lot.setReference(genererReference());
            lot.setDateEntreeLot(LocalDateTime.now());

            // TODO: Arranger la règle de gestion — la quantité prévue doit être calculée
            //       selon les ratios définis par le responsable (ex: Xkg matière → Y briquettes).
            //       Pour l'instant on met (qteMatierePremiere / 2) par exemple
            lot.setQuantiteProduitPrevue(lot.getQuantiteMatiereUtilisee().intValue());
        }

        lotProductionRepository.save(lot);
    }

    @Transactional
    public void deleteById(Integer id) {
        if (!lotProductionRepository.existsById(id)) {
            throw new EntityNotFoundException("Lot introuvable : id=" + id);
        }
        lotProductionRepository.deleteById(id);
    }

    // ── Génération de référence LOT-001, LOT-002, ... ────────────────────────
    private String genererReference() {
        long count = lotProductionRepository.getNextId();
        return String.format("LOT-%04d", count);
    }
}