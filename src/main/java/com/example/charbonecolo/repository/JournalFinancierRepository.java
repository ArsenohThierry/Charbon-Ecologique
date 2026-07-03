package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.JournalFinancierModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JournalFinancierRepository extends JpaRepository<JournalFinancierModel, Long> {

    List<JournalFinancierModel> findAllByOrderByDateOperationDesc();

    List<JournalFinancierModel> findAllByOrderByDateOperationAscIdAsc();

    List<JournalFinancierModel> findByDateOperationBetweenOrderByDateOperationDesc(
            LocalDateTime debut, LocalDateTime fin);

    List<JournalFinancierModel> findByTypeJournal_IdOrderByDateOperationDesc(Integer typeJournalId);

    List<JournalFinancierModel> findByOrigine_IdOrderByDateOperationDesc(Integer origineId);

    @Query("SELECT COALESCE(SUM(j.debit), 0) FROM JournalFinancierModel j " +
           "WHERE j.typeJournal.code IN ('VTE', 'VENTE') AND j.dateOperation BETWEEN :debut AND :fin")
    BigDecimal calculerCA(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(j.debit), 0) - COALESCE(SUM(j.credit), 0) " +
           "FROM JournalFinancierModel j WHERE j.dateOperation BETWEEN :debut AND :fin")
    BigDecimal calculerBenefice(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(j.debit), 0) FROM JournalFinancierModel j " +
           "WHERE j.dateOperation BETWEEN :debut AND :fin")
    BigDecimal calculerTotalEntrees(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(j.credit), 0) FROM JournalFinancierModel j " +
           "WHERE j.dateOperation BETWEEN :debut AND :fin")
    BigDecimal calculerTotalSorties(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    @Query("""
       SELECT COALESCE(SUM(j.debit),0) - COALESCE(SUM(j.credit),0)
       FROM JournalFinancierModel j
       """)
       BigDecimal calculerSolde();

              @Query(value = """
                      SELECT
                                    TO_CHAR(j.date_operation, 'YYYY-MM') AS mois,
                                    COALESCE(SUM(j.debit), 0) AS total
                      FROM journal_financier j
                      JOIN type_journal tj ON tj.id = j.id_type_journal
                      WHERE tj.code IN ('VTE', 'VENTE')
                             AND j.date_operation BETWEEN :debut AND :fin
                      GROUP BY TO_CHAR(j.date_operation, 'YYYY-MM')
                      ORDER BY TO_CHAR(j.date_operation, 'YYYY-MM')
                      """, nativeQuery = true)
              List<Object[]> evolutionMensuelleCA(
                                          @Param("debut") LocalDateTime debut,
                                          @Param("fin") LocalDateTime fin);
}
