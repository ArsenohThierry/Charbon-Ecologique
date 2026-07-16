package com.example.charbonecolo.service;

import com.example.charbonecolo.model.JournalFinancierModel;
import com.example.charbonecolo.model.TypeJournalModel;
import com.example.charbonecolo.model.OrigineModel;
import com.example.charbonecolo.repository.JournalFinancierRepository;
import com.example.charbonecolo.repository.TypeJournalRepository;
import com.example.charbonecolo.repository.OrigineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class JournalFinancierService {

    private final JournalFinancierRepository journalRepo;
    private final TypeJournalRepository typeJournalRepo;
    private final OrigineRepository origineRepo;

    public JournalFinancierService(JournalFinancierRepository journalRepo,
                                   TypeJournalRepository typeJournalRepo,
                                   OrigineRepository origineRepo) {
        this.journalRepo = journalRepo;
        this.typeJournalRepo = typeJournalRepo;
        this.origineRepo = origineRepo;
    }

    // ================================================================
    // CRUD
    // ================================================================

    public JournalFinancierModel enregistrer(JournalFinancierModel ecriture) {
        if (ecriture.getDebit() == null) {
            ecriture.setDebit(BigDecimal.ZERO);
        }

        if (ecriture.getCredit() == null) {
            ecriture.setCredit(BigDecimal.ZERO);
        }

        if (ecriture.getDebit().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le débit ne peut pas être négatif.");
        }
        if (ecriture.getCredit().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le crédit ne peut pas être négatif.");
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

    // ================================================================
    // Calculs financiers
    // ================================================================

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

    // ================================================================
    // Filtrage
    // ================================================================

    @Transactional(readOnly = true)
    public List<JournalFinancierModel> filtrerJournal(LocalDateTime debut, LocalDateTime fin) {
        return journalRepo.findByDateOperationBetweenOrderByDateOperationDesc(debut, fin);
    }

    @Transactional(readOnly = true)
    public List<JournalFinancierModel> filtrerParType(Integer typeJournalId) {
        return journalRepo.findByTypeJournal_IdOrderByDateOperationDesc(typeJournalId);
    }

    @Transactional(readOnly = true)
    public List<JournalFinancierModel> filtrerParTypeCode(String code) {
        return journalRepo.findByTypeJournal_CodeOrderByDateOperationDesc(code);
    }

    @Transactional(readOnly = true)
    public List<JournalFinancierModel> filtrerParOrigineCode(String code) {
        return journalRepo.findByOrigine_CodeOrderByDateOperationDesc(code);
    }

    @Transactional(readOnly = true)
    public List<JournalFinancierModel> rechercherParReference(String reference) {
        return journalRepo.findByReferenceContainingOrderByDateOperationDesc(reference);
    }

    // ================================================================
    // Pagination
    // ================================================================

    @Transactional(readOnly = true)
    public Page<JournalFinancierModel> findAll(Pageable pageable) {
        return journalRepo.findAllByOrderByDateOperationDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<JournalFinancierModel> filtrerJournal(
            LocalDateTime debut, LocalDateTime fin, Pageable pageable) {
        return journalRepo.findByDateOperationBetweenOrderByDateOperationDesc(debut, fin, pageable);
    }

    @Transactional(readOnly = true)
    public Page<JournalFinancierModel> filtrerParType(Integer typeJournalId, Pageable pageable) {
        return journalRepo.findByTypeJournal_IdOrderByDateOperationDesc(typeJournalId, pageable);
    }

    // ================================================================
    // Évolution CA
    // ================================================================

    @Transactional(readOnly = true)
    public List<Map<String, Object>> evolutionMensuelleCA() {
        LocalDateTime debut = LocalDateTime.now().minusMonths(12)
                .withDayOfMonth(1).withHour(0).withMinute(0);
        return journalRepo.evolutionMensuelleCA(debut);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> evolutionCAParPeriode(String periode) {
        return switch (periode.toUpperCase()) {
            case "HEBDO" -> {
                LocalDateTime d = LocalDateTime.now().minusWeeks(12).withHour(0).withMinute(0);
                yield journalRepo.evolutionHebdomadaireCA(d);
            }
            case "ANNUEL" -> {
                LocalDateTime d = LocalDateTime.now().minusYears(5).withDayOfYear(1).withHour(0).withMinute(0);
                yield journalRepo.evolutionAnnuelleCA(d);
            }
            default -> evolutionMensuelleCA();
        };
    }

    // ================================================================
    // Vérification doublons
    // ================================================================
    @Transactional(readOnly = true)
    public boolean verifierDoublon(String reference, Integer origineId) {
        return journalRepo.existsByReferenceAndOrigine_Id(reference, origineId);
    }

    @Transactional(readOnly = true)
    public Optional<JournalFinancierModel> trouverParReferenceEtOrigine(
            String reference, Integer origineId) {
        return journalRepo.findByReferenceAndOrigine_Id(reference, origineId);
    }

    // ================================================================
    // Recherche par source (traçabilité inter-modules)
    // ================================================================

    @Transactional(readOnly = true)
    public List<JournalFinancierModel> rechercherParSource(String typeSource, Long idSource) {
        return journalRepo.findByTypeSourceAndIdSourceOrderByDateOperationDesc(typeSource, idSource);
    }

    @Transactional(readOnly = true)
    public Page<JournalFinancierModel> rechercherParSource(
            String typeSource, Long idSource, Pageable pageable) {
        return journalRepo.findByTypeSourceAndIdSource(typeSource, idSource, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<JournalFinancierModel> trouverParSource(String typeSource, Long idSource) {
        return journalRepo.findByIdSourceAndTypeSource(idSource, typeSource);
    }

    @Transactional
    public void mettreAJourEcriture(JournalFinancierModel ecriture) {
        if (ecriture.getDebit() == null) ecriture.setDebit(BigDecimal.ZERO);
        if (ecriture.getCredit() == null) ecriture.setCredit(BigDecimal.ZERO);
        if (ecriture.getDebit().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le débit ne peut pas être négatif.");
        }
        if (ecriture.getCredit().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le crédit ne peut pas être négatif.");
        }
        journalRepo.save(ecriture);
    }

    @Transactional
    public void supprimerEcriture(Long id) {
        journalRepo.deleteById(id);
    }

    @Transactional
    public void supprimerEcrituresParSource(String typeSource, Long idSource) {
        List<JournalFinancierModel> ecritures = journalRepo
                .findByTypeSourceAndIdSourceOrderByDateOperationDesc(typeSource, idSource);
        journalRepo.deleteAll(ecritures);
    }

    private JournalFinancierModel creerEcriture(
            LocalDateTime dateOperation,
            TypeJournalModel typeJournal,
            OrigineModel origine,
            BigDecimal debit,
            BigDecimal credit,
            String reference,
            String description,
            String typeSource,
            Long idSource) {

        JournalFinancierModel ecriture = new JournalFinancierModel();
        ecriture.setDateOperation(dateOperation);
        ecriture.setTypeJournal(typeJournal);
        ecriture.setOrigine(origine);
        ecriture.setDebit(debit);
        ecriture.setCredit(credit);
        ecriture.setReference(reference);
        ecriture.setDescription(description);
        ecriture.setTypeSource(typeSource);
        ecriture.setIdSource(idSource);
        return ecriture;
    }

    // ================================================================
    // Écritures inter-modules (appelées par les autres modules)
    // ================================================================

    public JournalFinancierModel enregistrerVente(
            LocalDateTime dateOperation,
            BigDecimal montant,
            String reference,
            String description,
            String typeSource,
            Long idSource) {

        TypeJournalModel typeVente = typeJournalRepo.findByCode("VTE")
                .orElseThrow(() -> new RuntimeException("Type journal VTE introuvable"));

        OrigineModel origineCommande = origineRepo.findByCode("COMMANDE")
                .orElseThrow(() -> new RuntimeException("Origine COMMANDE introuvable"));

        return enregistrer(creerEcriture(
            dateOperation,
            typeVente,
            origineCommande,
            montant,
            BigDecimal.ZERO,
            reference,
            description,
            typeSource,
            idSource));
    }

    public JournalFinancierModel enregistrerPaiement(
            LocalDateTime dateOperation,
            BigDecimal montant,
            String reference,
            String description,
            String typeMethodePaiement,
            String typeSource,
            Long idSource) {

        TypeJournalModel typePaiement = typeJournalRepo.findByCode(typeMethodePaiement)
                .orElseThrow(() -> new RuntimeException(
                        "Type journal " + typeMethodePaiement + " introuvable"));

        OrigineModel originePaiement = origineRepo.findByCode("PAIEMENT")
                .orElseThrow(() -> new RuntimeException("Origine PAIEMENT introuvable"));

        return enregistrer(creerEcriture(
            dateOperation,
            typePaiement,
            originePaiement,
            montant,
            BigDecimal.ZERO,
            reference,
            description,
            typeSource,
            idSource));
    }

    public JournalFinancierModel enregistrerAchat(
            LocalDateTime dateOperation,
            BigDecimal montant,
            String reference,
            String description,
            String typeSource,
            Long idSource) {

        TypeJournalModel typeAchat = typeJournalRepo.findByCode("ACH")
                .orElseThrow(() -> new RuntimeException("Type journal ACH introuvable"));

        OrigineModel origineAchat = origineRepo.findByCode("ACHAT_FOURNISSEUR")
                .orElseThrow(() -> new RuntimeException("Origine ACHAT_FOURNISSEUR introuvable"));

        return enregistrer(creerEcriture(
            dateOperation,
            typeAchat,
            origineAchat,
            BigDecimal.ZERO,
            montant,
            reference,
            description,
            typeSource,
            idSource));
    }

    public JournalFinancierModel enregistrerFraisLivraison(
            LocalDateTime dateOperation,
            BigDecimal montant,
            String reference,
            String description,
            String typeSource,
            Long idSource) {

        TypeJournalModel typeVente = typeJournalRepo.findByCode("VTE")
                .orElseThrow(() -> new RuntimeException("Type journal VTE introuvable"));

        OrigineModel origineFrais = origineRepo.findByCode("FRAIS_LIVRAISON")
                .orElseThrow(() -> new RuntimeException("Origine FRAIS_LIVRAISON introuvable"));

        return enregistrer(creerEcriture(
            dateOperation,
            typeVente,
            origineFrais,
            montant,
            BigDecimal.ZERO,
            reference,
            description,
            typeSource,
            idSource));
    }

    public JournalFinancierModel enregistrerSortieStock(
            LocalDateTime dateOperation,
            BigDecimal montant,
            String reference,
            String description,
            String typeSource,
            Long idSource) {

        TypeJournalModel typeOD = typeJournalRepo.findByCode("OD")
                .orElseThrow(() -> new RuntimeException("Type journal OD introuvable"));

        OrigineModel origineSortie = origineRepo.findByCode("SORTIE_STOCK")
                .orElseThrow(() -> new RuntimeException("Origine SORTIE_STOCK introuvable"));

        return enregistrer(creerEcriture(
            dateOperation,
            typeOD,
            origineSortie,
            BigDecimal.ZERO,
            montant,
            reference,
            description,
            typeSource,
            idSource));
    }

    public JournalFinancierModel enregistrerPaiementSalaire(
            LocalDateTime dateOperation,
            BigDecimal montant,
            String reference,
            String description,
            String typeSource,
            Long idSource) {

        TypeJournalModel typeCSS = typeJournalRepo.findByCode("CSS")
                .orElseThrow(() -> new RuntimeException("Type journal CSS introuvable"));

        OrigineModel origineSalaire = origineRepo.findByCode("PAIEMENT_SALAIRE")
                .orElseThrow(() -> new RuntimeException("Origine PAIEMENT_SALAIRE introuvable"));

        return enregistrer(creerEcriture(
            dateOperation,
            typeCSS,
            origineSalaire,
            BigDecimal.ZERO,
            montant,
            reference,
            description,
            typeSource,
            idSource));
    }
}
