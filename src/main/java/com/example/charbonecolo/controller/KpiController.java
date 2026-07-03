package com.example.charbonecolo.controller;

import com.example.charbonecolo.service.JournalFinancierService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

@Controller
@RequestMapping("/finance/kpi")
public class KpiController {

    private final JournalFinancierService journalService;

    public KpiController(JournalFinancierService journalService) {
        this.journalService = journalService;
    }

    @GetMapping
    public String afficherKpi(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateDebut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFin,
            Model model) {

        // Par défaut : mois courant
        if (dateDebut == null || dateFin == null) {
            YearMonth mois = YearMonth.now();
            dateDebut = mois.atDay(1);
            dateFin = mois.atEndOfMonth();
        }

        // Sécuriser une plage inversée venant des paramètres
        if (dateDebut.isAfter(dateFin)) {
            LocalDate tmp = dateDebut;
            dateDebut = dateFin;
            dateFin = tmp;
        }

        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(23, 59, 59);

        // Période précédente de même durée (en jours calendaires réels)
        long nbJours = ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;

        LocalDate debutPrec = dateDebut.minusDays(nbJours);
        LocalDate finPrec = dateDebut.minusDays(1);

        // Graphe KPI : 12 derniers mois (cohérent avec le titre de la vue)
        YearMonth moisFinGraph = YearMonth.from(dateFin);
        LocalDateTime debutGraph = moisFinGraph.minusMonths(11).atDay(1).atStartOfDay();
        LocalDateTime finGraph = moisFinGraph.atEndOfMonth().atTime(23, 59, 59);

        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        model.addAttribute("ca",journalService.calculerCA(debut, fin));
        model.addAttribute("caPrecedent",journalService.calculerCA(debutPrec.atStartOfDay(),finPrec.atTime(23,59,59)));
        model.addAttribute("benefice",journalService.calculerBenefice(debut, fin));
        model.addAttribute("entrees",journalService.calculerTotalEntrees(debut, fin));
        model.addAttribute("sorties",journalService.calculerTotalSorties(debut, fin));
        model.addAttribute("solde",journalService.calculerSolde());
        model.addAttribute("evolutionCA",journalService.evolutionMensuelleCA(debutGraph, finGraph));

        return "stitch/module_finance/kpi";
    }
}