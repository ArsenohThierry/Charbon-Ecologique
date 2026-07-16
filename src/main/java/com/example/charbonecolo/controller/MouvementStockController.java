package com.example.charbonecolo.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.charbonecolo.dto.AlerteProduitDTO;
import com.example.charbonecolo.dto.EntreeCriteriaWrapper;
import com.example.charbonecolo.dto.EntreeDto;
import com.example.charbonecolo.dto.EntreeStockDTO;
import com.example.charbonecolo.dto.EtatStockCriteriaWrapper;
import com.example.charbonecolo.dto.EtatStockDto;
import com.example.charbonecolo.dto.MouvementEditDTO;
import com.example.charbonecolo.dto.SortieCriteriaWrapper;
import com.example.charbonecolo.dto.SortieDto;
import com.example.charbonecolo.dto.SortieStockDTO;
import com.example.charbonecolo.exception.BusinessException;
import com.example.charbonecolo.exception.FieldBusinessException;
import com.example.charbonecolo.model.MouvementStockModel;
import com.example.charbonecolo.service.ExportSortieStockService;
import com.example.charbonecolo.service.MouvementStockService;

import jakarta.validation.Valid;

/**
 * Controller for Stock Movement module
 */
@Controller
public class MouvementStockController {

    private final MouvementStockService mouvementStockService;

    public MouvementStockController(MouvementStockService mouvementStockService) {
        this.mouvementStockService = mouvementStockService;
    }

    @Autowired
    private ExportSortieStockService exportService;
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

    private void remplirModeleEntree(Model model) {
        model.addAttribute("lotsTermines", mouvementStockService.getLotsTermines());
        model.addAttribute("mouvements", mouvementStockService.getAllMouvementsStock());
    }

    @GetMapping("stock/entree")
    public String entreeStock(@ModelAttribute EntreeCriteriaWrapper wrapper, Model model) {
        model.addAttribute("entry", new EntreeStockDTO());
        remplirModeleEntree(model, wrapper);
        return "stitch/module_stock/entree_stock";
    }

    @PostMapping("stock/entree")
    public String saveEntreeStock(@Valid @ModelAttribute("entry") EntreeStockDTO entry,
            BindingResult bindingResult,
            @ModelAttribute EntreeCriteriaWrapper wrapper,
            Model model) {
        if (!bindingResult.hasErrors()) {
            try {
                mouvementStockService.saveEntreeStock(entry);
                return "redirect:/stock/entree";
            } catch (FieldBusinessException e) {
                bindingResult.rejectValue(e.getChamp(), "error.metier", e.getMessage());
            } catch (BusinessException e) {
                model.addAttribute("error", e.getMessage());
            }
        }

        remplirModeleEntree(model, wrapper);
        return "stitch/module_stock/entree_stock";
    }

