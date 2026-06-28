package com.example.charbonecolo.controller;

import com.example.charbonecolo.repository.TypeJournalRepository;
import com.example.charbonecolo.service.JournalFinancierService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Controller
@RequestMapping("/finance")
public class JournalController {

    private final JournalFinancierService financeService;
    private final TypeJournalRepository typeJournalRepo;

    public JournalController(JournalFinancierService financeService, TypeJournalRepository typeJournalRepo) {
        this.financeService = financeService;
        this.typeJournalRepo = typeJournalRepo;
    }

    @GetMapping("/journal")
    public String afficher(
            @RequestParam(required = false) String type,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        LocalDateTime d = financeService.calculerDateDebut(debut, 1);
        LocalDateTime f = financeService.calculerDateFin(fin);

        model.addAttribute("lignes", financeService.filtrerJournal(type, d, f));
        model.addAttribute("types", typeJournalRepo.findAll());
        model.addAttribute("typeSelectionne", type);
        return "stitch/module_finance/journal";
    }

    @GetMapping("/journal/export-csv")
    public ResponseEntity<byte[]> exportCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("Date,Type,Référence,Description,Débit,Crédit\n");
        financeService.filtrerJournal(null,
            LocalDateTime.now().minusYears(1), LocalDateTime.now())
            .forEach(l -> sb
                .append(l.getDateOperation()).append(",")
                .append(escapeCSV(l.getTypeJournal().getLibelle())).append(",")
                .append(escapeCSV(l.getReference())).append(",")
                .append(escapeCSV(l.getDescription())).append(",")
                .append(l.getDebit()).append(",")
                .append(l.getCredit()).append("\n"));
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=journal.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(bytes);
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        String s = value.trim();
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}