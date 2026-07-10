package com.example.charbonecolo.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.charbonecolo.model.EmployeModel;
import com.example.charbonecolo.model.SalaireHistoriqueModel;
import com.example.charbonecolo.service.EmployeService;

@Controller
@RequestMapping("/employes")
public class EmployeController {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "reference", "nom", "dateEmbauche", "emploi.libelle");
    private static final String DEFAULT_SORT_FIELD = "nom";
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final EmployeService employeService;

    public EmployeController(EmployeService employeService) {
        this.employeService = employeService;
    }

    public record PageLink(int index, int number, boolean current, boolean ellipsis) {}

    public static String toggleSortColumn(String currentSort, String column) {
        if (currentSort != null && currentSort.startsWith(column + ",")) {
            String dir = currentSort.substring(column.length() + 1);
            if ("asc".equalsIgnoreCase(dir)) {
                return column + ",desc";
            }
            return null;
        }
        return column + ",asc";
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
    public String listeEmployes(
            @RequestParam(defaultValue = "") String nom,
            @RequestParam(defaultValue = "") String reference,
            @RequestParam(required = false) Integer idEmploi,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

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
        Page<EmployeModel> result = employeService.searchEmployes(nom, reference, idEmploi, pageable);

        if (!model.containsAttribute("employeModel")) {
            model.addAttribute("employeModel", new EmployeModel());
        }

        model.addAttribute("listeEmployes", result.getContent());
        model.addAttribute("pageNumber", result.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("totalElements", result.getTotalElements());
        model.addAttribute("hasPrevious", result.hasPrevious());
        model.addAttribute("hasNext", result.hasNext());
        model.addAttribute("pagination", buildPagination(result.getNumber(), result.getTotalPages()));

        model.addAttribute("nom", nom);
        model.addAttribute("reference", reference);
        model.addAttribute("idEmploi", idEmploi);
        model.addAttribute("sortParam", sortParam);
        model.addAttribute("emplois", employeService.getAllEmplois());

        return "employe/employes";
    }

    @GetMapping("/update/{id}")
    public String getUpdateForm(@PathVariable Integer id, RedirectAttributes rad) {
        try {
            EmployeModel employe = employeService.getById(id);
            rad.addFlashAttribute("employeModel", employe);
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Employé introuvable.");
        }
        return "redirect:/employes/home";
    }

    @PostMapping("/add")
    public String addEmploye(EmployeModel employe, RedirectAttributes rad) {
        try {
            employeService.saveEmploye(employe);
            rad.addFlashAttribute("success", "Employé ajouté avec succès !");
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Erreur lors de l'ajout.");
        }
        return "redirect:/employes/home";
    }

    @PostMapping("/update")
    public String updateEmploye(EmployeModel employe, RedirectAttributes rad) {
        if (employe.getId() == null) {
            rad.addFlashAttribute("error", "Impossible de modifier un employé sans identifiant.");
            return "redirect:/employes/home";
        }
        try {
            EmployeModel existing = employeService.getById(employe.getId());
            existing.setNom(employe.getNom());
            existing.setDateEmbauche(employe.getDateEmbauche());
            existing.setEmploi(employe.getEmploi());
            employeService.saveEmploye(existing);
            rad.addFlashAttribute("success", "Employé modifié avec succès !");
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Erreur lors de la modification.");
        }
        return "redirect:/employes/home";
    }

    @GetMapping("/delete/{id}")
    public String deleteEmploye(@PathVariable Integer id, RedirectAttributes rad) {
        try {
            employeService.deleteById(id);
            rad.addFlashAttribute("success", "Employé supprimé avec succès.");
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Impossible de supprimer cet employé.");
        }
        return "redirect:/employes/home";
    }

    @GetMapping("/salaire/{id}")
    public String ficheSalaire(@PathVariable Integer id,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model) {
        EmployeModel employe = employeService.getById(id);
        model.addAttribute("employe", employe);
        model.addAttribute("salaireBase", employe.getSalaireBase());
        model.addAttribute("totalSalaire", employe.getTotalSalaire());

        int histSize = Math.min(Math.max(size, 1), 10);
        Pageable pageable = PageRequest.of(Math.max(page, 0), histSize);
        Page<SalaireHistoriqueModel> histo = employeService.getHistoriqueByEmployeId(id, pageable);

        model.addAttribute("historique", histo.getContent());
        model.addAttribute("histPageNumber", histo.getNumber());
        model.addAttribute("histTotalPages", histo.getTotalPages());
        model.addAttribute("histHasPrevious", histo.hasPrevious());
        model.addAttribute("histHasNext", histo.hasNext());
        model.addAttribute("histPagination", buildPagination(histo.getNumber(), histo.getTotalPages()));

        return "employe/fiche_salaire";
    }

    @GetMapping("/salarier")
    public String formulaireSalarier(@RequestParam(required = false) Integer id, Model model) {
        if (id != null) {
            EmployeModel employe = employeService.getById(id);
            model.addAttribute("employe", employe);
            model.addAttribute("salaireBase", employe.getSalaireBase());
        } else {
            model.addAttribute("employe", null);
            model.addAttribute("salaireBase", BigDecimal.ZERO);
        }
        model.addAttribute("employes", employeService.getAllEmployes());
        model.addAttribute("emplois", employeService.getAllEmplois());
        return "employe/salarier";
    }

    @PostMapping("/salarier")
    public String enregistrerSalarier(@RequestParam Integer employeId,
                                      @RequestParam BigDecimal salaireBase,
                                      @RequestParam BigDecimal prime,
                                      @RequestParam BigDecimal indemnite,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEffet,
                                      RedirectAttributes rad) {
        try {
            employeService.salarier(employeId, salaireBase, prime, indemnite, dateEffet);
            rad.addFlashAttribute("success", "Salaire enregistré avec succès !");
            return "redirect:/employes/salaire/" + employeId;
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Erreur lors de l'enregistrement du salaire.");
            return "redirect:/employes/salarier?id=" + employeId;
        }
    }
}
