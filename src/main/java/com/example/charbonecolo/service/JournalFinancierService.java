package com.example.charbonecolo.service;

import com.example.charbonecolo.model.JournalFinancierModel;
import com.example.charbonecolo.repository.JournalFinancierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class JournalFinancierService {

    private final JournalFinancierRepository journalRepo;

    public JournalFinancierService(JournalFinancierRepository journalRepo) {
        this.journalRepo = journalRepo;
    }

    public JournalFinancierModel enregistrer(JournalFinancierModel ecriture) {
        if (ecriture.getDebit() == null) {
            ecriture.setDebit(BigDecimal.ZERO);
        }
        if (ecriture.getCredit() == null) {
            ecriture.setCredit(BigDecimal.ZERO);
        }
        return journalRepo.save(ecriture);
    }

    @Transactional(readOnly = true)
    public List<JournalFinancierModel> findAll() {
        return journalRepo.findAllByOrderByDateOperationDesc();
    }

    @Transactional(readOnly = true)
    public BigDecimal calculerCA(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal v = journalRepo.calculerCA(debut, fin);
        return v != null ? v : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculerBenefice(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal v = journalRepo.calculerBenefice(debut, fin);
        return v != null ? v : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculerTotalEntrees(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal v = journalRepo.calculerTotalEntrees(debut, fin);
        return v != null ? v : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculerTotalSorties(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal v = journalRepo.calculerTotalSorties(debut, fin);
        return v != null ? v : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculerSolde() {
        BigDecimal v = journalRepo.calculerSolde();
        return v != null ? v : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<JournalFinancierModel> filtrerJournal(LocalDateTime debut, LocalDateTime fin) {
        return journalRepo.findByDateOperationBetweenOrderByDateOperationDesc(debut, fin);
    }

    @Transactional(readOnly = true)
    public List<JournalFinancierModel> filtrerParType(Integer typeJournalId) {
        return journalRepo.findByTypeJournal_IdOrderByDateOperationDesc(typeJournalId);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> evolutionMensuelleCA(
            LocalDateTime debut,
            LocalDateTime fin) {
        if (debut == null || fin == null) {
            return List.of();
        }

        if (debut.isAfter(fin)) {
            LocalDateTime tmp = debut;
            debut = fin;
            fin = tmp;
        }

        List<Object[]> lignes = journalRepo.evolutionMensuelleCA(debut, fin);
        Map<String, BigDecimal> totalParMois = new HashMap<>();

        for (Object[] ligne : lignes) {
            if (ligne == null || ligne.length < 2) {
                continue;
            }

            String mois = ligne[0] != null ? ligne[0].toString() : null;
            if (mois == null || mois.isBlank()) {
                continue;
            }

            BigDecimal total = BigDecimal.ZERO;
            Object totalObj = ligne[1];
            if (totalObj instanceof BigDecimal bd) {
                total = bd;
            } else if (totalObj instanceof Number n) {
                total = BigDecimal.valueOf(n.doubleValue());
            }

            totalParMois.put(mois, total);
        }

        YearMonth moisDebut = YearMonth.from(debut);
        YearMonth moisFin = YearMonth.from(fin);
        List<Map<String, Object>> resultat = new ArrayList<>();

        for (YearMonth ym = moisDebut; !ym.isAfter(moisFin); ym = ym.plusMonths(1)) {
            Map<String, Object> point = new HashMap<>();
            String cleMois = ym.toString();
            point.put("mois", cleMois);
            point.put("total", totalParMois.getOrDefault(cleMois, BigDecimal.ZERO));
            resultat.add(point);
        }

        return resultat;
    }
}
