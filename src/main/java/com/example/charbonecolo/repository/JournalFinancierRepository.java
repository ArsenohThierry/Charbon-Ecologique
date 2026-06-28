package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.JournalFinancierModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface JournalFinancierRepository extends JpaRepository<JournalFinancierModel, Long> {

    List<JournalFinancierModel> findAllByOrderByDateOperationDesc();

    List<JournalFinancierModel> findByDateOperationBetweenOrderByDateOperationDesc(
            LocalDateTime debut, LocalDateTime fin);

    List<JournalFinancierModel> findByTypeJournal_IdOrderByDateOperationDesc(Integer typeJournalId);

    List<JournalFinancierModel> findByOrigine_IdOrderByDateOperationDesc(Integer origineId);

    // Chiffre d'affaires = somme des montants de type VENTE
    @Query("SELECT COALESCE(SUM(j.montant), 0) FROM JournalFinancierModel j " +
           "WHERE j.typeJournal.code = 'VENTE' AND j.dateOperation BETWEEN :debut AND :fin")
    BigDecimal calculerCA(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    // Bénéfice = VENTE - ACHAT
    @Query("SELECT COALESCE(SUM(CASE WHEN j.typeJournal.code = 'VENTE' THEN j.montant " +
           "WHEN j.typeJournal.code = 'ACHAT' THEN -j.montant ELSE 0 END), 0) " +
           "FROM JournalFinancierModel j WHERE j.dateOperation BETWEEN :debut AND :fin")
    BigDecimal calculerBenefice(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    // Total entrées (BANQUE et CAISSE positifs = VENTE + BANQUE_ENTREE)
    @Query("SELECT COALESCE(SUM(j.montant), 0) FROM JournalFinancierModel j " +
           "WHERE j.typeJournal.code IN ('VENTE', 'BANQUE') AND j.dateOperation BETWEEN :debut AND :fin")
    BigDecimal calculerTotalEntrees(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    // Total sorties (ACHAT)
    @Query("SELECT COALESCE(SUM(j.montant), 0) FROM JournalFinancierModel j " +
           "WHERE j.typeJournal.code = 'ACHAT' AND j.dateOperation BETWEEN :debut AND :fin")
    BigDecimal calculerTotalSorties(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    // Évolution mensuelle du CA (pour graphiques)
    @Query(value = "SELECT TO_CHAR(j.date_operation, 'YYYY-MM') AS mois, SUM(j.montant) AS total " +
                   "FROM journal_financier j " +
                   "JOIN type_journal tj ON j.id_type_journal = tj.id " +
                   "WHERE tj.code = 'VENTE' AND j.date_operation >= :debut " +
                   "GROUP BY mois ORDER BY mois",
           nativeQuery = true)
    List<Map<String, Object>> evolutionMensuelleCA(@Param("debut") LocalDateTime debut);
}
