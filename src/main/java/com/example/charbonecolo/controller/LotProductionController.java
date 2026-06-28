package com.example.charbonecolo.controller;

import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.service.LotProductionService;
import com.example.charbonecolo.service.ProduitService;
import com.example.charbonecolo.service.TypeMatierePremiereService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("stock/lot")
public class LotProductionController {

    private final LotProductionService lotProductionService;
    private final ProduitService produitService;
    private final TypeMatierePremiereService typeMatierePremiereService;

    public LotProductionController(LotProductionService lotProductionService,
                                   ProduitService produitService,
                                   TypeMatierePremiereService typeMatierePremiereService) {
        this.lotProductionService = lotProductionService;
        this.produitService = produitService;
        this.typeMatierePremiereService = typeMatierePremiereService;
    }
    
    @GetMapping("/liste")
public String listLots(Model model) {
    model.addAttribute("lots", lotProductionService.getAllLotProductions());
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
            @RequestParam Integer quantiteProduitPrevues,
            @RequestParam(required = false) String dateEntreeLot,
            @RequestParam(required = false) String remarques,
            Model model) {      
        LotProductionModel lot = new LotProductionModel();      
        // Résolution manuelle des objets depuis les ids
        lot.setTypeMatierePremiere(
            typeMatierePremiereService.getById(idTypeMatierePremiere)
        );
        lot.setProduit(
            produitService.findById(idProduit)
        );
        lot.setQuantiteMatiereUtilisee(quantiteMatiereUtilisee);
        lot.setQuantiteProduitPrevues(quantiteProduitPrevues);
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
        if (lot.isEmpty()) return "redirect:/stock/lot/liste";
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
    lot.setQuantiteProduitPrevues(quantiteProduitPrevues);
    lot.setRemarques(remarques);
    if (dateEntreeLot != null && !dateEntreeLot.isBlank()) {
        lot.setDateEntreeLot(LocalDateTime.parse(dateEntreeLot));
    }
    lotProductionService.saveLotProduction(lot);
    return "redirect:/stock/lot/liste";
}

@PostMapping("/supprimer/{id}")
public String supprimerLot(@PathVariable Integer id) {
    lotProductionService.deleteLotProduction(id);
    return "redirect:/stock/lot/liste";
}
}