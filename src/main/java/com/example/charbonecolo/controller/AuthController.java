package com.example.charbonecolo.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.charbonecolo.dto.AlerteProduitDTO;
import com.example.charbonecolo.dto.MouvementMensuelDTO;
import com.example.charbonecolo.exception.BusinessException;
import com.example.charbonecolo.model.UtilisateurModel;
import com.example.charbonecolo.repository.ClientRepository;
import com.example.charbonecolo.service.EmployeService;
import com.example.charbonecolo.service.FournisseurService;
import com.example.charbonecolo.service.JournalFinancierService;
import com.example.charbonecolo.service.MouvementStockService;
import com.example.charbonecolo.service.ProduitService;
import com.example.charbonecolo.service.UtilisateurService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final UtilisateurService utilisateurService;
    private final MouvementStockService mouvementStockService;
    private final FournisseurService fournisseurService;
    private final JournalFinancierService journalFinancierService;
    private final ProduitService produitService;
    private final EmployeService employeService;
    private final ClientRepository clientRepository;

    public AuthController(UtilisateurService utilisateurService,
                          MouvementStockService mouvementStockService,
                          FournisseurService fournisseurService,
                          JournalFinancierService journalFinancierService,
                          ProduitService produitService,
                          EmployeService employeService,
                          ClientRepository clientRepository) {
        this.utilisateurService = utilisateurService;
        this.mouvementStockService = mouvementStockService;
        this.fournisseurService = fournisseurService;
        this.journalFinancierService = journalFinancierService;
        this.produitService = produitService;
        this.employeService = employeService;
        this.clientRepository = clientRepository;
    }

    private static LocalDateTime debutMois() {
        return LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth())
                .withHour(0).withMinute(0).withSecond(0);
    }

    private static LocalDateTime finMois() {
        return LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth())
                .withHour(23).withMinute(59).withSecond(59);
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

        if ("ADMIN".equals(user.getRole().getLibelle())) {
            LocalDateTime debut = debutMois();
            LocalDateTime fin = finMois();
            LocalDateTime debutPrev = debut.minusMonths(1);
            LocalDateTime finPrev = debut.minusSeconds(1);

            model.addAttribute("ca", journalFinancierService.calculerCA(debut, fin));
            model.addAttribute("caPrecedent", journalFinancierService.calculerCA(debutPrev, finPrev));
            model.addAttribute("benefice", journalFinancierService.calculerBenefice(debut, fin));
            model.addAttribute("totalEntrees", journalFinancierService.calculerTotalEntrees(debut, fin));
            model.addAttribute("totalSorties", journalFinancierService.calculerTotalSorties(debut, fin));
            model.addAttribute("solde", journalFinancierService.calculerSolde());
            model.addAttribute("evolutionCA", journalFinancierService.evolutionMensuelleCA());

            model.addAttribute("stockRestant", mouvementStockService.getStockRestantGlobal());
            model.addAttribute("nombreLotsFinis", mouvementStockService.getNombreLotsFinis());
            model.addAttribute("alertesActives", mouvementStockService.getAlertesActives());
            model.addAttribute("nbAlertes", mouvementStockService.countAlertStock());
            model.addAttribute("mouvementsParMois", mouvementStockService.getMouvementsParMois());

            model.addAttribute("nombreFournisseursActifs", fournisseurService.getNombreFournisseursActifs());
            model.addAttribute("nbProduits", produitService.findAll().size());
            model.addAttribute("nbEmployes", employeService.getAllEmployes().size());
            model.addAttribute("nbClients", clientRepository.count());

            return "dashboard";
        }

        return "dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
