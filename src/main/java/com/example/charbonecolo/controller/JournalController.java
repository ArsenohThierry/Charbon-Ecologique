package com.example.charbonecolo.controller;

import com.example.charbonecolo.model.JournalFinancierModel;
import com.example.charbonecolo.model.OrigineModel;
import com.example.charbonecolo.model.TypeJournalModel;
import com.example.charbonecolo.repository.OrigineRepository;
import com.example.charbonecolo.repository.TypeJournalRepository;
import com.example.charbonecolo.service.JournalFinancierService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/finance/journal")
public class JournalController {

    private final JournalFinancierService journalService;
    private final TypeJournalRepository typeJournalRepo;
    private final OrigineRepository origineRepo;

    public JournalController(JournalFinancierService journalService,
                              TypeJournalRepository typeJournalRepo,
                              OrigineRepository origineRepo) {
        this.journalService = journalService;
        this.typeJournalRepo = typeJournalRepo;
        this.origineRepo = origineRepo;
    }

    /** GET /finance/journal — Affiche la liste du journal */
    @GetMapping
    public String afficherJournal(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam(required = false) Integer typeJournalId,
            Model model) {

        List<JournalFinancierModel> ecritures;

        if (debut != null && fin != null) {
            ecritures = journalService.filtrerJournal(debut, fin);
        } else if (typeJournalId != null) {
            ecritures = journalService.filtrerParType(typeJournalId);
        } else {
            ecritures = journalService.findAll();
        }

        model.addAttribute("ecritures", ecritures);
        model.addAttribute("typesJournal", typeJournalRepo.findAll());
        model.addAttribute("origines", origineRepo.findAll());
        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        model.addAttribute("typeJournalId", typeJournalId);
        return "stitch/module_finance/journal";
    }

    /** POST /finance/journal — Enregistre une nouvelle écriture */
    @PostMapping
    public String enregistrer(
            @RequestParam("dateOperation") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateOperation,
            @RequestParam("typeJournalId") Integer typeJournalId,
            @RequestParam(value = "origineId", required = false) Integer origineId,
            @RequestParam("montant") BigDecimal montant,
            @RequestParam(value = "devise", defaultValue = "MGA") String devise,
            @RequestParam(value = "reference", required = false) String reference,
            @RequestParam(value = "description", required = false) String description) {

        JournalFinancierModel ecriture = new JournalFinancierModel();
        ecriture.setDateOperation(dateOperation);
        ecriture.setMontant(montant);
        ecriture.setDevise(devise);
        ecriture.setReference(reference);
        ecriture.setDescription(description);

        typeJournalRepo.findById(typeJournalId).ifPresent(ecriture::setTypeJournal);
        if (origineId != null) {
            origineRepo.findById(origineId).ifPresent(ecriture::setOrigine);
        }

        journalService.enregistrer(ecriture);
        return "redirect:/finance/journal";
    }

    /** GET /finance/journal/export-csv — Export CSV du journal */
    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exportCsv() throws Exception {
        List<JournalFinancierModel> ecritures = journalService.findAll();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

        // BOM UTF-8 pour Excel
        baos.write(0xEF);
        baos.write(0xBB);
        baos.write(0xBF);

        writer.write("ID;Date;Type Journal;Origine;Montant;Devise;Référence;Description\n");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (JournalFinancierModel e : ecritures) {
            writer.write(String.format("%d;%s;%s;%s;%s;%s;%s;%s\n",
                    e.getId(),
                    e.getDateOperation().format(fmt),
                    e.getTypeJournal().getLibelle(),
                    e.getOrigine() != null ? e.getOrigine().getLibelle() : "",
                    e.getMontant().toPlainString(),
                    e.getDevise() != null ? e.getDevise() : "",
                    e.getReference() != null ? e.getReference() : "",
                    e.getDescription() != null ? e.getDescription() : ""
            ));
        }
        writer.flush();

        byte[] bytes = baos.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"journal_financier.csv\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(bytes);
    }
}
