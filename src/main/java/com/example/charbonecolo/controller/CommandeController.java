package com.example.charbonecolo.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.charbonecolo.model.CommandeModel;
import com.example.charbonecolo.model.DetailCommandeModel;
import com.example.charbonecolo.model.ProduitModel;
import com.example.charbonecolo.repository.CommandeRepository;
import com.example.charbonecolo.service.ClientService;
import com.example.charbonecolo.service.CommandeService;
import com.example.charbonecolo.service.ProduitService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/cmd")
public class CommandeController {

    private final CommandeRepository commandeRepository;
    private final CommandeService commandeService;
    private final ClientService clientService;
    private final ProduitService produitService;

    public CommandeController(CommandeRepository commandeRepository, CommandeService commandeService, ClientService clientService, ProduitService produitService) {
        this.commandeRepository = commandeRepository;
        this.commandeService = commandeService;
        this.clientService = clientService;
        this.produitService = produitService;
    }

    @GetMapping("/new")
    public ModelAndView commandeFormDisplay() {
        ModelAndView mav = new ModelAndView("stitch/module_commercial/nouvelle_commande");
        mav.addObject("clients", clientService.findAll());
        return mav;
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/new/products")
    public ModelAndView inputProducts(HttpSession session) {
        ModelAndView mav = new ModelAndView("stitch/module_commercial/form_produit");
        List<DetailCommandeModel> details = (List<DetailCommandeModel>) session.getAttribute("tmp_details");
        Double montantTotal = 0.0;
        if(details != null) {
            montantTotal = details.parallelStream().mapToDouble(e -> e.findMontant()).sum();
        }
        mav.addObject("produits", produitService.findAll());
        mav.addObject("panier", details);
        mav.addObject("montantTotal", montantTotal);
        return mav;
    }
    
    @PostMapping("/new/products")
    @SuppressWarnings("unchecked")
    public ModelAndView storeProductInSession(HttpSession session, @ModelAttribute DetailCommandeModel detail) {
        List<DetailCommandeModel> details = (List<DetailCommandeModel>) session.getAttribute("tmp_details");
        if(details == null) {
            details = new ArrayList<>();
            session.setAttribute("tmp_details", details);
        }
        ModelAndView mav = new ModelAndView("redirect:/cmd/new/products");
        ProduitModel found = produitService.findById(detail.getProduit().getId());
        detail.setProduit(found);
        details.add(detail);
        return mav;
    }

    @PostMapping("/new")
    public ModelAndView storeSession(HttpSession session, @ModelAttribute CommandeModel commande) {
        ModelAndView mav = new ModelAndView("redirect:/cmd/new/products");
        session.setAttribute("tmp_cmd", commande);
        commande.setDateCommande(LocalDateTime.now());
        return mav;
    }

    @GetMapping
    public ModelAndView list(
        HttpSession session, 
        @RequestParam(required = false, name = "page") Integer page, 
        @RequestParam(required = false, name = "limit") Integer limit,
        @RequestParam(required = false, name = "sort") String currentSort,
        @RequestParam(required = false, name = "dir") String currentDir
    ) {
        if(limit == null) {
            limit = 10;
        }
        if(page == null) {
            page = 1;
        }
        ModelAndView mav = new ModelAndView("stitch/module_commercial/liste_commande");
        mav.addObject("commandes", commandeService.listCommandes(page, limit));
        mav.addObject("currentPage", page);
        mav.addObject("currentDir", currentDir);
        mav.addObject("currentSort", currentSort);
        return mav;
    }

    @PostMapping("/save")
    @SuppressWarnings("unchecked")
    public ModelAndView saveCommande(HttpSession session) {
        ModelAndView mav = new ModelAndView("redirect:/cmd/new");
        List<DetailCommandeModel> details = (List<DetailCommandeModel>) session.getAttribute("tmp_details");
        CommandeModel commande = (CommandeModel) session.getAttribute("tmp_cmd");
        commandeService.save(commande, details);
        session.removeAttribute("tmp_details");
        session.removeAttribute("tmp_cmd");
        return mav;
    }
}
