package com.example.charbonecolo.service;

import com.example.charbonecolo.model.JournalFinancierModel;
import com.example.charbonecolo.repository.JournalFinancierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class TresorerieService {

    private final JournalFinancierRepository journalRepo;

    public TresorerieService(JournalFinancierRepository journalRepo) {
        this.journalRepo = journalRepo;
    }

    @Transactional(readOnly = true)
    public List<MouvementTresorerie> findAll() {
        List<MouvementTresorerie> mouvements = construireHistorique();
        Collections.reverse(mouvements);
        return mouvements;
    }

    @Transactional(readOnly = true)
    public Page<MouvementTresorerie> rechercher(LocalDate date, String reference, String keyword, Pageable pageable) {
        List<MouvementTresorerie> mouvements = findAll().stream()
                .filter(m -> date == null || m.getDateOperation().toLocalDate().equals(date))
                .filter(m -> estVide(reference) || contient(m.getReference(), reference))
                .filter(m -> estVide(keyword)
                        || contient(m.getReference(), keyword)
                        || contient(m.getDescription(), keyword))
                .toList();

        int start = (int) pageable.getOffset();
        if (start >= mouvements.size()) {
            return new PageImpl<>(List.of(), pageable, mouvements.size());
        }

        int end = Math.min(start + pageable.getPageSize(), mouvements.size());
        return new PageImpl<>(mouvements.subList(start, end), pageable, mouvements.size());
    }

    @Transactional(readOnly = true)
    public BigDecimal calculerSolde() {
        BigDecimal solde = journalRepo.calculerSolde();
        return solde != null ? solde : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculerTotalEntrees() {
        return construireHistorique().stream()
                .map(MouvementTresorerie::getDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculerTotalSorties() {
        return construireHistorique().stream()
                .map(MouvementTresorerie::getCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<MouvementTresorerie> construireHistorique() {
        List<JournalFinancierModel> ecritures = journalRepo.findAllByOrderByDateOperationAscIdAsc();
        List<MouvementTresorerie> mouvements = new ArrayList<>();
        BigDecimal solde = BigDecimal.ZERO;

        for (JournalFinancierModel ecriture : ecritures) {
            BigDecimal debit = valeurOuZero(ecriture.getDebit());
            BigDecimal credit = valeurOuZero(ecriture.getCredit());
            solde = solde.add(debit).subtract(credit);
            mouvements.add(new MouvementTresorerie(
                    ecriture.getId(),
                    ecriture.getDateOperation(),
                    ecriture.getReference(),
                    ecriture.getDescription(),
                    debit,
                    credit,
                    solde
            ));
        }

        return mouvements;
    }

    private BigDecimal valeurOuZero(BigDecimal valeur) {
        return valeur != null ? valeur : BigDecimal.ZERO;
    }

    private boolean estVide(String valeur) {
        return valeur == null || valeur.trim().isEmpty();
    }

    private boolean contient(String champ, String recherche) {
        return champ != null && champ.toLowerCase().contains(recherche.trim().toLowerCase());
    }

    public static class MouvementTresorerie {
        private final Long journalId;
        private final LocalDateTime dateOperation;
        private final String reference;
        private final String description;
        private final BigDecimal debit;
        private final BigDecimal credit;
        private final BigDecimal solde;

        public MouvementTresorerie(Long journalId, LocalDateTime dateOperation, String reference,
                                   String description, BigDecimal debit, BigDecimal credit, BigDecimal solde) {
            this.journalId = journalId;
            this.dateOperation = dateOperation;
            this.reference = reference;
            this.description = description;
            this.debit = debit;
            this.credit = credit;
            this.solde = solde;
        }

        public Long getJournalId() { return journalId; }
        public LocalDateTime getDateOperation() { return dateOperation; }
        public String getReference() { return reference; }
        public String getDescription() { return description; }
        public BigDecimal getDebit() { return debit; }
        public BigDecimal getCredit() { return credit; }
        public BigDecimal getSolde() { return solde; }
    }
}
