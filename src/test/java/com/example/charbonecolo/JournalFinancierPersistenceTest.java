package com.example.charbonecolo;

import com.example.charbonecolo.model.JournalFinancierModel;
import com.example.charbonecolo.model.TypeJournalModel;
import com.example.charbonecolo.model.OrigineModel;
import com.example.charbonecolo.repository.JournalFinancierRepository;
import com.example.charbonecolo.repository.TypeJournalRepository;
import com.example.charbonecolo.repository.OrigineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
class JournalFinancierPersistenceTest {

    @Autowired
    private JournalFinancierRepository journalRepo;

    @Autowired
    private TypeJournalRepository typeJournalRepo;

    @Autowired
    private OrigineRepository origineRepo;

    private TypeJournalModel typeVente;
    private OrigineModel origineCommande;

    @BeforeEach
    void setUp() {
        typeVente = typeJournalRepo.findByCode("VTE")
                .orElseGet(() -> {
                    TypeJournalModel t = new TypeJournalModel();
                    t.setLibelle("Vente");
                    t.setCode("VTE");
                    return typeJournalRepo.save(t);
                });

        origineCommande = origineRepo.findByCode("COMMANDE")
                .orElseGet(() -> {
                    OrigineModel o = new OrigineModel();
                    o.setLibelle("Commande");
                    o.setCode("COMMANDE");
                    return origineRepo.save(o);
                });
    }

