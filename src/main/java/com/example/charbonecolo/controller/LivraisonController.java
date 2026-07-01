package com.example.charbonecolo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.charbonecolo.model.LivraisonModel;
import com.example.charbonecolo.repository.LivreurRepository;
import com.example.charbonecolo.service.LivraisonService;

@Controller
@RequestMapping("/livraisons")
public class LivraisonController {

    private final LivraisonService livraisonService;
    private final LivreurRepository livreurRepository;

    public LivraisonController(LivraisonService livraisonService, LivreurRepository livreurRepository) {
        this.livraisonService = livraisonService;
        this.livreurRepository = livreurRepository;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("livraisons", livraisonService.findAll());
        return "stitch/module_commercial/liste-livraisons";
    }

    @GetMapping("/new")
    public String formulaireAjout(Model model) {
        model.addAttribute("livraison", new LivraisonModel());
        model.addAttribute("commandesDisponibles", livraisonService.findAvailableCommandes());
        model.addAttribute("livreurs", livreurRepository.findAll());
        return "stitch/module_commercial/nouvelle_livraison";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute LivraisonModel livraison,
                       @RequestParam(name = "commandeIds", required = false) List<Integer> commandeIds) {
        if (commandeIds == null || commandeIds.isEmpty()) {
            return "redirect:/livraisons/new?error=noCommandes";
        }
        livraisonService.createLivraison(livraison, commandeIds);
        return "redirect:/livraisons";
    }

    @GetMapping("/edit/{id}")
    public String formulaireModification(@PathVariable Integer id, Model model) {
        LivraisonModel livraison = livraisonService.findById(id)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable : " + id));
        model.addAttribute("livraison", livraison);
        model.addAttribute("commandesLiees", livraisonService.findCommandesByLivraisonId(id));
        model.addAttribute("statuts", livraisonService.findStatutsByLivraisonId(id));
        model.addAttribute("livreurs", livreurRepository.findAll());
        return "stitch/module_commercial/form-modification-livraison";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute LivraisonModel livraison) {
        livraison.setId(id);
        livraisonService.updateLivraison(livraison);
        return "redirect:/livraisons";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        livraisonService.deleteById(id);
        return "redirect:/livraisons";
    }
}
