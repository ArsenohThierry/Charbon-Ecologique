package com.example.charbonecolo;

import com.example.charbonecolo.model.JournalFinancierModel;
import com.example.charbonecolo.service.JournalFinancierService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
class JournalFinancierServiceTest {

    @Autowired
    private JournalFinancierService journalService;

    @Test
    void testEnregistrerVente() {
        JournalFinancierModel ecriture = journalService.enregistrerVente(
                LocalDateTime.now(),
                new BigDecimal("500000.00"),
                "CMD-VTE-001",
                "Facture n°FACT-VTE-001 — Client Test",
                "FACTURE",
                1L
        );

        assertNotNull(ecriture.getId());
        assertEquals(new BigDecimal("500000.00"), ecriture.getDebit());
        assertEquals(BigDecimal.ZERO, ecriture.getCredit());
        assertEquals("VTE", ecriture.getTypeJournal().getCode());
        assertEquals("COMMANDE", ecriture.getOrigine().getCode());
        assertEquals("FACTURE", ecriture.getTypeSource());
    }

    @Test
    void testEnregistrerPaiementCarteBancaire() {
        JournalFinancierModel ecriture = journalService.enregistrerPaiement(
                LocalDateTime.now(),
                new BigDecimal("300000.00"),
                "PAI-PAY-001",
                "Paiement Carte bancaire — Commande CMD-PAY-001",
                "BNQ",
                "PAIEMENT",
                1L
        );

        assertNotNull(ecriture.getId());
        assertEquals("BNQ", ecriture.getTypeJournal().getCode());
        assertEquals("PAIEMENT", ecriture.getOrigine().getCode());
        assertEquals(new BigDecimal("300000.00"), ecriture.getDebit());
    }

    @Test
    void testEnregistrerPaiementEspeces() {
        JournalFinancierModel ecriture = journalService.enregistrerPaiement(
                LocalDateTime.now(),
                new BigDecimal("150000.00"),
                "PAI-ESP-001",
                "Paiement Espèces — Commande CMD-ESP-001",
                "CSS",
                "PAIEMENT",
                2L
        );

        assertNotNull(ecriture.getId());
        assertEquals("CSS", ecriture.getTypeJournal().getCode());
        assertEquals("PAIEMENT", ecriture.getOrigine().getCode());
    }

    @Test
    void testEnregistrerAchat() {
        JournalFinancierModel ecriture = journalService.enregistrerAchat(
                LocalDateTime.now(),
                new BigDecimal("250000.00"),
                "LOT-ACH-001",
                "Achat Bois — Fournisseur ABC",
                "MOUVEMENT_STOCK",
                10L
        );

        assertNotNull(ecriture.getId());
        assertEquals("ACH", ecriture.getTypeJournal().getCode());
        assertEquals("ACHAT_FOURNISSEUR", ecriture.getOrigine().getCode());
        assertEquals(BigDecimal.ZERO, ecriture.getDebit());
        assertEquals(new BigDecimal("250000.00"), ecriture.getCredit());
    }

    @Test
    void testEnregistrerFraisLivraison() {
        JournalFinancierModel ecriture = journalService.enregistrerFraisLivraison(
                LocalDateTime.now(),
                new BigDecimal("25000.00"),
                "CMD-FRAIS-001",
                "Frais de livraison — Commande CMD-FRAIS-001",
                "FACTURE",
                5L
        );

        assertNotNull(ecriture.getId());
        assertEquals("ACH", ecriture.getTypeJournal().getCode());
        assertEquals("FRAIS_LIVRAISON", ecriture.getOrigine().getCode());
        assertEquals(new BigDecimal("25000.00"), ecriture.getCredit());
    }

    @Test
    void testDetectionDoublons() {
        journalService.enregistrerVente(
                LocalDateTime.now(),
                new BigDecimal("100000.00"),
                "CMD-DOUBLON-SVC-001",
                "Test détection doublon",
                "FACTURE",
                1L
        );

        boolean doublon = journalService.verifierDoublon(
                "CMD-DOUBLON-SVC-001",
                journalService.rechercherParReference("CMD-DOUBLON-SVC-001")
                        .get(0).getOrigine().getId()
        );
        assertTrue(doublon);
    }

    @Test
    void testEnregistrerVenteDoublonLanceException() {
        journalService.enregistrerVente(
                LocalDateTime.now(),
                new BigDecimal("100000.00"),
                "CMD-ERR-DOUBLON",
                "Première écriture",
                "FACTURE",
                1L
        );

        assertThrows(IllegalArgumentException.class, () ->
                journalService.enregistrerVente(
                        LocalDateTime.now(),
                        new BigDecimal("100000.00"),
                        "CMD-ERR-DOUBLON",
                        "Deuxième écriture (doublon)",
                        "FACTURE",
                        1L
                )
        );
    }

    @Test
    void testCalculerCA() {
        LocalDateTime debut = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime fin = LocalDateTime.now().withDayOfMonth(28).withHour(23).withMinute(59);

        journalService.enregistrerVente(
                LocalDateTime.now(),
                new BigDecimal("100000.00"),
                "CMD-CA-001",
                "Vente pour test CA",
                "FACTURE",
                1L
        );

        BigDecimal ca = journalService.calculerCA(debut, fin);
        assertNotNull(ca);
        assertTrue(ca.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void testCalculerSolde() {
        BigDecimal solde = journalService.calculerSolde();
        assertNotNull(solde);
    }

    @Test
    void testRechercherParSource() {
        journalService.enregistrerVente(
                LocalDateTime.now(),
                new BigDecimal("200000.00"),
                "CMD-SRC-001",
                "Vente pour test source",
                "FACTURE",
                99L
        );

        List<JournalFinancierModel> resultats = journalService.rechercherParSource("FACTURE", 99L);
        assertFalse(resultats.isEmpty());
        assertEquals(99L, resultats.get(0).getIdSource());
    }

    @Test
    void testFiltrerParTypeCode() {
        journalService.enregistrerVente(
                LocalDateTime.now(),
                new BigDecimal("50000.00"),
                "CMD-FILTRE-VTE-001",
                "Vente pour test filtrage",
                "FACTURE",
                1L
        );

        List<JournalFinancierModel> ventes = journalService.filtrerParTypeCode("VTE");
        assertFalse(ventes.isEmpty());
        assertTrue(ventes.stream()
                .allMatch(e -> "VTE".equals(e.getTypeJournal().getCode())));
    }

    @Test
    void testFiltrerParOrigineCode() {
        journalService.enregistrerAchat(
                LocalDateTime.now(),
                new BigDecimal("75000.00"),
                "CMD-FILTRE-ACH-001",
                "Achat pour test filtrage",
                "MOUVEMENT_STOCK",
                1L
        );

        List<JournalFinancierModel> achats = journalService.filtrerParOrigineCode("ACHAT_FOURNISSEUR");
        assertFalse(achats.isEmpty());
        assertTrue(achats.stream()
                .allMatch(e -> "ACHAT_FOURNISSEUR".equals(e.getOrigine().getCode())));
    }

    @Test
    void testExisteDeja() {
        journalService.enregistrerVente(
                LocalDateTime.now(),
                new BigDecimal("300000.00"),
                "CMD-EXISTE-001",
                "Test existeDeja",
                "FACTURE",
                1L
        );

        Integer origineId = journalService.rechercherParReference("CMD-EXISTE-001")
                .get(0).getOrigine().getId();

        assertTrue(journalService.verifierDoublon("CMD-EXISTE-001", origineId));
        assertFalse(journalService.verifierDoublon("CMD-NEXISTE-PAS", origineId));
    }
}
