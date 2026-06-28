package com.example.charbonecolo.service;

import com.example.charbonecolo.model.JournalFinancierModel;
import com.example.charbonecolo.model.TresorerieModel;
import com.example.charbonecolo.repository.JournalFinancierRepository;
import com.example.charbonecolo.repository.TresorerieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class JournalFinancierService {

    private final JournalFinancierRepository journalRepo;
    private final TresorerieRepository tresorerieRepo;

    public JournalFinancierService(JournalFinancierRepository journalRepo,
                                    TresorerieRepository tresorerieRepo) {
        this.journalRepo = journalRepo;
        this.tresorerieRepo = tresorerieRepo;
    }

    /**
     * Enregistre une écriture dans le journal et met à jour la trésorerie.
     */
    public JournalFinancierModel enregistrer(JournalFinancierModel ecriture) {
        JournalFinancierModel saved = journalRepo.save(ecriture);

        // Synchronisation automatique de la trésorerie
        String code = ecriture.getTypeJournal().getCode();
        String typeMouvement;
        if ("VENTE".equals(code) || "BANQUE".equals(code)) {
            typeMouvement = "ENTREE";
        } else {
            typeMouvement = "SORTIE";
        }

        BigDecimal soldeActuel = tresorerieRepo.calculerSolde().orElse(BigDecimal.ZERO);
        BigDecimal nouveauSolde = "ENTREE".equals(typeMouvement)
                ? soldeActuel.add(ecriture.getMontant())
                : soldeActuel.subtract(ecriture.getMontant());

        TresorerieModel mvt = new TresorerieModel();
        mvt.setDateMouvement(ecriture.getDateOperation());
        mvt.setType(typeMouvement);
        mvt.setMontant(ecriture.getMontant());
        mvt.setSolde(nouveauSolde);
        mvt.setLibelle(ecriture.getDescription() != null ? ecriture.getDescription() : code);
        mvt.setJournalId(saved.getId());
        tresorerieRepo.save(mvt);

        return saved;
    }

    /**
     * Retourne toutes les écritures classées par date décroissante.
     */
    @Transactional(readOnly = true)
    public List<JournalFinancierModel> findAll() {
        return journalRepo.findAllByOrderByDateOperationDesc();
    }

    /**
     * Calcule le chiffre d'affaires (type VENTE) sur une période.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculerCA(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal v = journalRepo.calculerCA(debut, fin);
        return v != null ? v : BigDecimal.ZERO;
    }

    /**
     * Calcule le bénéfice (VENTE - ACHAT) sur une période.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculerBenefice(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal v = journalRepo.calculerBenefice(debut, fin);
        return v != null ? v : BigDecimal.ZERO;
    }

    /**
     * Calcule le total des entrées sur une période.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculerTotalEntrees(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal v = journalRepo.calculerTotalEntrees(debut, fin);
        return v != null ? v : BigDecimal.ZERO;
    }

    /**
     * Calcule le total des sorties sur une période.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculerTotalSorties(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal v = journalRepo.calculerTotalSorties(debut, fin);
        return v != null ? v : BigDecimal.ZERO;
    }

    /**
     * Filtre le journal par période.
     */
    @Transactional(readOnly = true)
    public List<JournalFinancierModel> filtrerJournal(LocalDateTime debut, LocalDateTime fin) {
        return journalRepo.findByDateOperationBetweenOrderByDateOperationDesc(debut, fin);
    }

    /**
     * Filtre le journal par type de journal.
     */
    @Transactional(readOnly = true)
    public List<JournalFinancierModel> filtrerParType(Integer typeJournalId) {
        return journalRepo.findByTypeJournal_IdOrderByDateOperationDesc(typeJournalId);
    }

    /**
     * Retourne l'évolution mensuelle du CA (12 derniers mois) pour les graphiques.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> evolutionMensuelleCA() {
        LocalDateTime debut = LocalDateTime.now().minusMonths(12).withDayOfMonth(1).withHour(0).withMinute(0);
        return journalRepo.evolutionMensuelleCA(debut);
    }
}
