package com.example.charbonecolo.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.service.LotProductionService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("stock/lot")
public class LotProductionController {

    private final LotProductionService lotProductionService;

    public LotProductionController(LotProductionService lotProductionService) {
        this.lotProductionService = lotProductionService;
    }

    @GetMapping("/nouveau")
    public String nouveauLot(Model model) {
        model.addAttribute("lotProduction", new LotProductionModel());
        return "stitch/module_stock/nouveau_lot";
    }

    @GetMapping("/liste")
    public String listLots(Model model) {
        List<LotProductionModel> lots = lotProductionService.getAllLotProductions();
        model.addAttribute("lots", lots);
        return "stitch/module_stock/liste_lot";
    }

    @PostMapping("/nouveau")
    public String creerLot(@Valid @ModelAttribute("lotProduction") LotProductionModel lotProduction,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "stitch/module_stock/nouveau_lot";
        }

        // Set default values if not provided
        if (lotProduction.getDateEntreeLot() == null) {
            lotProduction.setDateEntreeLot(LocalDateTime.now());
        }

        lotProductionService.saveLotProduction(lotProduction);
        return "redirect:/stock/lot/liste";
    }

    @GetMapping("/modifier/{id}")
    public String modifierLot(@PathVariable Integer id, Model model) {
        Optional<LotProductionModel> lot = lotProductionService.getLotProductionById(id);
        if (lot.isEmpty()) {
            return "redirect:/stock/lot/liste";
        }
        model.addAttribute("lotProduction", lot.get());
        return "stitch/module_stock/nouveau_lot"; // réutilise le même formulaire
    }

    @PostMapping("/modifier/{id}")
    public String updateLot(@PathVariable Integer id,
            @Valid @ModelAttribute("lotProduction") LotProductionModel lotProduction,
            BindingResult result) {
        if (result.hasErrors()) {
            return "stitch/module_stock/nouveau_lot";
        }
        lotProduction.setId(id); // forcer le bon ID
        lotProductionService.updateLotProduction(lotProduction);
        return "redirect:/stock/lot/liste";
    }

    @PostMapping("/supprimer/{id}")
    public String supprimerLot(@PathVariable Integer id) {
        lotProductionService.deleteLotProduction(id);
        return "redirect:/stock/lot/liste";
    }
}
