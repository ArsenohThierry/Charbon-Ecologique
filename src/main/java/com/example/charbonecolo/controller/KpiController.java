package com.example.charbonecolo.controller;

import com.example.charbonecolo.service.JournalFinancierService;
import com.example.charbonecolo.service.TresorerieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/finance/kpi")
public class KpiController {

    private final JournalFinancierService journalService;
    private final TresorerieService tresorerieService;

    public KpiController(JournalFinancierService journalService, TresorerieService tresorerieService) {
        this.journalService = journalService;
        this.tresorerieService = tresorerieService;
    }

    /** GET /finance/kpi — Tableau de bord KPI financier */
    @GetMapping
    public String afficherKpi(Model model) {
        // Mois en cours
        LocalDateTime debutMois = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finMois   = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        // Mois précédent
        LocalDateTime debutPrecedent = debutMois.minusMonths(1);
        LocalDateTime finPrecedent   = debutMois.minusSeconds(1);

        model.addAttribute("ca",              journalService.calculerCA(debutMois, finMois));
        model.addAttribute("caPrecedent",     journalService.calculerCA(debutPrecedent, finPrecedent));
        model.addAttribute("benefice",        journalService.calculerBenefice(debutMois, finMois));
        model.addAttribute("entrees",         journalService.calculerTotalEntrees(debutMois, finMois));
        model.addAttribute("sorties",         journalService.calculerTotalSorties(debutMois, finMois));
        model.addAttribute("solde",           tresorerieService.calculerSolde());
        model.addAttribute("evolutionCA",     journalService.evolutionMensuelleCA());

        return "stitch/module_finance/kpi";
    }
}
