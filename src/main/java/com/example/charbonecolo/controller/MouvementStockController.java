package com.example.charbonecolo.controller;

import com.example.charbonecolo.exception.BusinessException;
import com.example.charbonecolo.model.MouvementSortieDetailModel;
import com.example.charbonecolo.model.MouvementStockModel;
import com.example.charbonecolo.model.ProduitModel;
import com.example.charbonecolo.service.MouvementStockService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Stock Movement module
 */
@Controller
public class MouvementStockController {

    private final MouvementStockService mouvementStockService;

    public MouvementStockController(MouvementStockService mouvementStockService) {
        this.mouvementStockService = mouvementStockService;
    }

    // ── ENTRÉE ───────────────────────────────────────────────────

    /**
     * Affiche la page d'entrée stock avec les lots et l'historique
     */
    @GetMapping("stock/entree")
    public String entreeStock(Model model) {
        model.addAttribute("lots", mouvementStockService.getLotsTermines());
        model.addAttribute("mouvements", mouvementStockService.getAllMouvementsStock());
        return "stitch/module_stock/entree_stock";
    }

    /**
     * Enregistre une entrée stock et redirige
     */
    @PostMapping("stock/entree")
    public String saveEntreeStock(@RequestParam Integer idLot,
                                  @RequestParam Integer quantite,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        mouvementStockService.saveEntreeStock(idLot, quantite, date);
        return "redirect:/stock/entree";
    }

    // ── SORTIE (FIFO) ───────────────────────────────────────────

    /**
     * Affiche la page de sortie stock avec les produits, motifs et l'historique
     */
    @GetMapping("stock/sortie")
    public String sortieStock(Model model) {
        List<ProduitModel> produits = mouvementStockService.getAllProduits();
        model.addAttribute("produits", produits);

        Map<Integer, String> produitsMap = new HashMap<>();
        for (ProduitModel p : produits) {
            produitsMap.put(p.getId(), p.getNom());
        }
        model.addAttribute("produitsMap", produitsMap);

        model.addAttribute("motifs", mouvementStockService.getAllMotifsSortie());
        List<MouvementStockModel> mouvements = mouvementStockService.getAllMouvementsStock();
        model.addAttribute("mouvements", mouvements);

        Map<Integer, List<MouvementSortieDetailModel>> sortieDetailsMap = new HashMap<>();
        for (MouvementStockModel m : mouvements) {
            if (m.getTypeMouvement().getId() == 2) {
                sortieDetailsMap.put(m.getId(), mouvementStockService.getDetailsByMouvement(m));
            }
        }
        model.addAttribute("sortieDetails", sortieDetailsMap);
        return "stitch/module_stock/sortie_stock";
    }

    /**
     * Enregistre une sortie stock avec déduction FIFO et redirige
     */
    @PostMapping("stock/sortie")
    public String saveSortieStock(@RequestParam Integer idProduit,
                                  @RequestParam Integer quantite,
                                  @RequestParam Integer idMotif,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                  RedirectAttributes ra) {
        try {
            mouvementStockService.saveSortieStock(idProduit, quantite, idMotif, date);
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/stock/sortie";
        }
        return "redirect:/stock/sortie";
    }

    // ── MODIFIER ─────────────────────────────────────────────────

    /**
     * Affiche le formulaire de modification pré-rempli
     */
    @GetMapping("stock/mouvement/modifier")
    public String editMouvement(@RequestParam Integer id, Model model) {
        MouvementStockModel m = mouvementStockService.getMouvementStockById(id).orElseThrow();
        model.addAttribute("mouvement", m);
        model.addAttribute("lots", mouvementStockService.getLotsTermines());
        model.addAttribute("motifs", mouvementStockService.getAllMotifsSortie());
        return "stitch/module_stock/edit_mouvement";
    }

    /**
     * Sauvegarde les modifications
     */
    @PostMapping("stock/mouvement/modifier")
    public String saveEditMouvement(@RequestParam Integer id,
                                    @RequestParam(required = false) Integer idLot,
                                    @RequestParam Integer quantite,
                                    @RequestParam(required = false) Integer idMotif,
                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                    RedirectAttributes ra) {
        MouvementStockModel m = mouvementStockService.getMouvementStockById(id).orElseThrow();
        boolean isEntree = m.getTypeMouvement().getId() == 1;
        if (isEntree) {
            mouvementStockService.updateEntreeStock(id, idLot, quantite, date);
            return "redirect:/stock/entree";
        } else {
            try {
                mouvementStockService.updateSortieStock(id, quantite, idMotif, date);
            } catch (BusinessException e) {
                ra.addFlashAttribute("error", e.getMessage());
                return "redirect:/stock/mouvement/modifier?id=" + id;
            }
            return "redirect:/stock/sortie";
        }
    }

    // ── SUPPRIMER ────────────────────────────────────────────────

    @PostMapping("stock/mouvement/supprimer")
    public String deleteMouvement(@RequestParam Integer id) {
        MouvementStockModel m = mouvementStockService.getMouvementStockById(id).orElseThrow();
        boolean isEntree = m.getTypeMouvement().getId() == 1;
        mouvementStockService.deleteMouvementStock(id);
        return isEntree ? "redirect:/stock/entree" : "redirect:/stock/sortie";
    }
}