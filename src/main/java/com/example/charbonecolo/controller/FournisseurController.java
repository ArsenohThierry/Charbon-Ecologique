package com.example.charbonecolo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.charbonecolo.model.FournisseurModel;
import com.example.charbonecolo.service.FournisseurService;

@Controller
@RequestMapping("/fournisseur")
public class FournisseurController {
    private final FournisseurService fournisseurService;

    public FournisseurController(FournisseurService fournisseurService) {
        this.fournisseurService = fournisseurService;
    }

    @GetMapping("")
    public ModelAndView getFournisseurs(){
        ModelAndView mav = new ModelAndView("");
        mav.addObject("listeFournisseurs", fournisseurService.getAll());
        return mav;
    }

    @GetMapping("/modifier")
    public String getUpdateForm(){
        return "redirect:/fournisseurs";
    }

    @PostMapping("/ajouter")
    public String ajouterFournisseur(FournisseurModel fournisseurModel){
        
        return "redirect:/fournisseurs";
    }

    @PostMapping("/modifier")
    public String modifierFournisseur(FournisseurModel fournisseurModel){
        return "redirect:/fournisseurs";
    }
}
