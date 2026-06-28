package com.example.charbonecolo.service;

import com.example.charbonecolo.model.TresorerieModel;
import com.example.charbonecolo.repository.TresorerieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class TresorerieService {

    private final TresorerieRepository tresorerieRepo;

    public TresorerieService(TresorerieRepository tresorerieRepo) {
        this.tresorerieRepo = tresorerieRepo;
    }

    /**
     * Retourne tous les mouvements de trésorerie (du plus récent au plus ancien).
     */
    @Transactional(readOnly = true)
    public List<TresorerieModel> findAll() {
        return tresorerieRepo.findAllByOrderByDateMouvementDesc();
    }

    /**
     * Retourne le solde courant de trésorerie.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculerSolde() {
        return tresorerieRepo.calculerSolde().orElse(BigDecimal.ZERO);
    }

    /**
     * Retourne uniquement les mouvements d'un type donné (ENTREE ou SORTIE).
     */
    @Transactional(readOnly = true)
    public List<TresorerieModel> findByType(String type) {
        return tresorerieRepo.findByTypeOrderByDateMouvementDesc(type);
    }
}
