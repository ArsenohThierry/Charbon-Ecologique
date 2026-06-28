package com.example.charbonecolo.service;

import com.example.charbonecolo.model.*;
import com.example.charbonecolo.repository.*;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service    
public class TresorerieService {

    private final TresorerieRepository tresorerieRepo;

    public TresorerieService(TresorerieRepository tresorerieRepo) {
        this.tresorerieRepo = tresorerieRepo;
    }

    public List<TresorerieModel> findAll() {
        return tresorerieRepo.findAll();
    }

    public void enregistrer(String typeOp, BigDecimal montant,
                             String origine, String reference, String description) {
        TresorerieModel t = new TresorerieModel();
        t.setDateOperation(LocalDateTime.now());
        t.setTypeOperation(typeOp);
        t.setMontant(montant);
        t.setOrigine(origine);
        t.setReferenceOrigine(reference);
        t.setDescription(description);
        tresorerieRepo.save(t);
    }
}