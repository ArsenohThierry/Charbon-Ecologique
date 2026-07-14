package com.example.charbonecolo.controller;

import jakarta.validation.Valid;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.charbonecolo.dto.ImportResult;
import com.example.charbonecolo.dto.PageLink;
import com.example.charbonecolo.model.TypeMatierePremiereModel;
import com.example.charbonecolo.service.TypeMatierePremiereService;
import com.example.charbonecolo.service.FournisseurService;

@Controller
@RequestMapping("/matiere")
public class TypeMatierePremiereController {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("reference", "libelle", "prixUnitaire",
            "fournisseur.nom");
    private static final String DEFAULT_SORT_FIELD = "reference";
    private static final int DEFAULT_PAGE_SIZE = 10;

    private TypeMatierePremiereService typeMatierePremiereService;
    private FournisseurService fournisseurService;

    public TypeMatierePremiereController(TypeMatierePremiereService typeMatierePremiereService,
            FournisseurService fournisseurService) {
        this.typeMatierePremiereService = typeMatierePremiereService;
        this.fournisseurService = fournisseurService;
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

    @GetMapping("/home")
    public ModelAndView home(
            @RequestParam(defaultValue = "") String libelle,
            @RequestParam(required = false) BigDecimal prixMin,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(required = false) Integer idFournisseur,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) String actif,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // ─── Conversion des filtres ───
        Boolean actifBool = (actif != null && !actif.isBlank()) ? Boolean.parseBoolean(actif) : null;

        LocalDateTime dateDebutDt = (dateDebut != null) ? dateDebut.atStartOfDay() : null;
        LocalDateTime dateFinDt = (dateFin != null) ? dateFin.atTime(LocalTime.MAX) : null;

        // ─── Tri (même système que Fournisseur) ───
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

        // ─── Pagination ───
        int pageSize = Math.min(Math.max(size, 1), DEFAULT_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize, sortObj);
        Page<TypeMatierePremiereModel> result = typeMatierePremiereService.searchTypeMatieres(
                libelle, prixMin, prixMax, idFournisseur, dateDebutDt, dateFinDt, actifBool, pageable);

        ModelAndView mav = new ModelAndView("matiere_premiere/list");
        // ─── Modèle ───
        mav.addObject("listeMatieres", result.getContent());
        mav.addObject("listeFournisseurs", fournisseurService.getAll());

        mav.addObject("pageNumber", result.getNumber());
        mav.addObject("pageSize", pageSize);
        mav.addObject("totalPages", result.getTotalPages());
        mav.addObject("totalElements", result.getTotalElements());
        mav.addObject("hasPrevious", result.hasPrevious());
        mav.addObject("hasNext", result.hasNext());
        mav.addObject("pagination", buildPagination(result.getNumber(), result.getTotalPages()));

        // Valeurs des filtres pour ré-affichage
        mav.addObject("libelle", libelle);
        mav.addObject("prixMin", prixMin);
        mav.addObject("prixMax", prixMax);
        mav.addObject("idFournisseur", idFournisseur);
        mav.addObject("dateDebut", dateDebut);
        mav.addObject("dateFin", dateFin);
        mav.addObject("actif", actif);
        mav.addObject("sortParam", sortParam);

        return mav;
    }

    @GetMapping("/form")
    public String getForm(Model model) {
        if (!model.containsAttribute("typeMatiere")) {
            model.addAttribute("typeMatiere", new TypeMatierePremiereModel());
        }
        model.addAttribute("listeFournisseurs", fournisseurService.getAll());
        return "matiere_premiere/form";
    }

    @GetMapping("/update/{id}")
    public String loadTypeMatiere(@PathVariable("id") Integer id, RedirectAttributes rad) {
        if (id <= 0) {
            rad.addFlashAttribute("error", "Identifiant invalide.");
            return "redirect:/matiere/home";
        }

        try {
            TypeMatierePremiereModel matiereExistante = typeMatierePremiereService.getById(id);
            rad.addFlashAttribute("typeMatiere", matiereExistante);
        } catch (NoSuchElementException e) {
            rad.addFlashAttribute("error", "Action impossible : Élément inexistant.");
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Impossible de charger la matière : " + e.getMessage());
        }
        return "redirect:/matiere/form";
    }

