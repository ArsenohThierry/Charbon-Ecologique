package com.example.charbonecolo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.charbonecolo.model.TypeMatierePremiereModel;
import com.example.charbonecolo.service.TypeMatierePremiereService;
import com.example.charbonecolo.service.FournisseurService;

@Controller
@RequestMapping("/matiere")
public class TypeMatierePremiereController {

    private TypeMatierePremiereService typeMatierePremiereService;
    private FournisseurService fournisseurService;

    public TypeMatierePremiereController(TypeMatierePremiereService typeMatierePremiereService,
            FournisseurService fournisseurService) {
        this.typeMatierePremiereService = typeMatierePremiereService;
        this.fournisseurService = fournisseurService;
    }

    @GetMapping("/home")
    public ModelAndView getMatiereHome(@ModelAttribute("typeMatiere") TypeMatierePremiereModel typeMatiere) {
        ModelAndView mav = new ModelAndView("matiere_premiere/types_mat_prem");
        mav.addObject("listeMatieres", typeMatierePremiereService.getAll());
        mav.addObject("listeFournisseurs", fournisseurService.getAll());
        
        if (typeMatiere == null || typeMatiere.getId() == null) {
            mav.addObject("typeMatiere", new TypeMatierePremiereModel());
        }

        return mav;
    }

    @PostMapping("/ajouter")
    public String ajouterMatiere(@ModelAttribute("typeMatiere") TypeMatierePremiereModel typeMatiere,
            BindingResult result,
            @RequestParam("id_fournisseur") Integer idFournisseur,
            RedirectAttributes rad) {

        if (result.hasErrors()) {
            rad.addFlashAttribute("error", "Une erreur est survenue lors de la saisie des données.");
            return "redirect:/matiere/home";
        }

        try {
            String message = (typeMatiere.getId() != null) 
                ? "Le type de matière première a bien été mis à jour !" 
                : "Le type de matière première a bien été inséré !";

            typeMatierePremiereService.saveMatiere(typeMatiere, idFournisseur);
            rad.addFlashAttribute("success", message);
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Erreur lors de l'enregistrement : " + e.getMessage());
        }

        return "redirect:/matiere/home";
    }

    @GetMapping("/modifier/{id}")
    public String chargerPourModification(@PathVariable("id") Integer id, RedirectAttributes rad) {
        try {
            TypeMatierePremiereModel matiereExistante = typeMatierePremiereService.getById(id);
            rad.addFlashAttribute("typeMatiere", matiereExistante);
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Impossible de charger la matière : " + e.getMessage());
        }
        return "redirect:/matiere/home";
    }

    @GetMapping("/supprimer/{id}")
    public String supprimerMatiere(@PathVariable("id") Integer id, RedirectAttributes rad) {
        try {
            typeMatierePremiereService.deleteById(id); 
            rad.addFlashAttribute("success", "Le type de matière première a été supprimé avec succès.");
        } catch (Exception e) {
            rad.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/matiere/home";
    }
}