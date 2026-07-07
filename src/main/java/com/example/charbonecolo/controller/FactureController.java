package com.example.charbonecolo.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.example.charbonecolo.dto.FactureCriteriaWrapper;
import com.example.charbonecolo.dto.FactureDto;
import com.example.charbonecolo.dto.FactureErrorWrapper;
import com.example.charbonecolo.model.CommandeModel;
import com.example.charbonecolo.repository.CommandeRepository;
import com.example.charbonecolo.repository.MethodePaiementRepository;
import com.example.charbonecolo.service.PaiementService;

@Controller
@RequestMapping("/factures")
public class FactureController {

    private static final Map<String, String> sortReferences;

    private final PaiementService paiementService;
    private final CommandeRepository commandeRepository;
    private final MethodePaiementRepository methodePaiementRepository;

    static {
        sortReferences = new HashMap<>();
        sortReferences.put("reference", "reference");
        sortReferences.put("date_commande", "date_commande");
        sortReferences.put("client_nom", "client_nom");
        sortReferences.put("montant_total", "montant_total");
        sortReferences.put("statut_paiement", "statut_paiement");
    }

    public FactureController(PaiementService paiementService,
                             CommandeRepository commandeRepository,
                             MethodePaiementRepository methodePaiementRepository) {
        this.paiementService = paiementService;
        this.commandeRepository = commandeRepository;
        this.methodePaiementRepository = methodePaiementRepository;
    }

    @GetMapping
    public ModelAndView liste(@ModelAttribute FactureCriteriaWrapper cri) {
        if (cri.getLimit() == null) {
            cri.setLimit(10);
        }
        if (cri.getFiltre() == null) {
            cri.setFiltre("Non payée");
        }
        if (cri.getCurrentSort() == null) {
            cri.setCurrentSort("date_commande");
            cri.setCurrentDir("desc");
        }
        Pageable pageable = null;

        if (cri.getCurrentSort() != null && cri.getCurrentDir() != null) {
            if (!cri.getCurrentSort().isEmpty() && !cri.getCurrentDir().isEmpty()) {
                String sort = sortReferences.get(cri.getCurrentSort());
                Sort.Direction direction = cri.getCurrentDir().equalsIgnoreCase("desc") ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
                pageable = PageRequest.of(cri.getPage() - 1, cri.getLimit(), Sort.by(direction, sort));
            }
        }
        if (pageable == null) {
            pageable = PageRequest.of(cri.getPage() - 1, cri.getLimit());
        }

        Slice<FactureDto> pageCommandes = paiementService.listCommandesFiltrees(pageable, cri);
        ModelAndView mav = new ModelAndView("stitch/module_commercial/liste_factures");
        mav.addObject("commandes", pageCommandes.getContent());
        mav.addObject("currentPage", cri.getPage());
        mav.addObject("currentDir", cri.getCurrentDir());
        mav.addObject("currentSort", cri.getCurrentSort());
        mav.addObject("filtre", cri.getFiltre());
        mav.addObject("page", pageCommandes);
        return mav;
    }

    @GetMapping("/new")
    public String formulaireFacture(@RequestParam("commandeId") Integer commandeId, Model model) {
        CommandeModel commande = commandeRepository.findById(commandeId).orElse(null);
        if (commande == null) {
            return "redirect:/factures?error=commande_introuvable";
        }
        BigDecimal montant = paiementService.calculerMontantCommande(commandeId);
        model.addAttribute("commande", commande);
        model.addAttribute("montantCommande", montant);
        model.addAttribute("methodes", methodePaiementRepository.findAll());
        return "stitch/module_commercial/formulaire_facture";
    }

    @PostMapping("/save")
    public ModelAndView saveFacture(@RequestParam("commandeId") Integer commandeId,
                                     @RequestParam(value = "fraisLivraison", required = false) BigDecimal fraisLivraison,
                                     @RequestParam(value = "methodePaiementId", required = false) Integer methodePaiementId) {
        FactureErrorWrapper errors = paiementService.validerFacture(fraisLivraison, methodePaiementId);
        if (errors != null) {
            ModelAndView mav = new ModelAndView("stitch/module_commercial/formulaire_facture");
            mav.addObject("factureError", errors);
            CommandeModel commande = commandeRepository.findById(commandeId).orElse(null);
            mav.addObject("commande", commande);
            if (commande != null) {
                mav.addObject("montantCommande", paiementService.calculerMontantCommande(commandeId));
            }
            mav.addObject("methodes", methodePaiementRepository.findAll());
            return mav;
        }
        Map<String, Object> result = paiementService.creerFacture(commandeId, fraisLivraison, methodePaiementId);
        Integer idFacture = (Integer) result.get("idFacture");
        return new ModelAndView("redirect:/factures/" + idFacture + "?success=created");
    }

    @GetMapping("/{id}")
    public String detailFacture(@PathVariable Integer id, Model model) {
        Map<String, Object> facture = paiementService.getFactureDetail(id);
        model.addAttribute("facture", facture);
        return "stitch/module_commercial/detail_facture";
    }
}
