package com.example.charbonecolo.controller;

import com.example.charbonecolo.repository.TypeMatierePremiereRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/matieres-premieres")
public class TypeMatierePremiereController {

    private final TypeMatierePremiereRepository repository;

    public TypeMatierePremiereController(TypeMatierePremiereRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public String lister(Model model) {
        model.addAttribute("types", repository.findAll());
        return "types-matieres-premieres";
    }
}