    private void remplirModeleEntree(Model model, EntreeCriteriaWrapper wrapper) {
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

        model.addAttribute("entrees", pageResult.getContent());
        model.addAttribute("currentPage", wrapper.getPage());
        model.addAttribute("currentDir", wrapper.getCurrentDir());
        model.addAttribute("currentSort", wrapper.getCurrentSort());
        model.addAttribute("page", pageResult);
        model.addAttribute("lotsTermines", mouvementStockService.getLotsTermines());
        model.addAttribute("produits", mouvementStockService.getAllProduits());
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
    public String sortieStock(@ModelAttribute SortieCriteriaWrapper wrapper, Model model) {
        model.addAttribute("sortie", new SortieStockDTO());
        remplirModeleSortie(model, wrapper);
        return "stitch/module_stock/sortie_stock";
    }

    /**
     * Enregistre une sortie stock avec déduction FIFO et redirige
     */
    @PostMapping("stock/sortie")
    public String saveSortieStock(@Valid @ModelAttribute("sortie") SortieStockDTO sortie,
            BindingResult bindingResult,
            @ModelAttribute SortieCriteriaWrapper wrapper,
            Model model) {
        if (!bindingResult.hasErrors()) {
            try {
                mouvementStockService.saveSortieStock(sortie);
                return "redirect:/stock/sortie";
            } catch (FieldBusinessException e) {
                bindingResult.rejectValue(e.getChamp(), "error.metier", e.getMessage());
            } catch (BusinessException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        remplirModeleSortie(model, wrapper);
        return "stitch/module_stock/sortie_stock";
    }

    private void remplirModeleSortie(Model model, SortieCriteriaWrapper wrapper) {
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
        model.addAttribute("sorties", pageResult.getContent());
        model.addAttribute("currentPage", wrapper.getPage());
        model.addAttribute("currentDir", wrapper.getCurrentDir());
        model.addAttribute("currentSort", wrapper.getCurrentSort());
        model.addAttribute("page", pageResult);
        model.addAttribute("produits", mouvementStockService.getAllProduits());
        model.addAttribute("motifs", mouvementStockService.getAllMotifsSortie());
    }

    // ── MODIFIER ─────────────────────────────────────────────────

    @GetMapping("stock/mouvement/modifier")
    public String editMouvement(@RequestParam Integer id, Model model) {
        MouvementStockModel m = mouvementStockService.getMouvementStockById(id).orElseThrow();

        if (!model.containsAttribute("mouvementEdit")) {
            MouvementEditDTO dto = new MouvementEditDTO();
            dto.setId(m.getId());
            dto.setQuantite(m.getQuantite());
            dto.setDate(m.getDateMouvement().toLocalDate());
            if (m.getLotProduction() != null) {
                dto.setIdLot(m.getLotProduction().getId());
            }
            if (m.getMotifSortie() != null) {
                dto.setIdMotif(m.getMotifSortie().getId());
            }
            model.addAttribute("mouvementEdit", dto);
        }

        model.addAttribute("mouvement", m);
        model.addAttribute("lotsTermines", mouvementStockService.getLotsTermines());
        model.addAttribute("motifs", mouvementStockService.getAllMotifsSortie());
        return "stitch/module_stock/edit_mouvement";
    }

    @PostMapping("stock/mouvement/modifier")
    public String saveEditMouvement(@Valid @ModelAttribute("mouvementEdit") MouvementEditDTO dto,
            BindingResult bindingResult,
            Model model) {
        MouvementStockModel m = mouvementStockService.getMouvementStockById(dto.getId()).orElseThrow();
        boolean isEntree = m.getTypeMouvement().getId() == 1;

        if (!bindingResult.hasErrors()) {
            try {
                if (isEntree) {
                    EntreeStockDTO entreeDto = new EntreeStockDTO();
                    entreeDto.setId(dto.getId());
                    entreeDto.setIdLot(dto.getIdLot());
                    entreeDto.setQuantite(dto.getQuantite());
                    entreeDto.setDateEntree(dto.getDate());
                    mouvementStockService.updateEntreeStock(entreeDto);
                    return "redirect:/stock/entree";
                } else {
                    SortieStockDTO sortieDto = new SortieStockDTO();
                    sortieDto.setId(dto.getId());
                    sortieDto.setQuantite(dto.getQuantite());
                    sortieDto.setIdMotif(dto.getIdMotif());
                    sortieDto.setDateSortie(dto.getDate());
                    mouvementStockService.updateSortieStock(sortieDto);
                    return "redirect:/stock/sortie";
                }
            } catch (FieldBusinessException e) {
                // Le service renvoie "quantite" ou "dateSortie" — on adapte au nom du champ du
                // formulaire unifié
                String champFormulaire = "dateSortie".equals(e.getChamp()) ? "date" : e.getChamp();
                bindingResult.rejectValue(champFormulaire, "error.metier", e.getMessage());
            }catch (BusinessException e) {
                model.addAttribute("error", e.getMessage());
            }
        }

        model.addAttribute("mouvement", m);
        model.addAttribute("lotsTermines", mouvementStockService.getLotsTermines());
        model.addAttribute("motifs", mouvementStockService.getAllMotifsSortie());
        return "stitch/module_stock/edit_mouvement";
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
        sortReferencesEtatStock.put("produit", "produit_nom");
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
        mav.addObject("totalEntree", mouvementStockService.getTotalEntreeGlobal());
        mav.addObject("totalSortie", mouvementStockService.getTotalSortieGlobal());
        mav.addObject("stockRestant", mouvementStockService.getStockRestantGlobal());
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

    private static final int TAILLE_EXPORT_MAX = 1_000_000;

    @GetMapping("stock/sortie/export")
    public ResponseEntity<byte[]> exporterSorties(@ModelAttribute SortieCriteriaWrapper wrapper) throws IOException {
        Pageable pageable;
        if (wrapper.getCurrentSort() != null && wrapper.getCurrentDir() != null
                && !wrapper.getCurrentSort().isEmpty() && !wrapper.getCurrentDir().isEmpty()) {
            String sort = sortReferencesSorties.get(wrapper.getCurrentSort());
            Sort.Direction direction = wrapper.getCurrentDir().equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            pageable = PageRequest.of(0, TAILLE_EXPORT_MAX, Sort.by(direction, sort));
        } else {
            pageable = PageRequest.of(0, TAILLE_EXPORT_MAX);
        }

        byte[] fichier = exportService.genererExcelSorties(wrapper, pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "sorties_stock.xlsx");

        return ResponseEntity.ok().headers(headers).body(fichier);
    }
}