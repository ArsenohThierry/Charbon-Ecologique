package com.example.charbonecolo.controller;

import java.io.IOException;
import jakarta.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.example.charbonecolo.dto.ImportResult;
import com.example.charbonecolo.dto.PageLink;
import com.example.charbonecolo.model.FournisseurModel;
import com.example.charbonecolo.service.FournisseurService;

@Controller
@RequestMapping("/fournisseur")
public class FournisseurController {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "nom", "email", "telephone", "adresse",
            "actif");
    private static final String DEFAULT_SORT_FIELD = "nom";
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final FournisseurService fournisseurService;

    public FournisseurController(FournisseurService fournisseurService) {
        this.fournisseurService = fournisseurService;
    }

    @ModelAttribute("allowedSortFields")
    public List<String> allowedSortFields() {
        return ALLOWED_SORT_FIELDS;
    }
    
    private List<PageLink> buildPagination(int currentPage, int totalPages) {
        List<PageLink> links = new ArrayList<>();
        links.add(new PageLink(0, 1, currentPage == 0, false));
        if (currentPage > 2)
            links.add(new PageLink(-1, 0, false, true));
        for (int i = Math.max(1, currentPage - 1); i <= Math.min(totalPages - 2, currentPage + 1); i++) {
            links.add(new PageLink(i, i + 1, i == currentPage, false));
        }
        if (currentPage < totalPages - 3)
            links.add(new PageLink(-1, 0, false, true));
        if (totalPages > 1)
            links.add(new PageLink(totalPages - 1, totalPages, currentPage == totalPages - 1, false));
        return links;
    }

    @GetMapping("/import")
    public String afficherFormulaireImport() {
        return "fournisseur/import";
    }

    @PostMapping("/import")
    public String traiterImportCsv(@RequestParam("fichier") MultipartFile fichier,
            RedirectAttributes rad) {
        try {
            ImportResult resultat = fournisseurService.importerFournisseursCsv(fichier);
            if (resultat.getSuccessCount() > 0) {
                rad.addFlashAttribute("success",
                        resultat.getSuccessCount() + " fournisseur(s) importé(s).");
            }
            if (resultat.hasWarnings()) {
                rad.addFlashAttribute("warnings", resultat.getWarnings());
            }
            if (resultat.hasErrors()) {
                rad.addFlashAttribute("importErrors", resultat.getErrors());
                return "redirect:/fournisseur/import";
            }
            return "redirect:/fournisseur/home";

        } catch (IllegalArgumentException e) {
            rad.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            rad.addFlashAttribute("error", "Erreur lecture fichier : " + e.getMessage());
        }
        return "redirect:/fournisseur/import";
    }

    @GetMapping("/home")
    public ModelAndView getFournisseurList(
            @RequestParam(defaultValue = "") String nom,
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "") String telephone,
            @RequestParam(defaultValue = "") String adresse,
            @RequestParam(required = false) String actif,
            @RequestParam(required = false) String sort, // ← String simple, pas List
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Boolean actifBool = (actif != null && !actif.isBlank()) ? Boolean.parseBoolean(actif) : null;

        // ─── Tri simple ───
        Sort sortObj;
        String sortParam;

        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            if (parts.length == 2 && ALLOWED_SORT_FIELDS.contains(parts[0].trim())) {
                String field = parts[0].trim();
                String dir = parts[1].trim().toLowerCase();
                sortObj = Sort.by("desc".equals(dir) ? Sort.Direction.DESC : Sort.Direction.ASC, field);
                sortParam = sort;
            } else {
                sortObj = Sort.by(Sort.Direction.ASC, DEFAULT_SORT_FIELD);
                sortParam = DEFAULT_SORT_FIELD + ",asc";
            }
        } else {
            sortObj = Sort.by(Sort.Direction.ASC, DEFAULT_SORT_FIELD);
            sortParam = DEFAULT_SORT_FIELD + ",asc";
        }

        int pageSize = Math.min(Math.max(size, 1), DEFAULT_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize, sortObj);
        Page<FournisseurModel> result = fournisseurService.searchFournisseurs(
                nom, email, telephone, adresse, actifBool, pageable);

        ModelAndView mav = new ModelAndView("fournisseur/list");
        mav.addObject("listeFournisseurs", result.getContent());
        mav.addObject("pageNumber", result.getNumber());
        mav.addObject("pageSize", pageSize);
        mav.addObject("totalPages", result.getTotalPages());
        mav.addObject("totalElements", result.getTotalElements());
        mav.addObject("hasPrevious", result.hasPrevious());
        mav.addObject("hasNext", result.hasNext());
        mav.addObject("pagination", buildPagination(result.getNumber(), result.getTotalPages()));
        mav.addObject("nom", nom);
        mav.addObject("email", email);
        mav.addObject("telephone", telephone);
        mav.addObject("adresse", adresse);
        mav.addObject("actif", actif);
        mav.addObject("sortParam", sortParam); 
        return mav;
    }

    @GetMapping("/form")
    public String getForm(Model model) {
        if(!model.containsAttribute("fournisseurModel")){
            System.out.println("OUi il contient deja");
            model.addAttribute("fournisseurModel", new FournisseurModel());            
        }
        return "fournisseur/form";
    }

    @GetMapping("/update/{id}")
    public String getUpdateForm(@PathVariable("id") Integer id, Model model, RedirectAttributes rad) {
        if (id <= 0) {
            rad.addFlashAttribute("error", "Identifiant du fournisseur invalide.");
            return "redirect:/fournisseur/form";
        }

        try {
            FournisseurModel fournisseurModel = fournisseurService.getById(id).get();
            model.addAttribute("fournisseurModel", fournisseurModel);
        } catch (NoSuchElementException e) {
            rad.addFlashAttribute("error", String.format("Le fournisseur avec la reference %d n'existe pas" + id));
        }
        return "fournisseur/form";
    }

    @PostMapping("/add")
    public String addFournisseur(
            @Valid @ModelAttribute("fournisseurModel") FournisseurModel fournisseurModel,
            BindingResult result,
            Model model,
            RedirectAttributes rad) {
        if (result.hasErrors()) {
            return "fournisseur/form";
        }

        try {
            String message = fournisseurModel.getId() != null && fournisseurModel.getId().intValue() > 0
                    ? "Le fournisseur a ete modifie avec succes"
                    : "Le fournisseur a ete ajoute avec succes";
            fournisseurService.persistFournisseur(fournisseurModel);
            rad.addFlashAttribute("success", message);
            return "redirect:/fournisseur/home";
        } catch (DataIntegrityViolationException e) {
            rad.addFlashAttribute("error", e.getMessage());
        }
        rad.addFlashAttribute("fournisseurModel", fournisseurModel);
        return "redirect:/fournisseur/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteFournisseur(@PathVariable("id") Integer id, RedirectAttributes rad) {
        if (id <= 0) {
            rad.addFlashAttribute("error", "Action impossible : Identifiant de suppression invalide.");
            return "redirect:/fournisseur/home";
        }

        try {
            fournisseurService.deleteById(id);
            rad.addFlashAttribute("success", "Le fournisseur a été supprimé avec succès.");
        } catch (NoSuchElementException e) {
            rad.addFlashAttribute("error", "Action impossible : Element inexistant");
        } catch (DataIntegrityViolationException e) {
            rad.addFlashAttribute("error",
                    "Impossible de supprimer ce fournisseur car il est lié à des matières premières.");
        }
        return "redirect:/fournisseur/home";
    }
}