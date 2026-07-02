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

    @GetMapping
    public String afficherTresorerie(Model model) {
        model.addAttribute("mouvements", tresorerieService.findAll());
        model.addAttribute("solde", tresorerieService.calculerSolde());
        model.addAttribute("totalEntrees", tresorerieService.calculerTotalEntrees());
        model.addAttribute("totalSorties", tresorerieService.calculerTotalSorties());
        return "stitch/module_finance/tresorerie";
    }
}
