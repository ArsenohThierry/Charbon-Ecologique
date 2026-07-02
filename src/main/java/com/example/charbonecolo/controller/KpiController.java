package com.example.charbonecolo.controller;

import com.example.charbonecolo.service.JournalFinancierService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Controller
@RequestMapping("/finance/kpi")
public class KpiController {

    private final JournalFinancierService journalService;

    public KpiController(JournalFinancierService journalService) {
        this.journalService = journalService;
    }

    @GetMapping
    public String afficherKpi(Model model) {
        LocalDateTime debutMois = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finMois = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        LocalDateTime debutPrecedent = debutMois.minusMonths(1);
        LocalDateTime finPrecedent = debutMois.minusSeconds(1);

        model.addAttribute("ca", journalService.calculerCA(debutMois, finMois));
        model.addAttribute("caPrecedent", journalService.calculerCA(debutPrecedent, finPrecedent));
        model.addAttribute("benefice", journalService.calculerBenefice(debutMois, finMois));
        model.addAttribute("entrees", journalService.calculerTotalEntrees(debutMois, finMois));
        model.addAttribute("sorties", journalService.calculerTotalSorties(debutMois, finMois));
        model.addAttribute("solde", journalService.calculerSolde());
        model.addAttribute("evolutionCA", journalService.evolutionMensuelleCA());

        return "stitch/module_finance/kpi";
    }
}
