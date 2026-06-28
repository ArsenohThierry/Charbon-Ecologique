package com.example.charbonecolo.service;

import com.example.charbonecolo.config.Constantes;
import com.example.charbonecolo.model.*;
import com.example.charbonecolo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service gérant les opérations du journal financier et de la trésorerie.
 */
@Service
@Transactional
public class JournalFinancierService {

    private final JournalFinancierRepository journalRepo;
    private final TresorerieRepository tresorerieRepo;
    private final TypeJournalRepository typeJournalRepo;
    private final OrigineRepository origineRepo;

    public JournalFinancierService(JournalFinancierRepository journalRepo, TresorerieRepository tresorerieRepo, TypeJournalRepository typeJournalRepo, OrigineRepository origineRepo) {
        this.journalRepo = journalRepo;
        this.tresorerieRepo = tresorerieRepo;
        this.typeJournalRepo = typeJournalRepo;
        this.origineRepo = origineRepo;
    }

    /**
     * Enregistre une opération dans le journal financier à la date courante.
     * Cette méthode synchronise automatiquement la trésorerie si nécessaire.
     *
     * @param typeLibelle    Le libellé du type de journal (ex: Vente, Achat)
     * @param origineLibelle Le libellé de l'origine de l'écriture (ex: Paiement, Achat)
     * @param reference      La référence unique de l'opération
     * @param debit          Le montant au débit
     * @param credit         Le montant au crédit
     * @param description    La description textuelle de l'opération
     */
    public void enregistrer(String typeLibelle, String origineLibelle,
                            String reference, BigDecimal debit,
                            BigDecimal credit, String description) {
        enregistrerAvecDate(typeLibelle, origineLibelle, reference, debit, credit, description, LocalDateTime.now());
    }

    /**
     * Enregistre une opération dans le journal financier avec une date spécifiée.
     * Cette méthode synchronise automatiquement la trésorerie si nécessaire.
     *
     * @param typeLibelle    Le libellé du type de journal
     * @param origineLibelle Le libellé de l'origine de l'écriture
     * @param reference      La référence unique de l'opération
     * @param debit          Le montant au débit
     * @param credit         Le montant au crédit
     * @param description    La description textuelle
     * @param dateOperation  La date de l'opération
     */
    public void enregistrerAvecDate(String typeLibelle, String origineLibelle,
                                    String reference, BigDecimal debit,
                                    BigDecimal credit, String description, LocalDateTime dateOperation) {
        TypeJournalModel type = typeJournalRepo.findByLibelle(typeLibelle)
            .orElseThrow(() -> new RuntimeException("Type journal introuvable : " + typeLibelle));
        OrigineModel origine = origineRepo.findByLibelle(origineLibelle)
            .orElseThrow(() -> new RuntimeException("Origine introuvable : " + origineLibelle));

        JournalFinancierModel ligne = new JournalFinancierModel();
        ligne.setReference(reference);
        ligne.setDateOperation(dateOperation != null ? dateOperation : LocalDateTime.now());
        ligne.setTypeJournal(type);
        ligne.setOrigine(origine);
        ligne.setDebit(debit != null ? debit : BigDecimal.ZERO);
        ligne.setCredit(credit != null ? credit : BigDecimal.ZERO);
        ligne.setDescription(description);
        journalRepo.save(ligne);

        // Synchronisation automatique de la trésorerie
        if (credit != null && credit.compareTo(BigDecimal.ZERO) > 0) {
            TresorerieModel t = new TresorerieModel();
            t.setDateOperation(dateOperation != null ? dateOperation : LocalDateTime.now());
            t.setTypeOperation(Constantes.TRESORERIE_ENTREE);
            t.setMontant(credit);
            t.setOrigine(origine.getLibelle());
            t.setReferenceOrigine(reference);
            t.setDescription(description);
            tresorerieRepo.save(t);
        } else if (debit != null && debit.compareTo(BigDecimal.ZERO) > 0) {
            TresorerieModel t = new TresorerieModel();
            t.setDateOperation(dateOperation != null ? dateOperation : LocalDateTime.now());
            t.setTypeOperation(Constantes.TRESORERIE_SORTIE);
            t.setMontant(debit);
            t.setOrigine(origine.getLibelle());
            t.setReferenceOrigine(reference);
            t.setDescription(description);
            tresorerieRepo.save(t);
        }
    }

    /**
     * Calcule la date de début par défaut si elle n'est pas fournie.
     * Par défaut, recule d'un nombre donné de mois, ou du début de l'année si moisMoins est négatif.
     *
     * @param debut la date de début fournie
     * @param moisMoins le nombre de mois à soustraire si la date est nulle
     * @return la date de début calculée
     */
    public LocalDateTime calculerDateDebut(LocalDate debut, int moisMoins) {
        if (debut != null) {
            return debut.atStartOfDay();
        }
        if (moisMoins < 0) {
            return LocalDateTime.now().withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
        return LocalDateTime.now().minusMonths(moisMoins).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Calcule la date de fin par défaut si elle n'est pas fournie.
     * Par défaut, retourne la fin de la journée courante.
     *
     * @param fin la date de fin fournie
     * @return la date de fin calculée
     */
    public LocalDateTime calculerDateFin(LocalDate fin) {
        if (fin != null) {
            return fin.atTime(23, 59, 59);
        }
        return LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    public List<JournalFinancierModel> filtrerJournal(
            String type, LocalDateTime debut, LocalDateTime fin) {
        if (type != null && !type.isBlank()) {
            return journalRepo.findByTypeJournalLibelleAndDateOperationBetween(type, debut, fin);
        }
        return journalRepo.findByDateOperationBetween(debut, fin);
    }

    public BigDecimal calculerCA(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal v = journalRepo.calculerCA(debut, fin);
        return v != null ? v : BigDecimal.ZERO;
    }

    public BigDecimal calculerBenefice(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal v = journalRepo.calculerBenefice(debut, fin);
        return v != null ? v : BigDecimal.ZERO;
    }

    public BigDecimal calculerTotalEntrees(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal v = journalRepo.calculerTotalEntrees(debut, fin);
        return v != null ? v : BigDecimal.ZERO;
    }

    public BigDecimal calculerTotalSorties(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal v = journalRepo.calculerTotalSorties(debut, fin);
        return v != null ? v : BigDecimal.ZERO;
    }

    public BigDecimal calculerSolde() {
        BigDecimal v = tresorerieRepo.calculerSolde();
        return v != null ? v : BigDecimal.ZERO;
    }

    public List<Object[]> evolutionCA() {
        return journalRepo.evolutionCA();
    }
}