package com.example.charbonecolo.controller;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.charbonecolo.model.TypeMatierePremiereModel;
import com.example.charbonecolo.service.TypeMatierePremiereService;
import com.example.charbonecolo.service.FournisseurService;

@Controller
@RequestMapping("/matiere")
public class TypeMatierePremiereController {

    private static final List<String> ALLOWED_SORT_FIELDS = 
            List.of("reference", "libelle", "prixUnitaire", "fournisseur.nom");
    private static final String DEFAULT_SORT_FIELD = "reference";
    private static final String DEFAULT_SORT_DIR = "asc";
    private static final int DEFAULT_PAGE_SIZE = 10;

    private TypeMatierePremiereService typeMatierePremiereService;
    private FournisseurService fournisseurService;

    public TypeMatierePremiereController(TypeMatierePremiereService typeMatierePremiereService,
                                 FournisseurService fournisseurService) {
        this.typeMatierePremiereService = typeMatierePremiereService;
        this.fournisseurService = fournisseurService;
    }

    public record PageLink(int index, int number, boolean current, boolean ellipsis) {}

    /**
     * Retourne la direction suivante pour une colonne.
     * Cycle : asc → desc → asc (retour au défaut).
     */
    public static String toggleSortDir(String currentColumn, String currentDir, String column) {
        if (column.equals(currentColumn)) {
            if ("asc".equalsIgnoreCase(currentDir)) return "desc";
            return "asc"; // 3e clic = retour à asc
        }
        return "asc"; // nouvelle colonne = toujours asc
    }

    private List<PageLink> buildPagination(int currentPage, int totalPages) {
        List<PageLink> links = new ArrayList<>();
        links.add(new PageLink(0, 1, currentPage == 0, false));
        if (currentPage > 2) links.add(new PageLink(-1, 0, false, true));
        for (int i = Math.max(1, currentPage - 1); i <= Math.min(totalPages - 2, currentPage + 1); i++) {
            links.add(new PageLink(i, i + 1, i == currentPage, false));
        }
        if (currentPage < totalPages - 3) links.add(new PageLink(-1, 0, false, true));
        if (totalPages > 1) links.add(new PageLink(totalPages - 1, totalPages, currentPage == totalPages - 1, false));
        return links;
    }

    @GetMapping("/home")
    public String home(
            @RequestParam(required = false) String sortColumn,
            @RequestParam(required = false) String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        String column = (sortColumn != null && ALLOWED_SORT_FIELDS.contains(sortColumn)) ? sortColumn : DEFAULT_SORT_FIELD;
        String dir = (sortDir != null && "desc".equalsIgnoreCase(sortDir)) ? "desc" : "asc";
        Sort sortObj = Sort.by("desc".equals(dir) ? Sort.Direction.DESC : Sort.Direction.ASC, column);

        int pageSize = Math.min(Math.max(size, 1), DEFAULT_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize, sortObj);
        Page<TypeMatierePremiereModel> result = typeMatierePremiereService.findAllPaginated(pageable);

        if (!model.containsAttribute("typeMatiere")) {
            model.addAttribute("typeMatiere", new TypeMatierePremiereModel());
        }
        model.addAttribute("listeMatieres", result.getContent());
        model.addAttribute("listeFournisseurs", fournisseurService.getAll());

        model.addAttribute("pageNumber", result.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("totalElements", result.getTotalElements());
        model.addAttribute("hasPrevious", result.hasPrevious());
        model.addAttribute("hasNext", result.hasNext());
        model.addAttribute("pagination", buildPagination(result.getNumber(), result.getTotalPages()));

        model.addAttribute("sortColumn", column);
        model.addAttribute("sortDir", dir);

        return "matiere_premiere/types_mat_prem";
    }

    @PostMapping("/add")
    public String addMatiere(@Valid @ModelAttribute("typeMatiere") TypeMatierePremiereModel typeMatiere,
            BindingResult result,
            @RequestParam(value = "id_fournisseur", required = false) Integer idFournisseur,
            Model model,
            RedirectAttributes rad) {

        if (idFournisseur == null || idFournisseur <= 0) {
            result.rejectValue("fournisseur", "error.typeMatiere", "Le choix du fournisseur est obligatoire.");
        }

        if (result.hasErrors()) {
            model.addAttribute("listeFournisseurs", fournisseurService.getAll());
            model.addAttribute("listeMatieres", typeMatierePremiereService.getAll());
            model.addAttribute("selectedIdFournisseur", idFournisseur);
            return "matiere_premiere/types_mat_prem"; // Pas de redirection
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
            Model model,
            RedirectAttributes rad) {
        if (typeMatiere.getId() == null) {
            rad.addFlashAttribute("error", "Impossible de modifier une matière sans identifiant.");
            return "redirect:/matiere/home";
        }

        if (idFournisseur == null || idFournisseur <= 0) {
            result.rejectValue("fournisseur", "error.typeMatiere", "Le choix du fournisseur est obligatoire.");
        }

        if (result.hasErrors()) {
            model.addAttribute("listeFournisseurs", fournisseurService.getAll());
            model.addAttribute("listeMatieres", typeMatierePremiereService.getAll());
            model.addAttribute("selectedIdFournisseur", idFournisseur);
            return "matiere_premiere/types_mat_prem";
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
        return "redirect:/matiere/home";
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