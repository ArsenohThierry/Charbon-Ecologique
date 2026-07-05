package com.example.charbonecolo.controller;

import com.example.charbonecolo.service.JournalFinancierService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.charbonecolo.service.ExportBilanService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Controller
@RequestMapping("/finance/bilan")
public class BilanController {

    private final JournalFinancierService journalService;
    private final ExportBilanService exportBilanService;
    
    public BilanController(JournalFinancierService journalService, ExportBilanService exportBilanService) {
        this.journalService = journalService;
        this.exportBilanService = exportBilanService;
    }

    /** GET /finance/bilan — Affiche le bilan financier du mois en cours */
    /*@GetMapping
    public String afficherBilan(Model model) {
        LocalDateTime debut = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fin   = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        model.addAttribute("ca",       journalService.calculerCA(debut, fin));
        model.addAttribute("benefice", journalService.calculerBenefice(debut, fin));
        model.addAttribute("entrees",  journalService.calculerTotalEntrees(debut, fin));
        model.addAttribute("sorties",  journalService.calculerTotalSorties(debut, fin));
        model.addAttribute("debut",    debut);
        model.addAttribute("fin",      fin);

        return "stitch/module_finance/bilan";
    }*/
    /*modification billan affichage  */
    @GetMapping
    public String afficherBilan(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate debut,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fin,

        Model model) {

    if (debut == null) {
        debut = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfMonth());
    }

    if (fin == null) {
        fin = LocalDate.now()
                .with(TemporalAdjusters.lastDayOfMonth());
    }

    LocalDateTime dateDebut = debut.atStartOfDay();
    LocalDateTime dateFin = fin.atTime(23, 59, 59);

    model.addAttribute("ca",
                journalService.calculerCA(dateDebut, dateFin));
        model.addAttribute("benefice",
                journalService.calculerBenefice(dateDebut, dateFin));
        model.addAttribute("entrees",
                journalService.calculerTotalEntrees(dateDebut, dateFin));
        model.addAttribute("sorties",
                journalService.calculerTotalSorties(dateDebut, dateFin));

        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);

        return "stitch/module_finance/bilan";
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate debut,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fin) throws Exception {

        if (debut == null) {
        debut = LocalDate.now()
                    .with(TemporalAdjusters.firstDayOfMonth());
        }

        if (fin == null) {
            fin = LocalDate.now()
                    .with(TemporalAdjusters.lastDayOfMonth());
        }

    LocalDateTime dateDebut = debut.atStartOfDay();
    LocalDateTime dateFin = fin.atTime(23, 59, 59);

        byte[] file = exportBilanService.exportBilanExcel(dateDebut,
        dateFin);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=bilan_financier.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    @GetMapping("/export-pdf")
    public ResponseEntity<byte[]> exportPdf( 
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate debut,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fin) throws Exception {

        if (debut == null) {
        debut = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfMonth());
        }

        if (fin == null) {
            fin = LocalDate.now()
                    .with(TemporalAdjusters.lastDayOfMonth());
        }

    LocalDateTime dateDebut = debut.atStartOfDay();
    LocalDateTime dateFin = fin.atTime(23, 59, 59);

        byte[] file = exportBilanService.exportBilanPdf(dateDebut, dateFin);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=bilan_financier.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }
}
