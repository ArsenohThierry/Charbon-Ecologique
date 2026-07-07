package com.example.charbonecolo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.charbonecolo.exception.BusinessException;
import com.example.charbonecolo.model.UtilisateurModel;
import com.example.charbonecolo.service.FournisseurService;
import com.example.charbonecolo.service.MouvementStockService;
import com.example.charbonecolo.service.UtilisateurService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final UtilisateurService utilisateurService;
    private final MouvementStockService mouvementStockService;
    private final FournisseurService fournisseurService;

    public AuthController(UtilisateurService utilisateurService, MouvementStockService mouvementStockService, FournisseurService fournisseurService) {
        this.utilisateurService = utilisateurService;
        this.mouvementStockService = mouvementStockService;
        this.fournisseurService = fournisseurService;
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        if (session == null) {
            return "redirect:/login";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
            HttpSession session, Model model) {
        try {
            UtilisateurModel user = utilisateurService.authenticate(username, password);
            session.setAttribute("user", user);
            return "redirect:/dashboard";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        UtilisateurModel user = (UtilisateurModel) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);

        if ("STOCK_MANAGER".equals(user.getRole().getLibelle())) {
            model.addAttribute("nombreLotsFinis", mouvementStockService.getNombreLotsFinis());
            model.addAttribute("nombreFournisseursActifs", fournisseurService.getNombreFournisseursActifs());
            model.addAttribute("mouvementsParMois", mouvementStockService.getMouvementsParMois());
            return "stitch/module_stock/dashboard";
        }

        return "dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
