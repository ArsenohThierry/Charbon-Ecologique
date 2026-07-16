package com.example.charbonecolo.controller;

import com.example.charbonecolo.model.ProduitModel;
import com.example.charbonecolo.service.ProduitService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/produits")
public class ProduitController {

    private final ProduitService produitService;

    public ProduitController(ProduitService produitService) {
        this.produitService = produitService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) Double puMin,
            @RequestParam(required = false) Double puMax,
            @RequestParam(required = false, defaultValue = "nom") String tri,
            @RequestParam(required = false, defaultValue = "asc") String direction,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int taille,
            Model model) {
        boolean rechercheActive = (nom != null && !nom.isBlank()) || puMin != null || puMax != null;

        List<ProduitModel> produits = rechercheActive
                ? produitService.rechercher(nom, puMin, puMax)
                : produitService.findAll();

        produitService.trier(produits, tri, direction);

        // Pagination (en mémoire, après filtrage + tri)
        int totalProduits = produits.size();
        int tailleEffective = Math.max(1, taille);
        int totalPages = Math.max(1, (int) Math.ceil(totalProduits / (double) tailleEffective));
        int pageEffective = Math.min(Math.max(0, page), totalPages - 1);
        int debutIndex = pageEffective * tailleEffective;
        int finIndex = Math.min(debutIndex + tailleEffective, totalProduits);
        List<ProduitModel> produitsPage = (debutIndex < finIndex)
                ? produits.subList(debutIndex, finIndex)
                : List.of();

        model.addAttribute("produits", produitsPage);

        // Critères de recherche (pour pré-remplir le formulaire)
        model.addAttribute("critNom", nom);
        model.addAttribute("critPuMin", puMin);
        model.addAttribute("critPuMax", puMax);
        
        // Tri + pagination
        model.addAttribute("critTri", tri);
        model.addAttribute("critDirection", direction);
        model.addAttribute("critTaille", tailleEffective);
        model.addAttribute("page", pageEffective);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalProduits", totalProduits);

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