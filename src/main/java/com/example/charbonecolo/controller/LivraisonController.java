package com.example.charbonecolo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.example.charbonecolo.dto.LivraisonAnnuleeDto;
import com.example.charbonecolo.dto.LivraisonCriteriaWrapper;
import com.example.charbonecolo.dto.LivraisonDto;
import com.example.charbonecolo.dto.LivraisonErrorWrapper;
import com.example.charbonecolo.model.LivraisonModel;
import com.example.charbonecolo.repository.LivreurRepository;
import com.example.charbonecolo.repository.LivraisonStatutRepository;
import com.example.charbonecolo.service.LivraisonService;
import com.example.charbonecolo.service.LivraisonValidationService;

@Controller
@RequestMapping("/livraisons")
public class LivraisonController {

    private static final Map<String, String> sortReferences;

    private final LivraisonService livraisonService;
    private final LivreurRepository livreurRepository;
    private final LivraisonValidationService validationService;
    private final LivraisonStatutRepository livraisonStatutRepository;

    static {
        sortReferences = new HashMap<>();
        sortReferences.put("reference", "reference");
        sortReferences.put("date_livraison", "date_livraison");
        sortReferences.put("livreur", "lvr.nom");
        sortReferences.put("statut", "statut_libelle");
    }

    public LivraisonController(LivraisonService livraisonService, LivreurRepository livreurRepository,
                                LivraisonValidationService validationService,
                                LivraisonStatutRepository livraisonStatutRepository) {
        this.livraisonService = livraisonService;
        this.livreurRepository = livreurRepository;
        this.validationService = validationService;
        this.livraisonStatutRepository = livraisonStatutRepository;
    }

    @GetMapping
    public ModelAndView liste(@ModelAttribute LivraisonCriteriaWrapper wrapper) {
        if (wrapper.getLimit() == null) {
            wrapper.setLimit(10);
        }
        if (wrapper.getCurrentSort() == null) {
            wrapper.setCurrentSort("date_livraison");
            wrapper.setCurrentDir("desc");
        }
        Pageable pageable = null;

        if (wrapper.getCurrentSort() != null && wrapper.getCurrentDir() != null) {
            if (!wrapper.getCurrentSort().isEmpty() && !wrapper.getCurrentDir().isEmpty()) {
                String sort = sortReferences.get(wrapper.getCurrentSort());
                Sort.Direction direction = wrapper.getCurrentDir().equalsIgnoreCase("desc") ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
                pageable = PageRequest.of(wrapper.getPage() - 1, wrapper.getLimit(), Sort.by(direction, sort));
            }
        }
        if (pageable == null) {
            pageable = PageRequest.of(wrapper.getPage() - 1, wrapper.getLimit());
        }

        Slice<LivraisonDto> pageLivraisons = livraisonService.listLivraisons(pageable, wrapper);
        ModelAndView mav = new ModelAndView("stitch/module_commercial/liste-livraisons");
        mav.addObject("livraisons", pageLivraisons.getContent());
        mav.addObject("currentPage", wrapper.getPage());
        mav.addObject("currentDir", wrapper.getCurrentDir());
        mav.addObject("currentSort", wrapper.getCurrentSort());
        mav.addObject("page", pageLivraisons);
        mav.addObject("statuts", livraisonStatutRepository.findAll());
        return mav;
    }

    @GetMapping("/new")
    public String formulaireAjout(Model model,
                                   @RequestParam(name = "commandeId", required = false) Integer commandeId) {
        model.addAttribute("livraison", new LivraisonModel());
        model.addAttribute("commandesDisponibles", livraisonService.findAvailableCommandes());
        model.addAttribute("livreurs", livreurRepository.findAll());
        if (commandeId != null) {
            model.addAttribute("preselectedCommandeId", commandeId);
        }
        return "stitch/module_commercial/nouvelle_livraison";
    }

    @PostMapping("/save")
    public ModelAndView save(@ModelAttribute LivraisonModel livraison,
                              @RequestParam(name = "commandeId", required = false) Integer commandeId) {
        LivraisonErrorWrapper errors = validationService.valider(livraison, commandeId);
        if (errors != null) {
            ModelAndView mav = new ModelAndView("stitch/module_commercial/nouvelle_livraison");
            mav.addObject("livraisonError", errors);
            mav.addObject("livreurs", livreurRepository.findAll());
            mav.addObject("commandesDisponibles", livraisonService.findAvailableCommandes());
            return mav;
        }
        livraisonService.createLivraison(livraison, commandeId);
        return new ModelAndView("redirect:/livraisons?success=created");
    }

    @GetMapping("/edit/{id}")
    public String formulaireModification(@PathVariable Integer id, Model model) {
        LivraisonModel livraison = livraisonService.findById(id)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable : " + id));
        model.addAttribute("livraison", livraison);
        model.addAttribute("commandeReference", livraisonService.findCommandeReferenceByLivraisonId(id));
        model.addAttribute("statuts", livraisonService.findStatutsByLivraisonId(id));
        model.addAttribute("livreurs", livreurRepository.findAll());
        return "stitch/module_commercial/form-modification-livraison";
    }

    @PostMapping("/update/{id}")
    public ModelAndView update(@PathVariable Integer id, @ModelAttribute LivraisonModel livraison) {
        LivraisonErrorWrapper errors = validationService.validerModification(livraison);
        if (errors != null) {
            ModelAndView mav = new ModelAndView("stitch/module_commercial/form-modification-livraison");
            mav.addObject("livraisonError", errors);
            mav.addObject("livraison", livraisonService.findById(id).orElse(livraison));
            mav.addObject("commandeReference", livraisonService.findCommandeReferenceByLivraisonId(id));
            mav.addObject("statuts", livraisonService.findStatutsByLivraisonId(id));
            mav.addObject("livreurs", livreurRepository.findAll());
            return mav;
        }
        livraison.setId(id);
        livraisonService.updateLivraison(livraison);
        return new ModelAndView("redirect:/livraisons?success=updated");
    }

    @PostMapping("/livrer/{id}")
    public String livrer(@PathVariable Integer id) {
        livraisonService.livrerLivraison(id);
        return "redirect:/livraisons?success=livre";
    }

    @PostMapping("/terminer/{id}")
    public String terminer(@PathVariable Integer id) {
        Integer commandeId = livraisonService.terminerLivraison(id);
        if (commandeId == null) {
            return "redirect:/livraisons?error=pas_de_commande";
        }
        return "redirect:/factures/new?commandeId=" + commandeId;
    }

    @PostMapping("/reporter/{id}")
    public String reporter(@PathVariable Integer id,
                            @RequestParam("dateLivraison") String dateLivraison) {
        livraisonService.reporterLivraison(id, java.time.LocalDateTime.parse(dateLivraison));
        return "redirect:/livraisons?success=reporte";
    }

    @PostMapping("/annuler/{id}")
    public String annuler(@PathVariable Integer id) {
        livraisonService.annulerLivraison(id);
        return "redirect:/livraisons?success=annule";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        livraisonService.deleteById(id);
        return "redirect:/livraisons";
    }

    @GetMapping("/annulees")
    @ResponseBody
    public List<LivraisonAnnuleeDto> getLivraisonsAnnulees() {
        return livraisonService.getLivraisonsAnnulees();
    }

    @PostMapping("/fermer/{id}")
    public ModelAndView close(@PathVariable Integer id) {
        livraisonService.closeLivraison(id);
        ModelAndView mav = new ModelAndView("redirect:/livraisons?success=ferme");
        return mav;
    }
}