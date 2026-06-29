package com.example.charbonecolo.controller;

import com.example.charbonecolo.model.LivraisonModel;
import com.example.charbonecolo.service.LivraisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/livraisons")
public class LivraisonController {

    @Autowired
    private LivraisonService livraisonService;

    // Liste de toutes les livraisons
    @GetMapping
    public String liste(Model model) {
        model.addAttribute("livraisons", livraisonService.findAll());
        return "stitch/module_commercial/liste-livraisons";
    }

    // Formulaire ajout
    @GetMapping("/new")
    public String formulaireAjout(Model model) {
        model.addAttribute("livraison", new LivraisonModel());
        return "stitch/module_commercial/form-ajout-livraison";
    }

    // Sauvegarde ajout
    @PostMapping("/save")
    public String save(@ModelAttribute LivraisonModel livraison) {
        livraison.setDateCreation(LocalDateTime.now());
        livraison.setActif(true);
        livraisonService.save(livraison);
        return "redirect:/livraisons";
    }

    // Formulaire modification
    @GetMapping("/edit/{id}")
    public String formulaireModification(@PathVariable Long id, Model model) {
        LivraisonModel livraison = livraisonService.findById(id)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable : " + id));
        model.addAttribute("livraison", livraison);
        return "stitch/module_commercial/form-modification-livraison";
    }

    // Sauvegarde modification
    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute LivraisonModel livraison) {
        livraison.setId(id);
        livraisonService.save(livraison);
        return "redirect:/livraisons";
    }

    // Suppression
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        livraisonService.deleteById(id);
        return "redirect:/livraisons";
    }
}