    @PostMapping("/add")
    public String addMatiere(@Valid @ModelAttribute("typeMatiere") TypeMatierePremiereModel typeMatiere,
            BindingResult result,
            @RequestParam(value = "id_fournisseur", required = false) Integer idFournisseur,
            RedirectAttributes rad) {

        if (idFournisseur == null || idFournisseur <= 0) {
            result.rejectValue("fournisseur", "error.typeMatiere", "Le choix du fournisseur est obligatoire.");
        }

        if (result.hasErrors()) {
            rad.addFlashAttribute("typeMatiere", typeMatiere);
            rad.addFlashAttribute("selectedIdFournisseur", idFournisseur);
            return "redirect:/matiere/form";
        }

        try {
            typeMatierePremiereService.saveMatiere(typeMatiere, idFournisseur);
            rad.addFlashAttribute("success", "Type de matière ajouté avec succès !");
        } catch (NoSuchElementException e) {
            rad.addFlashAttribute("error", "Le fournisseur sélectionné n'existe pas.");
        }
        return "redirect:/matiere/home";
    }

    @PostMapping("/update")
    public String updateMatiere(@Valid @ModelAttribute("typeMatiere") TypeMatierePremiereModel typeMatiere,
            BindingResult result,
            @RequestParam(value = "id_fournisseur", required = false) Integer idFournisseur,
            RedirectAttributes rad) {
        if (typeMatiere.getId() == null) {
            rad.addFlashAttribute("error", "Impossible de modifier une matière sans identifiant.");
            return "redirect:/matiere/home";
        }

        if (idFournisseur == null || idFournisseur <= 0) {
            result.rejectValue("fournisseur", "error.typeMatiere", "Le choix du fournisseur est obligatoire.");
        }

        if (result.hasErrors()) {
            rad.addFlashAttribute("listeMatieres", typeMatierePremiereService.getAll());
            rad.addFlashAttribute("selectedIdFournisseur", idFournisseur);
            return "redirect:/matiere/form";
        }

        try {
            typeMatierePremiereService.getById(typeMatiere.getId());
            typeMatierePremiereService.saveMatiere(typeMatiere, idFournisseur);
            rad.addFlashAttribute("success", "Type de matière mis à jour !");
        } catch (NoSuchElementException e) {
            rad.addFlashAttribute("error", "Élément ou fournisseur introuvable.");
        }
        return "redirect:/matiere/home";
    }

    @GetMapping("/import")
    public String afficherFormulaireImport() {
        return "matiere_premiere/import";
    }

    @PostMapping("/import")
    public String traiterImportCsv(@RequestParam("fichier") MultipartFile fichier,
            RedirectAttributes rad) {
        try {
            ImportResult resultat = typeMatierePremiereService.importerTypeMatieresCsv(fichier);
            if (resultat.getSuccessCount() > 0) {
                rad.addFlashAttribute("success",
                        resultat.getSuccessCount() + " type(s) de matière importé(s).");
            }
            if (resultat.hasWarnings()) {
                rad.addFlashAttribute("warnings", resultat.getWarnings());
            }
            if (resultat.hasErrors()) {
                rad.addFlashAttribute("importErrors", resultat.getErrors());
                return "redirect:/matiere/import";
            }
            return "redirect:/matiere/home";

        } catch (IllegalArgumentException e) {
            rad.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            rad.addFlashAttribute("error", "Erreur lecture fichier : " + e.getMessage());
        }
        return "redirect:/matiere/import";
    }

    @GetMapping("/delete/{id}")
    public String deleteTypeMatiere(@PathVariable("id") Integer id, RedirectAttributes rad) {
        if (id <= 0) {
            rad.addFlashAttribute("error", "Action impossible : Identifiant de suppression invalide.");
            return "redirect:/matiere/home";
        }

        try {
            // Vérifie l’existence avant suppression
            typeMatierePremiereService.getById(id);
            typeMatierePremiereService.deleteById(id);
            rad.addFlashAttribute("success", "Le type de matière première a été supprimé avec succès.");
        } catch (NoSuchElementException e) {
            rad.addFlashAttribute("error", "Action impossible : Élément introuvable.");
        } catch (DataIntegrityViolationException e) {
            rad.addFlashAttribute("error",
                    "Impossible de supprimer ce type de matière car il est lié à d'autres flux de production.");
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/matiere/home";
    }
}