package com.example.charbonecolo.controller;

import com.example.charbonecolo.service.JournalFinancierService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.DateTimeException;
import java.util.Map;
import com.example.charbonecolo.dto.KpiDto;

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

    //API JSON POUR FETCH LES DONNEES DU KPI
    @GetMapping("/data")
    @ResponseBody
    public ResponseEntity<?> getKpi(
            @RequestParam int mois,
            @RequestParam int annee
    ) {
        if (mois < 1 || mois > 12) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le mois doit etre compris entre 1 et 12."));
        }

        if (annee < 2000 || annee > 2100) {
            return ResponseEntity.badRequest().body(Map.of("error", "L'annee selectionnee est invalide."));
        }

        LocalDateTime debut;
        try {
            debut = LocalDateTime.of(annee, mois, 1, 0, 0);
        } catch (DateTimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "La periode demandee est invalide."));
        }

        LocalDateTime fin = debut.with(TemporalAdjusters.lastDayOfMonth())
                .withHour(23).withMinute(59).withSecond(59);

        LocalDateTime debutPrecedent = debut.minusMonths(1);
        LocalDateTime finPrecedent = debut.minusSeconds(1);

        /*Map<String, Object> res = new HashMap<>();

        res.put("ca", journalService.calculerCA(debut, fin));
        res.put("caPrecedent", journalService.calculerCA(debutPrecedent, finPrecedent));
        res.put("benefice", journalService.calculerBenefice(debut, fin));
        res.put("entrees", journalService.calculerTotalEntrees(debut, fin));
        res.put("sorties", journalService.calculerTotalSorties(debut, fin));
        res.put("solde", journalService.calculerSolde());
        res.put("evolutionCA", journalService.evolutionMensuelleCA());

        return res;*/

        KpiDto dto = new KpiDto();

        dto.setCa(journalService.calculerCA(debut, fin));
        dto.setCaPrecedent(journalService.calculerCA(debutPrecedent, finPrecedent));
        dto.setBenefice(journalService.calculerBenefice(debut, fin));
        dto.setEntrees(journalService.calculerTotalEntrees(debut, fin));
        dto.setSorties(journalService.calculerTotalSorties(debut, fin));
        dto.setSolde(journalService.calculerSolde());
        dto.setEvolutionCA(journalService.evolutionMensuelleCA());

        return ResponseEntity.ok(dto);
    }
}