    @Test
    void testCreationEcriture() {
        JournalFinancierModel ecriture = new JournalFinancierModel();
        ecriture.setDateOperation(LocalDateTime.now());
        ecriture.setTypeJournal(typeVente);
        ecriture.setOrigine(origineCommande);
        ecriture.setDebit(new BigDecimal("150000.00"));
        ecriture.setCredit(BigDecimal.ZERO);
        ecriture.setReference("CMD-TEST-001");
        ecriture.setDescription("Test création écriture");
        ecriture.setTypeSource("FACTURE");
        ecriture.setIdSource(1L);

        JournalFinancierModel saved = journalRepo.save(ecriture);

        assertNotNull(saved.getId());
        assertEquals(new BigDecimal("150000.00"), saved.getDebit());
        assertEquals(BigDecimal.ZERO, saved.getCredit());
        assertEquals("CMD-TEST-001", saved.getReference());
        assertEquals("FACTURE", saved.getTypeSource());
        assertEquals(1L, saved.getIdSource());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void testRefusDoublon() {
        JournalFinancierModel ecriture1 = new JournalFinancierModel();
        ecriture1.setDateOperation(LocalDateTime.now());
        ecriture1.setTypeJournal(typeVente);
        ecriture1.setOrigine(origineCommande);
        ecriture1.setDebit(new BigDecimal("200000.00"));
        ecriture1.setCredit(BigDecimal.ZERO);
        ecriture1.setReference("CMD-DOUBLON-001");
        journalRepo.save(ecriture1);

        assertTrue(journalRepo.existsByReferenceAndOrigine_Id(
                "CMD-DOUBLON-001", origineCommande.getId()));

        JournalFinancierModel ecriture2 = new JournalFinancierModel();
        ecriture2.setDateOperation(LocalDateTime.now());
        ecriture2.setTypeJournal(typeVente);
        ecriture2.setOrigine(origineCommande);
        ecriture2.setDebit(new BigDecimal("200000.00"));
        ecriture2.setCredit(BigDecimal.ZERO);
        ecriture2.setReference("CMD-DOUBLON-001");

        assertThrows(Exception.class, () -> journalRepo.save(ecriture2));
    }

    @Test
    void testLectureEcritures() {
        int countBefore = journalRepo.findAll().size();

        JournalFinancierModel e1 = new JournalFinancierModel();
        e1.setDateOperation(LocalDateTime.now());
        e1.setTypeJournal(typeVente);
        e1.setOrigine(origineCommande);
        e1.setDebit(new BigDecimal("50000.00"));
        e1.setCredit(BigDecimal.ZERO);
        e1.setReference("CMD-LECTURE-001");
        e1.setDescription("Écriture de test lecture");
        journalRepo.save(e1);

        JournalFinancierModel e2 = new JournalFinancierModel();
        e2.setDateOperation(LocalDateTime.now().plusMinutes(1));
        e2.setTypeJournal(typeVente);
        e2.setOrigine(origineCommande);
        e2.setDebit(BigDecimal.ZERO);
        e2.setCredit(new BigDecimal("30000.00"));
        e2.setReference("CMD-LECTURE-002");
        e2.setDescription("Deuxième écriture de test");
        journalRepo.save(e2);

        assertEquals(countBefore + 2, journalRepo.findAll().size());

        Optional<JournalFinancierModel> found = journalRepo.findByReferenceAndOrigine_Id(
                "CMD-LECTURE-001", origineCommande.getId());
        assertTrue(found.isPresent());
        assertEquals(new BigDecimal("50000.00"), found.get().getDebit());
    }

    @Test
    void testValidationContraintesDebitCredit() {
        JournalFinancierModel e = new JournalFinancierModel();
        e.setDateOperation(LocalDateTime.now());
        e.setTypeJournal(typeVente);
        e.setOrigine(origineCommande);
        e.setDebit(new BigDecimal("10000.00"));
        e.setCredit(new BigDecimal("5000.00"));
        e.setReference("CMD-CONTRAINTE-001");

        assertThrows(Exception.class, () -> journalRepo.save(e));
    }

    @Test
    void testCreatedAtAuto() {
        JournalFinancierModel e = new JournalFinancierModel();
        e.setDateOperation(LocalDateTime.now());
        e.setTypeJournal(typeVente);
        e.setOrigine(origineCommande);
        e.setDebit(new BigDecimal("10000.00"));
        e.setCredit(BigDecimal.ZERO);
        e.setReference("CMD-TIMESTAMP-001");

        JournalFinancierModel saved = journalRepo.save(e);
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void testTypeSourceEtIdSource() {
        JournalFinancierModel e = new JournalFinancierModel();
        e.setDateOperation(LocalDateTime.now());
        e.setTypeJournal(typeVente);
        e.setOrigine(origineCommande);
        e.setDebit(new BigDecimal("75000.00"));
        e.setCredit(BigDecimal.ZERO);
        e.setReference("CMD-SOURCE-001");
        e.setTypeSource("FACTURE");
        e.setIdSource(42L);

        JournalFinancierModel saved = journalRepo.save(e);
        assertEquals("FACTURE", saved.getTypeSource());
        assertEquals(42L, saved.getIdSource());

        var found = journalRepo.findByTypeSourceAndIdSourceOrderByDateOperationDesc("FACTURE", 42L);
        assertFalse(found.isEmpty());
    }

    @Test
    void testRechercheParReference() {
        JournalFinancierModel e = new JournalFinancierModel();
        e.setDateOperation(LocalDateTime.now());
        e.setTypeJournal(typeVente);
        e.setOrigine(origineCommande);
        e.setDebit(new BigDecimal("100000.00"));
        e.setCredit(BigDecimal.ZERO);
        e.setReference("CMD-REF-SEARCH-001");
        e.setDescription("Test recherche par référence");
        journalRepo.save(e);

        var results = journalRepo.findByReferenceContainingOrderByDateOperationDesc("REF-SEARCH");
        assertFalse(results.isEmpty());
        assertEquals("CMD-REF-SEARCH-001", results.get(0).getReference());
    }

    @Test
    void testRechercheParTypeJournalCode() {
        JournalFinancierModel e = new JournalFinancierModel();
        e.setDateOperation(LocalDateTime.now());
        e.setTypeJournal(typeVente);
        e.setOrigine(origineCommande);
        e.setDebit(new BigDecimal("80000.00"));
        e.setCredit(BigDecimal.ZERO);
        e.setReference("CMD-TYPECODE-001");
        journalRepo.save(e);

        var results = journalRepo.findByTypeJournal_CodeOrderByDateOperationDesc("VTE");
        assertFalse(results.isEmpty());
    }
}
