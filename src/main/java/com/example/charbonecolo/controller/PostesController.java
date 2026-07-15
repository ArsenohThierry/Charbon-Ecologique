package com.example.charbonecolo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.charbonecolo.model.EmploiModel;
import com.example.charbonecolo.service.EmployeService;

@Controller
@RequestMapping("/postes")
public class PostesController {

    private final EmployeService employeService;

    public PostesController(EmployeService employeService) {
        this.employeService = employeService;
    }

    @GetMapping("/home")
    public String listePostes(Model model) {
        List<EmploiModel> postes = employeService.getAllEmplois();
        if (!model.containsAttribute("emploiModel")) {
            model.addAttribute("emploiModel", new EmploiModel());
        }
        model.addAttribute("postes", postes);
        return "postes/home";
    }

    @PostMapping("/add")
    public String addPoste(EmploiModel emploi, RedirectAttributes rad) {
        try {
            employeService.saveEmploi(emploi);
            rad.addFlashAttribute("success", "Poste ajouté avec succès !");
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Erreur lors de l'ajout du poste.");
        }
        return "redirect:/postes/home";
    }

    @GetMapping("/update/{id}")
    public String getUpdateForm(@PathVariable Integer id, RedirectAttributes rad) {
        try {
            EmploiModel emploi = employeService.getEmploiById(id);
            rad.addFlashAttribute("emploiModel", emploi);
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Poste introuvable.");
        }
        return "redirect:/postes/home";
    }

    @PostMapping("/update")
    public String updatePoste(EmploiModel emploi, RedirectAttributes rad) {
        if (emploi.getId() == null) {
            rad.addFlashAttribute("error", "Impossible de modifier un poste sans identifiant.");
            return "redirect:/postes/home";
        }
        try {
            EmploiModel existing = employeService.getEmploiById(emploi.getId());
            existing.setLibelle(emploi.getLibelle());
            existing.setSalaire(emploi.getSalaire());
            employeService.saveEmploi(existing);
            rad.addFlashAttribute("success", "Poste modifié avec succès !");
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Erreur lors de la modification.");
        }
        return "redirect:/postes/home";
    }

    @GetMapping("/delete/{id}")
    public String deletePoste(@PathVariable Integer id, RedirectAttributes rad) {
        try {
            employeService.deleteEmploiById(id);
            rad.addFlashAttribute("success", "Poste supprimé avec succès.");
        } catch (Exception e) {
            rad.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/postes/home";
    }
}
