package com.example.charbonecolo.controller;

import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.service.LotProductionService;
import com.example.charbonecolo.service.ProduitService;
import com.example.charbonecolo.service.TypeMatierePremiereService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/lots")
public class LotProductionController {

    @Autowired
    private LotProductionService lotProductionService;

    @Autowired
    private TypeMatierePremiereService typeMatierePremiereService;

    @Autowired
    private ProduitService produitService;

    // ── LIST ──────────────────────────────────────────────────────────────────
    @GetMapping
    public String list(Model model) {
        model.addAttribute("lots", lotProductionService.getAll());
        return "lot/list";
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    @GetMapping("/nouveau")
    public String showCreateForm(Model model) {
        model.addAttribute("lot", new LotProductionModel());
        model.addAttribute("matieres", typeMatierePremiereService.getAll());
        model.addAttribute("produits", produitService.findAll());
        model.addAttribute("titre", "Nouveau lot de production");
        return "lot/form";
    }

    @PostMapping("/nouveau")
    public String create(@ModelAttribute LotProductionModel lot,
                         @RequestParam Integer idTypeMatiere,
                         @RequestParam Integer idProduit,
                         RedirectAttributes redirectAttributes) {
        lotProductionService.save(lot, idTypeMatiere, idProduit);
        redirectAttributes.addFlashAttribute("succes", "Lot créé avec succès.");
        return "redirect:/lots";
    }

    // ── EDIT ──────────────────────────────────────────────────────────────────
    @GetMapping("/{id}/modifier")
    public String showEditForm(@PathVariable Integer id, Model model) {
        LotProductionModel lot = lotProductionService.getById(id);
        model.addAttribute("lot", lot);
        model.addAttribute("matieres", typeMatierePremiereService.getAll());
        model.addAttribute("produits", produitService.findAll());
        model.addAttribute("titre", "Modifier le lot " + lot.getReference());
        return "lot/form";
    }

    @PostMapping("/{id}/modifier")
    public String update(@PathVariable Integer id,
                         @ModelAttribute LotProductionModel lot,
                         @RequestParam Integer idTypeMatiere,
                         @RequestParam Integer idProduit,
                         RedirectAttributes redirectAttributes) {
        lot.setId(id);
        lotProductionService.save(lot, idTypeMatiere, idProduit);
        redirectAttributes.addFlashAttribute("succes", "Lot modifié avec succès.");
        return "redirect:/lots";
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @PostMapping("/{id}/supprimer")
    public String delete(@PathVariable Integer id,
                         RedirectAttributes redirectAttributes) {
        lotProductionService.deleteById(id);
        redirectAttributes.addFlashAttribute("succes", "Lot supprimé.");
        return "redirect:/lots";
    }
}
