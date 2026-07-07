package com.example.charbonecolo.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import com.example.charbonecolo.model.FournisseurModel;
import com.example.charbonecolo.service.FournisseurService;
import com.example.charbonecolo.util.ImportResult;

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

    public record PageLink(int index, int number, boolean current, boolean ellipsis) {
    }

    /**
     * Tri simple : String unique.
     * null = retour au tri par défaut.
     */
    public static String toggleSortColumn(String currentSort, String column) {
        if (currentSort != null && currentSort.startsWith(column + ",")) {
            String dir = currentSort.substring(column.length() + 1);
            if ("asc".equalsIgnoreCase(dir)) {
                return column + ",desc";
            }
            return null; // 3e clic : suppression
        }
        return column + ",asc";
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
            RedirectAttributes attributsRedirect) {
        try {
            ImportResult resultat = fournisseurService.importerFournisseursCsv(fichier);

            if (resultat.getSuccessCount() > 0) {
                attributsRedirect.addFlashAttribute("success",
                        resultat.getSuccessCount() + " fournisseur(s) importé(s).");
            }
            if (resultat.hasWarnings()) {
                attributsRedirect.addFlashAttribute("warnings", resultat.getWarnings());
            }
            if (resultat.hasErrors()) {
                attributsRedirect.addFlashAttribute("importErrors", resultat.getErrors());
                return "redirect:/fournisseur/import";
            }

            return "redirect:/fournisseur/home";

        } catch (IllegalArgumentException e) {
            attributsRedirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/fournisseur/import";
        } catch (IOException e) {
            attributsRedirect.addFlashAttribute("error", "Erreur lecture fichier : " + e.getMessage());
            return "redirect:/fournisseur/import";
        }
    }

    @GetMapping("/home")
    public String getFournisseurList(
            @RequestParam(defaultValue = "") String nom,
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "") String telephone,
            @RequestParam(defaultValue = "") String adresse,
            @RequestParam(required = false) String actif,
            @RequestParam(required = false) String sort, // ← String simple, pas List
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

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

        if (!model.containsAttribute("fournisseurModel")) {
            model.addAttribute("fournisseurModel", new FournisseurModel());
        }

        model.addAttribute("listeFournisseurs", result.getContent());
        model.addAttribute("pageNumber", result.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("totalElements", result.getTotalElements());
        model.addAttribute("hasPrevious", result.hasPrevious());
        model.addAttribute("hasNext", result.hasNext());
        model.addAttribute("pagination", buildPagination(result.getNumber(), result.getTotalPages()));

        model.addAttribute("nom", nom);
        model.addAttribute("email", email);
        model.addAttribute("telephone", telephone);
        model.addAttribute("adresse", adresse);
        model.addAttribute("actif", actif);
        model.addAttribute("sortParam", sortParam); // ← String simple

        return "fournisseur/fournisseurs";
    }

    @GetMapping("/update/{id}")
    public String getUpdateForm(@PathVariable("id") Integer id, RedirectAttributes rad) {
        if (id <= 0) {
            rad.addFlashAttribute("error", "Identifiant du fournisseur invalide.");
            return "redirect:/fournisseur/home";
        }

        try {
            FournisseurModel fournisseurModel = fournisseurService.getById(id);
            rad.addFlashAttribute("fournisseurModel", fournisseurModel);
        } catch (NoSuchElementException e) {
            rad.addFlashAttribute("error", "Action impossible : Element inexistant");
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Une erreur est survenue lors de la récupération du fournisseur.");
        }
        return "redirect:/fournisseur/home";
    }

    @PostMapping("/add")
    public String addFournisseur(
            @Valid @ModelAttribute("fournisseurModel") FournisseurModel fournisseurModel,
            BindingResult result,
            Model model,
            RedirectAttributes rad) {
        if (result.hasErrors()) {
            model.addAttribute("listeFournisseurs", fournisseurService.getAll());
            return "fournisseur/fournisseurs";
        }
        try {
            fournisseurModel.setDate_creation(LocalDateTime.now());

            fournisseurService.persistFournisseur(fournisseurModel);
            rad.addFlashAttribute("success", "Le fournisseur a été ajouté avec succès !");
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Une erreur est survenue lors de l'ajout.");
        }
        return "redirect:/fournisseur/home";
    }

    @PostMapping("/update")
    public String updateFournisseur(
            @Valid @ModelAttribute("fournisseurModel") FournisseurModel fournisseurModel,
            BindingResult result,
            Model model,
            RedirectAttributes rad) {
        if (fournisseurModel.getId() == null) {
            rad.addFlashAttribute("error", "Impossible de modifier un fournisseur sans identifiant.");
            return "redirect:/fournisseur/home";
        }
        if (result.hasErrors()) {
            model.addAttribute("listeFournisseurs", fournisseurService.getAll());
            return "fournisseur/fournisseurs";
        }
        try {
            fournisseurService.getById(fournisseurModel.getId());
            fournisseurService.persistFournisseur(fournisseurModel);
            rad.addFlashAttribute("success", "Le fournisseur a été modifié avec succès !");
        } catch (NoSuchElementException e) {
            rad.addFlashAttribute("error", "Action impossible : Element inexistant");
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Une erreur est survenue lors de la modification.");
        }
        return "redirect:/fournisseur/home";
    }

    @GetMapping("/delete/{id}")
    public String deleteFournisseur(@PathVariable("id") Integer id, RedirectAttributes rad) {
        if (id <= 0) {
            rad.addFlashAttribute("error", "Action impossible : Identifiant de suppression invalide.");
            return "redirect:/fournisseur/home";
        }

        try {
            fournisseurService.getById(id);
            fournisseurService.deleteById(id);
            rad.addFlashAttribute("success", "Le fournisseur a été supprimé avec succès.");
        } catch (NoSuchElementException e) {
            rad.addFlashAttribute("error", "Action impossible : Element inexistant");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            rad.addFlashAttribute("error",
                    "Impossible de supprimer ce fournisseur car il est lié à des matières premières.");
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Une erreur inconnue est survenue lors de la suppression.");
        }
        return "redirect:/fournisseur/home";
    }
}