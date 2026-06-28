package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.JournalFinancierModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface JournalFinancierRepository extends JpaRepository<JournalFinancierModel, Integer> {

    List<JournalFinancierModel> findByDateOperationBetween(
        LocalDateTime debut, LocalDateTime fin);

    List<JournalFinancierModel> findByTypeJournalLibelleAndDateOperationBetween(
        String libelle, LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT SUM(j.credit) FROM JournalFinancierModel j " +
           "WHERE j.typeJournal.libelle = 'Vente' " +
           "AND j.dateOperation BETWEEN :debut AND :fin")
    BigDecimal calculerCA(@Param("debut") LocalDateTime debut,
                          @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(j.credit) - SUM(j.debit) FROM JournalFinancierModel j " +
           "WHERE j.dateOperation BETWEEN :debut AND :fin")
    BigDecimal calculerBenefice(@Param("debut") LocalDateTime debut,
                                @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(j.credit) FROM JournalFinancierModel j " +
           "WHERE j.dateOperation BETWEEN :debut AND :fin")
    BigDecimal calculerTotalEntrees(@Param("debut") LocalDateTime debut,
                                    @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(j.debit) FROM JournalFinancierModel j " +
           "WHERE j.dateOperation BETWEEN :debut AND :fin")
    BigDecimal calculerTotalSorties(@Param("debut") LocalDateTime debut,
                                    @Param("fin") LocalDateTime fin);

    @Query(value = "SELECT DATE_TRUNC('month', date_operation) as mois, " +
                   "SUM(credit) as ca FROM journal_financier " +
                   "JOIN type_journal tj ON id_type_journal = tj.id " +
                   "WHERE tj.libelle = 'Vente' " +
                   "GROUP BY mois ORDER BY mois", nativeQuery = true)
    List<Object[]> evolutionCA();
}