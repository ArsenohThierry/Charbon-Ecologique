package com.example.charbonecolo.controller;

import com.example.charbonecolo.service.JournalFinancierService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.charbonecolo.service.ExportBilanService;
import org.springframework.http.HttpHeaders;
        import org.springframework.http.MediaType;
        import org.springframework.http.ResponseEntity;


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
    @GetMapping
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
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel() throws Exception {

        byte[] file = exportBilanService.exportBilanExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=bilan_financier.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    @GetMapping("/export-pdf")
    public ResponseEntity<byte[]> exportPdf() throws Exception {

        byte[] file = exportBilanService.exportBilanPdf();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=bilan_financier.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }
}
