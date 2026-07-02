package com.example.charbonecolo.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.charbonecolo.dto.CommandeDto;
import com.example.charbonecolo.dto.CommandeInput;
import com.example.charbonecolo.exception.InvalidCommandeException;
import com.example.charbonecolo.model.ClientModel;
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

    private static final Map<String, String> sortReferences;

    private final CommandeRepository commandeRepository;
    private final CommandeService commandeService;
    private final ClientService clientService;
    private final ProduitService produitService;
    static {
        sortReferences = new HashMap<>();
        sortReferences.put("reference", "reference");
        sortReferences.put("client_nom", "cli.nom");
        sortReferences.put("date", "date_commande");
        sortReferences.put("montant", "montant_total");
    }

    public CommandeController(CommandeRepository commandeRepository, CommandeService commandeService, ClientService clientService, ProduitService produitService) {
        this.commandeRepository = commandeRepository;
        this.commandeService = commandeService;
        this.clientService = clientService;
        this.produitService = produitService;
    }

    @GetMapping("/update/{id}")
    public ModelAndView updateFormDisplay(@PathVariable("id") Integer id, HttpSession session) {
        ModelAndView mav = new ModelAndView("stitch/module_commercial/modif_commande");
        CommandeModel commande = commandeService.findById(id);
        List<ProduitModel> produits = produitService.findAll();
        session.setAttribute("tmp_cmd_update", commande);
        List<DetailCommandeModel> details = commandeService.findDetails(id);
        Double montant = details.stream().mapToDouble(e -> e.findMontant()).sum();
        mav.addObject("details", details);
        mav.addObject("commande", commande);
        mav.addObject("total", montant);
        mav.addObject("produits", produits);
        return mav;
    }

    @PostMapping("/detail/update/add")
    public ModelAndView addDetailUpdateMode(HttpSession session, @ModelAttribute DetailCommandeModel detail) {
        CommandeModel tmp = (CommandeModel) session.getAttribute("tmp_cmd_update");
        detail.setCommande(tmp);
        detail.setMontant(new BigDecimal(0));
        commandeService.saveDetail(detail);
        ModelAndView mav = new ModelAndView("redirect:/cmd/update/" + tmp.getId());
        return mav;
    }

    @PostMapping("/detail/delete")
    public ModelAndView deleteDetail(@RequestParam("id") Integer idDetail, HttpSession session) {
        CommandeModel tmp = (CommandeModel) session.getAttribute("tmp_cmd_update");
        ModelAndView mav = new ModelAndView("redirect:/cmd/update/" + tmp.getId());
        commandeService.deleteDetail(idDetail);
        return mav;   
    }

    @PostMapping("/detail/update")
    public ModelAndView updateDetail(HttpSession session, @ModelAttribute DetailCommandeModel detail, RedirectAttributes ra) {
        CommandeModel tmp = (CommandeModel) session.getAttribute("tmp_cmd_update");
        detail.setCommande(tmp);
        detail.setMontant(new BigDecimal(0));
        commandeService.saveDetail(detail);
        ModelAndView mav = new ModelAndView("redirect:/cmd/update/" + tmp.getId());
        return mav;
    }


    @PostMapping("update")
    public ModelAndView update(@ModelAttribute CommandeModel commande, HttpSession session, RedirectAttributes ra, @ModelAttribute CommandeInput input) {
        CommandeModel tmp = (CommandeModel) session.getAttribute("tmp_cmd_update");
        ModelAndView mav = new ModelAndView("redirect:/cmd/update/" + tmp.getId());
        try {
            commandeService.checkCommandeEntry(commande);
        } catch (InvalidCommandeException e) {
            ra.addFlashAttribute("clientError", e.getFieldErrors());
            ra.addFlashAttribute("input", input);
            return mav;
        }
        commande.setId(tmp.getId());
        commande.setDeletedAt(tmp.getDeletedAt());
        commande.setReference(tmp.getReference());
        commande.setDateCommande(tmp.getDateCommande());
        commandeService.save(commande);
        return mav;
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

    @ResponseBody
    @GetMapping("/api/cli")
    public List<ClientModel> listClientsJson(@RequestParam(name = "kw", required = false, defaultValue = "") String keyWord) {
        return clientService.findByName(keyWord);
    }

    @GetMapping
    public ModelAndView list(
        HttpSession session, 
        @RequestParam(required = false, name = "page", defaultValue = "1") Integer page, 
        @RequestParam(required = false, name = "limit", defaultValue = "10") Integer limit,
        @RequestParam(required = false, name = "sort") String currentSort,
        @RequestParam(required = false, name = "dir") String currentDir,
        @RequestParam(required = false, name = "kw") String keyWord
    ) { 
        Pageable pageable = null;
        if(currentSort != null && currentDir != null) {
            if(!currentSort.isEmpty() && !currentDir.isEmpty()) {
                String sort = sortReferences.get(currentSort);
                Sort.Direction direction = currentDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
                pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sort));
            }
        }
        if(pageable == null) {
                pageable = PageRequest.of(page - 1, limit);
        }
        Page<CommandeDto> pageService = commandeService.listCommandes(pageable, keyWord);
        ModelAndView mav = new ModelAndView("stitch/module_commercial/liste_commande");
        mav.addObject("commandes", pageService.getContent());
        mav.addObject("currentPage", page);
        mav.addObject("currentDir", currentDir);
        mav.addObject("currentSort", currentSort);
        mav.addObject("page", pageService);
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
