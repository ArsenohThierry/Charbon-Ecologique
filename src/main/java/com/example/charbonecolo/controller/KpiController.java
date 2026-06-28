package com.example.charbonecolo.controller;

import com.example.charbonecolo.service.JournalFinancierService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/finance")
public class KpiController {

    private final JournalFinancierService financeService;

    public KpiController(JournalFinancierService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/kpi")
    public String afficher(Model model) {
        LocalDateTime debut = LocalDateTime.now().withDayOfYear(1);
        LocalDateTime fin   = LocalDateTime.now();

        model.addAttribute("ca",         financeService.calculerCA(debut, fin));
        model.addAttribute("benefice",   financeService.calculerBenefice(debut, fin));
        model.addAttribute("solde",      financeService.calculerSolde());
        model.addAttribute("evolutionCA",financeService.evolutionCA());
        return "stitch/module_finance/kpi";
    }
}