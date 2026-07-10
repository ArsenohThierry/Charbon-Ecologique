package com.example.charbonecolo.service;

import com.example.charbonecolo.model.JournalFinancierModel;
import com.example.charbonecolo.repository.JournalFinancierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

        if (ecriture.getCreatedAt() == null) {
            ecriture.setCreatedAt(LocalDateTime.now());
        }

        if (ecriture.getReference() != null
                && ecriture.getOrigine() != null
                && journalRepo.existsByReferenceAndOrigine_Id(
                        ecriture.getReference(),
                        ecriture.getOrigine().getId())) {

            throw new IllegalArgumentException(
                    "Une écriture existe déjà avec cette référence."
            );
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
    public List<Map<String, Object>> evolutionMensuelleCA() {
        LocalDateTime debut = LocalDateTime.now().minusMonths(12).withDayOfMonth(1).withHour(0).withMinute(0);
        return journalRepo.evolutionMensuelleCA(debut);
    }

    // Méthodes pour la pagination
    @Transactional(readOnly = true)
    public Page<JournalFinancierModel> findAll(
            Pageable pageable) {

        return journalRepo
                .findAllByOrderByDateOperationDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<JournalFinancierModel> filtrerJournal(
            LocalDateTime debut,
            LocalDateTime fin,
            Pageable pageable) {

        return journalRepo
                .findByDateOperationBetweenOrderByDateOperationDesc(
                        debut,
                        fin,
                        pageable);
    }

    @Transactional(readOnly = true)
    public Page<JournalFinancierModel> filtrerParType(
            Integer typeJournalId,
            Pageable pageable) {

        return journalRepo
                .findByTypeJournal_IdOrderByDateOperationDesc(
                        typeJournalId,
                        pageable);
    }

    @Transactional(readOnly = true)
    public boolean existeDeja(String reference, Integer origineId) {

        return journalRepo.existsByReferenceAndOrigine_Id(
                reference,
                origineId
        );
    }

    @Transactional(readOnly = true)
    public List<JournalFinancierModel> rechercherParSource(
            String typeSource,
            Long idSource) {

        return journalRepo.findByTypeSourceAndIdSourceOrderByDateOperationDesc(
                typeSource,
                idSource
        );
    }

    @Transactional(readOnly = true)
    public Page<JournalFinancierModel> rechercherParSource(
            String typeSource,
            Long idSource,
            Pageable pageable) {

        return journalRepo.findByTypeSourceAndIdSource(
                typeSource,
                idSource,
                pageable
        );
    }

    public JournalFinancierModel enregistrerVente(JournalFinancierModel ecriture) {
        return enregistrer(ecriture);
    }

    public JournalFinancierModel enregistrerPaiement(JournalFinancierModel ecriture) {
        return enregistrer(ecriture);
    }

    public JournalFinancierModel enregistrerAchat(JournalFinancierModel ecriture) {
        return enregistrer(ecriture);
    }

    public JournalFinancierModel enregistrerFraisLivraison(JournalFinancierModel ecriture) {
        return enregistrer(ecriture);
    }
}
