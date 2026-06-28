package com.example.charbonecolo.controller;

import com.example.charbonecolo.service.JournalFinancierService;
import com.example.charbonecolo.service.TresorerieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/finance")
public class TresorerieController {

    private final JournalFinancierService financeService;
    private final TresorerieService tresorerieService;

    public TresorerieController(JournalFinancierService financeService, TresorerieService tresorerieService) {
        this.financeService = financeService;
        this.tresorerieService = tresorerieService;
    }

    @GetMapping("/tresorerie")
    public String afficher(Model model) {
        model.addAttribute("solde", financeService.calculerSolde());
        model.addAttribute("mouvements", tresorerieService.findAll());
        return "stitch/module_finance/tresorerie";
    }
}