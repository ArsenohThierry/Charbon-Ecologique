package com.example.charbonecolo.controller;

import com.example.charbonecolo.model.JournalFinancierModel;
import com.example.charbonecolo.repository.OrigineRepository;
import com.example.charbonecolo.repository.TypeJournalRepository;
import com.example.charbonecolo.service.JournalFinancierService;
import com.example.charbonecolo.service.ExportFinanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Controller
@RequestMapping("/finance/journal")
public class JournalController {

    private final JournalFinancierService journalService;
    private final TypeJournalRepository typeJournalRepo;
    private final OrigineRepository origineRepo;
    private final ExportFinanceService exportFinanceService;
    
    public JournalController(JournalFinancierService journalService,
                             TypeJournalRepository typeJournalRepo,
                             OrigineRepository origineRepo,
                             ExportFinanceService exportFinanceService) {
        this.journalService = journalService;
        this.typeJournalRepo = typeJournalRepo;
        this.origineRepo = origineRepo;
        this.exportFinanceService = exportFinanceService;
    }

    @GetMapping
    public String afficherJournal(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam(required = false) Integer typeJournalId,
            @RequestParam(defaultValue = "1")
            int page,

            @RequestParam(defaultValue = "10")
            int limit,

            Model model) {

        int pageCourante = Math.max(page, 1);
        int limite = normaliserLimite(limit);
        Pageable pageable = PageRequest.of(pageCourante - 1, limite);
        Page<JournalFinancierModel> ecritures;

        if ((debut == null && fin != null) || (debut != null && fin == null)) {
            model.addAttribute("error", "Veuillez renseigner la date de debut et la date de fin.");
            ecritures = journalService.findAll(pageable);
        }
        else if (debut != null && debut.isAfter(fin)) {
            model.addAttribute("error", "La date de debut doit etre inferieure ou egale a la date de fin.");
            ecritures = journalService.findAll(pageable);
        }
        else if (debut != null) {
            ecritures =
                    journalService.filtrerJournal(
                            debut,
                            fin,
                            pageable);
        }
        else if (typeJournalId != null) {
            if (typeJournalRepo.existsById(typeJournalId)) {
                ecritures =
                        journalService.filtrerParType(
                                typeJournalId,
                                pageable);
            } else {
                model.addAttribute("error", "Le type de journal selectionne n'existe pas.");
                ecritures = journalService.findAll(pageable);
            }
        }
        else {
            ecritures =
                    journalService.findAll(pageable);
        }
        

        //model.addAttribute("ecritures", ecritures);
        model.addAttribute(
            "ecritures",
            ecritures.getContent());

        model.addAttribute(
                "page",
                ecritures);

        model.addAttribute(
                "currentPage",
                pageCourante);

        model.addAttribute(
                "limit",
                limite);
        model.addAttribute("typesJournal", typeJournalRepo.findAll());
        model.addAttribute("origines", origineRepo.findAll());
        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        model.addAttribute("typeJournalId", typeJournalId);
        return "stitch/module_finance/journal";
    }

    @PostMapping
    public String enregistrer(
            @RequestParam("dateOperation") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateOperation,
            @RequestParam("typeJournalId") Integer typeJournalId,
            @RequestParam(value = "origineId", required = false) Integer origineId,
            @RequestParam(value = "debit", defaultValue = "0") BigDecimal debit,
            @RequestParam(value = "credit", defaultValue = "0") BigDecimal credit,
            @RequestParam(value = "reference", required = false) String reference,
            @RequestParam(value = "description", required = false) String description,
            RedirectAttributes redirectAttributes) {

        if (debit == null || credit == null || debit.signum() < 0 || credit.signum() < 0) {
            redirectAttributes.addFlashAttribute("error", "Les montants debit et credit doivent etre positifs.");
            return "redirect:/finance/journal";
        }

        if (debit.compareTo(BigDecimal.ZERO) == 0 && credit.compareTo(BigDecimal.ZERO) == 0) {
            redirectAttributes.addFlashAttribute("error", "Veuillez renseigner un debit ou un credit.");
            return "redirect:/finance/journal";
        }

        JournalFinancierModel ecriture = new JournalFinancierModel();
        ecriture.setDateOperation(dateOperation);
        ecriture.setDebit(debit);
        ecriture.setCredit(credit);
        ecriture.setReference(reference);
        ecriture.setDescription(description);

        var typeJournal = typeJournalRepo.findById(typeJournalId);
        if (typeJournal.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Le type de journal selectionne n'existe pas.");
            return "redirect:/finance/journal";
        }
        ecriture.setTypeJournal(typeJournal.get());

        if (origineId != null) {
            var origine = origineRepo.findById(origineId);
            if (origine.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "L'origine selectionnee n'existe pas.");
                return "redirect:/finance/journal";
            }
            ecriture.setOrigine(origine.get());
        }

        journalService.enregistrer(ecriture);
        redirectAttributes.addFlashAttribute("success", "Ecriture enregistree avec succes.");
        return "redirect:/finance/journal";
    }

    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exportCsv() throws Exception {
        List<JournalFinancierModel> ecritures = journalService.findAll();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

        baos.write(0xEF);
        baos.write(0xBB);
        baos.write(0xBF);

        writer.write("ID;Date;Type Journal;Origine;Debit;Credit;Reference;Description\n");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (JournalFinancierModel e : ecritures) {
            writer.write(String.format("%d;%s;%s;%s;%s;%s;%s;%s\n",
                    e.getId(),
                    e.getDateOperation().format(fmt),
                    e.getTypeJournal().getLibelle(),
                    e.getOrigine() != null ? e.getOrigine().getLibelle() : "",
                    e.getDebit().toPlainString(),
                    e.getCredit().toPlainString(),
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

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel() throws Exception {

        byte[] file = exportFinanceService.exportJournalExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=journal.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    private int normaliserLimite(int limit) {
        return switch (limit) {
            case 5, 10, 25, 50 -> limit;
            default -> 10;
        };
    }
}
