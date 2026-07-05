package com.example.charbonecolo.controller;

import com.example.charbonecolo.exception.BusinessException;
import com.example.charbonecolo.model.MouvementSortieDetailModel;
import com.example.charbonecolo.model.MouvementStockModel;
import com.example.charbonecolo.dto.AlerteProduitDTO;
import com.example.charbonecolo.dto.EntreeCriteriaWrapper;
import com.example.charbonecolo.dto.EntreeDto;
import com.example.charbonecolo.dto.EntreeStockDTO;
import com.example.charbonecolo.dto.EtatStockCriteriaWrapper;
import com.example.charbonecolo.dto.EtatStockDto;
import com.example.charbonecolo.dto.LotStockSummaryDTO;
import com.example.charbonecolo.dto.SortieCriteriaWrapper;
import com.example.charbonecolo.dto.SortieDto;
import com.example.charbonecolo.dto.SortieStockDTO;
import com.example.charbonecolo.model.ProduitModel;
import com.example.charbonecolo.service.MouvementStockService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.*;

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

    // ENTRÉE
    private static final Map<String, String> sortReferencesEntrees;
    static {
        sortReferencesEntrees = new HashMap<>();
        sortReferencesEntrees.put("date", "date_mouvement");
        sortReferencesEntrees.put("lot", "lot_reference");
        sortReferencesEntrees.put("matiere", "matiere_libelle");
        sortReferencesEntrees.put("quantite", "quantite");
        sortReferencesEntrees.put("fournisseur", "fournisseur_nom");
    }

    @GetMapping("stock/entree")
    public ModelAndView entreeStock(@ModelAttribute EntreeCriteriaWrapper wrapper) {
        if (wrapper.getLimit() == null) {
            wrapper.setLimit(10);
        }

        Pageable pageable;
        if (wrapper.getCurrentSort() != null && wrapper.getCurrentDir() != null
                && !wrapper.getCurrentSort().isEmpty() && !wrapper.getCurrentDir().isEmpty()) {
            String sort = sortReferencesEntrees.get(wrapper.getCurrentSort());
            Sort.Direction direction = wrapper.getCurrentDir().equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            pageable = PageRequest.of(wrapper.getPage() - 1, wrapper.getLimit(), Sort.by(direction, sort));
        } else {
            pageable = PageRequest.of(wrapper.getPage() - 1, wrapper.getLimit());
        }

        Slice<EntreeDto> pageResult = mouvementStockService.listEntrees(pageable, wrapper);

        ModelAndView mav = new ModelAndView("stitch/module_stock/entree_stock");
        mav.addObject("entrees", pageResult.getContent());
        mav.addObject("currentPage", wrapper.getPage());
        mav.addObject("currentDir", wrapper.getCurrentDir());
        mav.addObject("currentSort", wrapper.getCurrentSort());
        mav.addObject("page", pageResult);
        mav.addObject("lotsTermines", mouvementStockService.getLotsTermines());
        mav.addObject("produits", mouvementStockService.getAllProduits());
        mav.addObject("dispo", mouvementStockService.getStockDisponible());
        return mav;
    }

    /**
     * Enregistre une entrée stock et redirige
     */
    @PostMapping("stock/entree")
    public String saveEntreeStock(@ModelAttribute EntreeStockDTO entry, RedirectAttributes ra) {
        try {
            mouvementStockService.saveEntreeStock(entry);

        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/stock/entree";
        }
        return "redirect:/stock/entree";
    }

    // SORTIE (FIFO)

    private static final Map<String, String> sortReferencesSorties;
    static {
        sortReferencesSorties = new HashMap<>();
        sortReferencesSorties.put("reference", "id");
        sortReferencesSorties.put("produit", "produit_nom");
        sortReferencesSorties.put("quantite", "quantite");
        sortReferencesSorties.put("motif", "motif_libelle");
        sortReferencesSorties.put("date", "date_mouvement");
    }

    @GetMapping("stock/sortie")
    public ModelAndView sortieStock(@ModelAttribute SortieCriteriaWrapper wrapper) {
        if (wrapper.getLimit() == null) {
            wrapper.setLimit(10);
        }

        Pageable pageable;
        if (wrapper.getCurrentSort() != null && wrapper.getCurrentDir() != null
                && !wrapper.getCurrentSort().isEmpty() && !wrapper.getCurrentDir().isEmpty()) {
            String sort = sortReferencesSorties.get(wrapper.getCurrentSort());
            Sort.Direction direction = wrapper.getCurrentDir().equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            pageable = PageRequest.of(wrapper.getPage() - 1, wrapper.getLimit(), Sort.by(direction, sort));
        } else {
            pageable = PageRequest.of(wrapper.getPage() - 1, wrapper.getLimit());
        }

        Slice<SortieDto> pageResult = mouvementStockService.listSorties(pageable, wrapper);

        ModelAndView mav = new ModelAndView("stitch/module_stock/sortie_stock");
        mav.addObject("sorties", pageResult.getContent());
        mav.addObject("currentPage", wrapper.getPage());
        mav.addObject("currentDir", wrapper.getCurrentDir());
        mav.addObject("currentSort", wrapper.getCurrentSort());
        mav.addObject("page", pageResult);
        mav.addObject("produits", mouvementStockService.getAllProduits());
        mav.addObject("motifs", mouvementStockService.getAllMotifsSortie());
        return mav;
    }

    /**
     * Enregistre une sortie stock avec déduction FIFO et redirige
     */
    @PostMapping("stock/sortie")
    public String saveSortieStock(@ModelAttribute SortieStockDTO sortie, RedirectAttributes ra) {
        try {
            mouvementStockService.saveSortieStock(sortie);
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
    public String editMouvement(@RequestParam Integer id, Model model, RedirectAttributes ra) {
        MouvementStockModel m = mouvementStockService.getMouvementStockById(id).orElseThrow();
        model.addAttribute("mouvement", m);
        model.addAttribute("lotsTermines", mouvementStockService.getLotsTermines());
        model.addAttribute("motifs", mouvementStockService.getAllMotifsSortie());
        return "stitch/module_stock/edit_mouvement";
    }

    /**
     * Sauvegarde les modifications
     */
    @PostMapping("stock/mouvement/modifier")
    public String saveEditMouvement(
            @RequestParam Integer id,
            @RequestParam(required = false) Integer idLot,
            @RequestParam Integer quantite,
            @RequestParam(required = false) Integer idMotif,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            RedirectAttributes ra) {

        MouvementStockModel m = mouvementStockService
                .getMouvementStockById(id)
                .orElseThrow();
        boolean isEntree = m.getTypeMouvement().getId() == 1;

        try {

            if (isEntree) {
                EntreeStockDTO dto = new EntreeStockDTO();
                dto.setId(id);
                dto.setIdLot(idLot);
                dto.setQuantite(quantite);
                dto.setDateEntree(date);

                mouvementStockService.updateEntreeStock(dto);
            } else {
                SortieStockDTO dto = new SortieStockDTO();
                dto.setId(id);
                dto.setQuantite(quantite);
                dto.setIdMotif(idMotif);
                dto.setDateSortie(date);

                mouvementStockService.updateSortieStock(dto);
            }

        } catch (BusinessException e) {

            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/stock/mouvement/modifier?id=" + id;
        }

        return isEntree ? "redirect:/stock/entree" : "redirect:/stock/sortie";
    }

    // ── SUPPRIMER ────────────────────────────────────────────────

    @PostMapping("stock/mouvement/supprimer")
    public String deleteMouvement(@RequestParam Integer id, RedirectAttributes ra) {

        MouvementStockModel m = mouvementStockService
                .getMouvementStockById(id)
                .orElseThrow();

        boolean isEntree = m.getTypeMouvement().getId() == 1;

        try {

            mouvementStockService.deleteMouvementStock(id);

        } catch (BusinessException e) {

            ra.addFlashAttribute("error", e.getMessage());

        } catch (DataIntegrityViolationException e) {

            ra.addFlashAttribute("error",
                    "Impossible de supprimer ce mouvement : il est référencé par d'autres enregistrements.");

        }

        return isEntree ? "redirect:/stock/entree" : "redirect:/stock/sortie";
    }

    private static final Map<String, String> sortReferencesEtatStock;
    static {
        sortReferencesEtatStock = new HashMap<>();
        sortReferencesEtatStock.put("produit", "nom");
        sortReferencesEtatStock.put("reference", "reference");
        sortReferencesEtatStock.put("entree", "total_entree");
        sortReferencesEtatStock.put("sortie", "total_sortie");
        sortReferencesEtatStock.put("restant", "restant");
    }

    @GetMapping("stock/etat")
    public ModelAndView etatStock(@ModelAttribute EtatStockCriteriaWrapper wrapper) {
        if (wrapper.getLimit() == null) {
            wrapper.setLimit(10);
        }

        Pageable pageable;
        if (wrapper.getCurrentSort() != null && wrapper.getCurrentDir() != null
                && !wrapper.getCurrentSort().isEmpty() && !wrapper.getCurrentDir().isEmpty()) {
            String sort = sortReferencesEtatStock.get(wrapper.getCurrentSort());
            Sort.Direction direction = wrapper.getCurrentDir().equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            pageable = PageRequest.of(wrapper.getPage() - 1, wrapper.getLimit(), Sort.by(direction, sort));
        } else {
            pageable = PageRequest.of(wrapper.getPage() - 1, wrapper.getLimit());
        }

        Slice<EtatStockDto> pageResult = mouvementStockService.listEtatStock(pageable, wrapper);

        ModelAndView mav = new ModelAndView("stitch/module_stock/etat_stock");
        mav.addObject("stockParLot", pageResult.getContent());
        mav.addObject("currentPage", wrapper.getPage());
        mav.addObject("currentDir", wrapper.getCurrentDir());
        mav.addObject("currentSort", wrapper.getCurrentSort());
        mav.addObject("produits", mouvementStockService.getAllProduits());
        mav.addObject("page", pageResult);

        // Les alertes restent calculées séparément — logique différente (pas paginée)
        List<AlerteProduitDTO> alertes = mouvementStockService.getAlertesActives();
        mav.addObject("alertes", alertes);
        mav.addObject("nbAlertesTotal", alertes.size());
        int nbRuptures = 0;
        for (AlerteProduitDTO a : alertes) {
            if ("Rupture".equals(a.niveauAlerte()))
                nbRuptures++;
        }
        mav.addObject("nbRuptures", nbRuptures);

        return mav;
    }
}