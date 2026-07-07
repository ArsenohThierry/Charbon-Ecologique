package com.example.charbonecolo.controller;

import com.example.charbonecolo.service.TresorerieService;
import com.example.charbonecolo.service.TresorerieService.MouvementTresorerie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/finance/tresorerie")
public class TresorerieController {

    private final TresorerieService tresorerieService;

    public TresorerieController(TresorerieService tresorerieService) {
        this.tresorerieService = tresorerieService;
    }

    @GetMapping
    public String afficherTresorerie(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Model model) {

        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, limit);
        Page<MouvementTresorerie> mouvements = tresorerieService.rechercher(date, reference, keyword, pageable);

        model.addAttribute("mouvements", mouvements.getContent());
        model.addAttribute("page", mouvements);
        model.addAttribute("currentPage", page);
        model.addAttribute("limit", limit);
        model.addAttribute("date", date);
        model.addAttribute("reference", reference);
        model.addAttribute("keyword", keyword);
        model.addAttribute("solde", tresorerieService.calculerSolde());
        model.addAttribute("totalEntrees", tresorerieService.calculerTotalEntrees());
        model.addAttribute("totalSorties", tresorerieService.calculerTotalSorties());
        return "stitch/module_finance/tresorerie";
    }
}
