package com.example.charbonecolo.controller;

import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.service.LotProductionService;
import com.example.charbonecolo.service.LotStatutsService;
import com.example.charbonecolo.service.ProduitService;
import com.example.charbonecolo.service.TypeMatierePremiereService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("stock/lot")
public class LotProductionController {

    private final LotProductionService lotProductionService;
    private final ProduitService produitService;
    private final TypeMatierePremiereService typeMatierePremiereService;
    private final LotStatutsService lotsStatutsService;

    public LotProductionController(LotProductionService lotProductionService,
            ProduitService produitService,
            TypeMatierePremiereService typeMatierePremiereService,
            LotStatutsService lotStatutsService) {
        this.lotProductionService = lotProductionService;
        this.produitService = produitService;
        this.typeMatierePremiereService = typeMatierePremiereService;
        this.lotsStatutsService = lotStatutsService;
    }

    @GetMapping("/liste")
    public String listLots(Model model) {
        model.addAttribute("lots", lotProductionService.getAllLotProductions());
        model.addAttribute("statusMap", lotProductionService.getLatestStatutsForAllLots());
        model.addAttribute("lotStatuts", lotsStatutsService.getAllLotsStatuts());
        return "stitch/module_stock/liste_lot";
    }

    @GetMapping("/nouveau")
    public String nouveauLot(Model model) {
        model.addAttribute("lotProduction", new LotProductionModel());
        model.addAttribute("produits", produitService.findAll());
        model.addAttribute("matieres", typeMatierePremiereService.getAll());
        return "stitch/module_stock/nouveau_lot";
    }

    @PostMapping("/nouveau")
    public String creerLot(
            @RequestParam Integer idTypeMatierePremiere,
            @RequestParam Integer idProduit,
            @RequestParam BigDecimal quantiteMatiereUtilisee,
            @RequestParam Integer quantiteProduitPrevue,
            @RequestParam(required = false) String dateEntreeLot,
            @RequestParam(required = false) String remarques,
            Model model) {
        LotProductionModel lot = new LotProductionModel();
        // Résolution manuelle des objets depuis les ids
        lot.setTypeMatierePremiere(
                typeMatierePremiereService.getById(idTypeMatierePremiere));
        lot.setProduit(
                produitService.findById(idProduit));
        lot.setQuantiteMatiereUtilisee(quantiteMatiereUtilisee);
        lot.setQuantiteProduitPrevue(quantiteProduitPrevue);
        lot.setRemarques(remarques);        
        if (dateEntreeLot != null && !dateEntreeLot.isBlank()) {
            lot.setDateEntreeLot(LocalDateTime.parse(dateEntreeLot));
        } else {
            lot.setDateEntreeLot(LocalDateTime.now());
        }
        lotProductionService.saveLotProduction(lot);
        return "redirect:/stock/lot/liste";
    }

    @GetMapping("/modifier/{id}")
    public String modifierLot(@PathVariable Integer id, Model model) {
        Optional<LotProductionModel> lot = lotProductionService.getLotProductionById(id);
        if (lot.isEmpty())
            return "redirect:/stock/lot/liste";
        model.addAttribute("lotProduction", lot.get());
        model.addAttribute("produits", produitService.findAll());
        model.addAttribute("matieres", typeMatierePremiereService.getAll());
        return "stitch/module_stock/nouveau_lot";
    }

    @PostMapping("/modifier/{id}")
public String updateLot(@PathVariable Integer id,
                        @RequestParam Integer idTypeMatierePremiere,
                        @RequestParam Integer idProduit,
                        @RequestParam BigDecimal quantiteMatiereUtilisee,
                        @RequestParam Integer quantiteProduitPrevues,
                        @RequestParam(required = false) String dateEntreeLot,
                        @RequestParam(required = false) String remarques) {
    LotProductionModel lot = lotProductionService.getLotProductionById(id).orElseThrow();
    lot.setTypeMatierePremiere(typeMatierePremiereService.getById(idTypeMatierePremiere));
    lot.setProduit(produitService.findById(idProduit));
    lot.setQuantiteMatiereUtilisee(quantiteMatiereUtilisee);
    lot.setQuantiteProduitPrevue(quantiteProduitPrevues);
    lot.setRemarques(remarques);
    if (dateEntreeLot != null && !dateEntreeLot.isBlank()) {
        lot.setDateEntreeLot(LocalDateTime.parse(dateEntreeLot));
    }


    @PostMapping("/supprimer/{id}")
    public String supprimerLot(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            lotProductionService.deleteLotProduction(id);
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("error",
                    "Impossible de supprimer ce lot : il est référencé par des statuts ou mouvements.");
        }
        return "redirect:/stock/lot/liste";
    }
}
