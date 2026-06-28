package com.example.charbonecolo.controller;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.charbonecolo.model.FournisseurModel;
import com.example.charbonecolo.service.FournisseurService;

@Controller
@RequestMapping("/fournisseur")
public class FournisseurController {
    private final FournisseurService fournisseurService;

    public FournisseurController(FournisseurService fournisseurService) {
        this.fournisseurService = fournisseurService;
    }

    @GetMapping("/home")
    public ModelAndView getFournisseurs(@ModelAttribute("fournisseurModel") FournisseurModel fournisseurModel) {
        ModelAndView mav = new ModelAndView("stitch/module_stock/fournisseurs");

        mav.addObject("listeFournisseurs", fournisseurService.getAll());
        if (fournisseurModel == null || fournisseurModel.getId() == null) {
            mav.addObject("fournisseurModel", new FournisseurModel());
        } else {
            mav.addObject("fournisseurModel", fournisseurModel);
        }

        return mav;
    }

    @GetMapping("/modifier")
    public String getUpdateForm(@RequestParam("id") Integer id, RedirectAttributes rad) {
        FournisseurModel fournisseurModel = fournisseurService.getById(id);
        rad.addFlashAttribute("fournisseurModel", fournisseurModel);
        return "redirect:/fournisseur/home";
    }

    @PostMapping("/ajouter")
    public String ajouterFournisseur(@Validated @ModelAttribute("fournisseurModel") FournisseurModel fournisseurModel,
            BindingResult result,
            RedirectAttributes rad) {
        fournisseurModel.setDate_creation(LocalDateTime.now());
        if (result.hasErrors()) {
            return "redirect:/fournisseur/home";
        }

        fournisseurService.persistFournisseur(fournisseurModel);
        rad.addFlashAttribute("success", "Le fournisseur a été ajouté avec succès !");

        return "redirect:/fournisseur/home";

    }

    @PostMapping("/modifier")
    public String modifierFournisseur(@Validated @ModelAttribute("fournisseurModel") FournisseurModel fournisseurModel,
            BindingResult result,
            RedirectAttributes rad) {
        if (result.hasErrors()) {
            return "fournisseurs/home";
        }

        fournisseurService.persistFournisseur(fournisseurModel);
        rad.addFlashAttribute("success", "Le fournisseur a été modifié avec succès !");

        return "redirect:/fournisseur/home";
    }
}
