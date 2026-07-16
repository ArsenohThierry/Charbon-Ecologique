package com.example.charbonecolo.repository;

import java.util.Optional;
import com.example.charbonecolo.model.JournalFinancierModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface JournalFinancierRepository extends JpaRepository<JournalFinancierModel, Long> {

    List<JournalFinancierModel> findAllByOrderByDateOperationDesc();

    List<JournalFinancierModel> findAllByOrderByDateOperationAscIdAsc();

    List<JournalFinancierModel> findByDateOperationBetweenOrderByDateOperationDesc(
            LocalDateTime debut, LocalDateTime fin);

    List<JournalFinancierModel> findByTypeJournal_IdOrderByDateOperationDesc(Integer typeJournalId);

    List<JournalFinancierModel> findByOrigine_IdOrderByDateOperationDesc(Integer origineId);

    // Méthode pour la pagination
    Page<JournalFinancierModel> findAllByOrderByDateOperationDesc(Pageable pageable);
    Page<JournalFinancierModel> findByDateOperationBetweenOrderByDateOperationDesc(LocalDateTime debut, LocalDateTime   fin, Pageable pageable);
    Page<JournalFinancierModel> findByTypeJournal_IdOrderByDateOperationDesc(Integer typeJournalId, Pageable pageable);

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

    @Query("SELECT COALESCE(SUM(j.debit), 0) - COALESCE(SUM(j.credit), 0) FROM JournalFinancierModel j")
    BigDecimal calculerSolde();

    @Query(value = "SELECT TO_CHAR(j.date_operation, 'YYYY-MM') AS mois, SUM(j.debit) AS total " +
                   "FROM journal_financier j " +
                   "JOIN type_journal tj ON j.id_type_journal = tj.id " +
                   "WHERE tj.code IN ('VTE', 'VENTE') AND j.date_operation >= :debut " +
                   "GROUP BY mois ORDER BY mois",
           nativeQuery = true)
    List<Map<String, Object>> evolutionMensuelleCA(@Param("debut") LocalDateTime debut);

    @Query(value = "SELECT TO_CHAR(j.date_operation, 'IYYY-\"W\"IW') AS mois, SUM(j.debit) AS total " +
                   "FROM journal_financier j " +
                   "JOIN type_journal tj ON j.id_type_journal = tj.id " +
                   "WHERE tj.code IN ('VTE', 'VENTE') AND j.date_operation >= :debut " +
                   "GROUP BY mois ORDER BY mois",
           nativeQuery = true)
    List<Map<String, Object>> evolutionHebdomadaireCA(@Param("debut") LocalDateTime debut);

    @Query(value = "SELECT TO_CHAR(j.date_operation, 'YYYY') AS mois, SUM(j.debit) AS total " +
                   "FROM journal_financier j " +
                   "JOIN type_journal tj ON j.id_type_journal = tj.id " +
                   "WHERE tj.code IN ('VTE', 'VENTE') AND j.date_operation >= :debut " +
                   "GROUP BY mois ORDER BY mois",
           nativeQuery = true)
    List<Map<String, Object>> evolutionAnnuelleCA(@Param("debut") LocalDateTime debut);
    // Vérifie si une écriture existe déjà
       boolean existsByReferenceAndOrigine_Id(String reference, Integer origineId); 
       // Recherche une écriture par référence et origine
       Optional<JournalFinancierModel> findByReferenceAndOrigine_Id(
              String reference,
              Integer origineId
       );     
       // Recherche toutes les écritures provenant d'un même objet métier
       List<JournalFinancierModel> findByTypeSourceAndIdSourceOrderByDateOperationDesc(
              String typeSource,
              Long idSource
       );     
       // Recherche paginée
       Page<JournalFinancierModel> findByTypeSourceAndIdSource(
              String typeSource,
              Long idSource,
              Pageable pageable
       );
       Optional<JournalFinancierModel> findByIdSourceAndTypeSource(
               Long idSource,
               String typeSource
       );

       List<JournalFinancierModel> findByReferenceContainingOrderByDateOperationDesc(
              String reference
       );

       List<JournalFinancierModel> findByTypeJournal_CodeOrderByDateOperationDesc(
              String code
       );

       List<JournalFinancierModel> findByOrigine_CodeOrderByDateOperationDesc(
              String code
       );
}
