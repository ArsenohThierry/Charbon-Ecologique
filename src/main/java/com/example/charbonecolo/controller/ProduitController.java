package com.example.charbonecolo.controller;

import com.example.charbonecolo.model.ProduitModel;
import com.example.charbonecolo.service.ProduitService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/produits")
public class ProduitController {

    private final ProduitService produitService;

    public ProduitController(ProduitService produitService) {
        this.produitService = produitService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("produits", produitService.findAll());
        return "produit/list";
    }

    @GetMapping("/nouveau")
    public String showCreateForm(Model model) {
        model.addAttribute("produit", new ProduitModel());
        model.addAttribute("titre", "Nouveau produit");
        return "produit/form";
    }

    @PostMapping("/nouveau")
    public String create(@ModelAttribute ProduitModel produit,
                         RedirectAttributes redirectAttributes) {
        produitService.save(produit);
        redirectAttributes.addFlashAttribute("succes", "Produit créé avec succès.");
        return "redirect:/produits";
    }

    @GetMapping("/{id}/modifier")
    public String showEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("produit", produitService.findById(id));
        model.addAttribute("titre", "Modifier le produit");
        return "produit/form";
    }

    @PostMapping("/{id}/modifier")
    public String update(@PathVariable Integer id,
                         @ModelAttribute ProduitModel produit,
                         RedirectAttributes redirectAttributes) {
        produit.setId(id); // assurer le bon id
        produitService.save(produit);
        redirectAttributes.addFlashAttribute("succes", "Produit modifié avec succès.");
        return "redirect:/produits";
    }

    @PostMapping("/{id}/supprimer")
    public String delete(@PathVariable Integer id,
                         RedirectAttributes redirectAttributes) {
        produitService.deleteById(id);
        redirectAttributes.addFlashAttribute("succes", "Produit supprimé.");
        return "redirect:/produits";
    }
}