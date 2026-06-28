package com.example.charbonecolo.controller;

import com.example.charbonecolo.service.TresorerieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/finance/tresorerie")
public class TresorerieController {

    private final TresorerieService tresorerieService;

    public TresorerieController(TresorerieService tresorerieService) {
        this.tresorerieService = tresorerieService;
    }

    /** GET /finance/tresorerie — Affiche les mouvements de trésorerie et le solde */
    @GetMapping
    public String afficherTresorerie(Model model) {
        model.addAttribute("mouvements", tresorerieService.findAll());
        model.addAttribute("solde",      tresorerieService.calculerSolde());
        model.addAttribute("entrees",    tresorerieService.findByType("ENTREE"));
        model.addAttribute("sorties",    tresorerieService.findByType("SORTIE"));
        return "stitch/module_finance/tresorerie";
    }
}
