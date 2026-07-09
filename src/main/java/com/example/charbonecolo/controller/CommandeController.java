package com.example.charbonecolo.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.web.server.autoconfigure.ServerProperties.Reactive.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;

import com.example.charbonecolo.dto.CommandeDto;
import com.example.charbonecolo.dto.CommandeInput;
import com.example.charbonecolo.dto.CriteriaWrapper;
import com.example.charbonecolo.dto.DetailErrorWrapper;
import com.example.charbonecolo.dto.SessionDetailErrorWrapper;
import com.example.charbonecolo.exception.InvalidCommandeException;
import com.example.charbonecolo.exception.InvalidDetailException;
import com.example.charbonecolo.exception.StockUnavailableException;
import com.example.charbonecolo.model.ClientModel;
import com.example.charbonecolo.model.CommandeModel;
import com.example.charbonecolo.model.DetailCommandeModel;
import com.example.charbonecolo.model.ProduitModel;
import com.example.charbonecolo.repository.CommandeRepository;
import com.example.charbonecolo.repository.CommandeStatutRepository;
import com.example.charbonecolo.service.ClientService;
import com.example.charbonecolo.service.CommandeService;
import com.example.charbonecolo.service.ProduitService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/cmd")
public class CommandeController {

    private static final Map<String, String> sortReferences;

    private final CommandeRepository commandeRepository;
    private final CommandeService commandeService;
    private final ClientService clientService;
    private final ProduitService produitService;
    private final CommandeStatutRepository commandeStatutRepository;
    static {
        sortReferences = new HashMap<>();
        sortReferences.put("reference", "reference");
        sortReferences.put("client_nom", "cli.nom");
        sortReferences.put("date", "date_commande");
        sortReferences.put("montant", "montant_total");
    }

    public CommandeController(CommandeRepository commandeRepository, CommandeService commandeService,
            ClientService clientService, ProduitService produitService,
            CommandeStatutRepository commandeStatutRepository) {
        this.commandeRepository = commandeRepository;
        this.commandeService = commandeService;
        this.clientService = clientService;
        this.produitService = produitService;
        this.commandeStatutRepository = commandeStatutRepository;
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
        Map<Integer, SessionDetailErrorWrapper> errors = (Map<Integer, SessionDetailErrorWrapper>) session
                .getAttribute("tmp_errors");
        Double montantTotal = 0.0;
        if (details != null) {
            montantTotal = details.parallelStream().mapToDouble(e -> e.findMontant()).sum();
        }
        mav.addObject("produits", produitService.findAll());
        mav.addObject("panier", details);
        mav.addObject("montantTotal", montantTotal);
        mav.addObject("errors", errors);
        mav.addObject("isSavable", commandeService.isSavable(errors));
        return mav;
    }

    @PostMapping("/new/products")
    @SuppressWarnings("unchecked")
    public ModelAndView storeProductInSession(HttpSession session, @ModelAttribute DetailCommandeModel detail) {
        List<DetailCommandeModel> details = (List<DetailCommandeModel>) session.getAttribute("tmp_details");
        Map<Integer, SessionDetailErrorWrapper> errors = (Map<Integer, SessionDetailErrorWrapper>) session
                .getAttribute("tmp_errors");
        ModelAndView mav = new ModelAndView("redirect:/cmd/new/products");
        if (details == null) {
            details = new ArrayList<>();
            session.setAttribute("tmp_details", details);
        }
        if (errors == null) {
            errors = new HashMap<>();
            session.setAttribute("tmp_errors", errors);
        }
        try {
            commandeService.stockAvailable(detail);
        } catch (StockUnavailableException e) {
            if (commandeService.canPassToEnAttente(detail)) {
                session.setAttribute("immediate_status", "En attente");
                SessionDetailErrorWrapper wrapper = new SessionDetailErrorWrapper();
                wrapper.setIndex(details.size());
                wrapper.setMessage("La commande peut etre passee en attente.");
                wrapper.setLevel("WARNING");
                errors.put(wrapper.getIndex(), wrapper);
            } else {
                SessionDetailErrorWrapper wrapper = new SessionDetailErrorWrapper();
                wrapper.setIndex(details.size());
                wrapper.setMessage("Le stock est insuffisant.");
                wrapper.setLevel("DANGER");
                System.out.println("EXCEPTIONNNNNNNNNNNNN");
                System.out.println(wrapper.getIndex());
                System.out.println("EXCEPTIONNNNNNNNNNNNN");
                System.out.println("EXCEPTIONNNNNNNNNNNNN");
                errors.put(wrapper.getIndex(), wrapper);
            }
        } finally {
            ProduitModel found = produitService.findById(detail.getProduit().getId());
            detail.setProduit(found);
            details.add(detail);
        }
        return mav;
    }

    @PostMapping("/new")
    public ModelAndView storeSession(HttpSession session, @ModelAttribute CommandeModel commande,
            @RequestParam(value = "dateCommandeStr", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateCommande) {
        ModelAndView mav = new ModelAndView("redirect:/cmd/new/products");
        if (dateCommande != null) {
            commande.setDateCommande(dateCommande.atStartOfDay());
        } else {
            commande.setDateCommande(LocalDateTime.now());
        }
        session.setAttribute("tmp_cmd", commande);
        return mav;
    }

    @PostMapping("/cancel")
    public ModelAndView cancelCommande(@RequestParam("id") Integer idCommande) {
        ModelAndView mav = new ModelAndView("redirect:/cmd");
        commandeService.cancelCommande(idCommande);
        return mav;
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/session/products/delete/{index}")
    public ModelAndView deleteProductFromSession(HttpSession session, @PathVariable Integer index) {
        List<DetailCommandeModel> details = (List<DetailCommandeModel>) session.getAttribute("tmp_details");
        Map<Integer, SessionDetailErrorWrapper> errors = (Map<Integer, SessionDetailErrorWrapper>) session
                .getAttribute("tmp_errors");
        if (details != null && index >= 0 && index < details.size()) {
            details.remove((int) index);
        }
        if (errors != null) {
            errors.remove(index);
        }
        return new ModelAndView("redirect:/cmd/new/products");
    }

    @GetMapping
    public ModelAndView list(
            HttpSession session,
            @ModelAttribute CriteriaWrapper wrapper) {
        if (wrapper.getLimit() == null) {
            wrapper.setLimit(10);
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
        Slice<CommandeDto> pageService = commandeService.listCommandes(pageable, wrapper);
        ModelAndView mav = new ModelAndView("stitch/module_commercial/liste_commande");
        mav.addObject("commandes", pageService.getContent());
        mav.addObject("currentPage", wrapper.getPage());
        mav.addObject("currentDir", wrapper.getCurrentDir());
        mav.addObject("currentSort", wrapper.getCurrentSort());
        mav.addObject("page", pageService);
        mav.addObject("statuts", commandeStatutRepository.findAll());
        return mav;
    }

    @PostMapping("/save")
    @SuppressWarnings("unchecked")
    public ModelAndView saveCommande(HttpSession session) {
        ModelAndView mav = new ModelAndView("redirect:/cmd/new");
        List<DetailCommandeModel> details = (List<DetailCommandeModel>) session.getAttribute("tmp_details");
        CommandeModel commande = (CommandeModel) session.getAttribute("tmp_cmd");
        if (session.getAttribute("immdetiate_status") == null) {
            commandeService.save(commande, details);
        } else {
            commandeService.saveEnAttente(commande, details);
        }
        session.removeAttribute("tmp_details");
        session.removeAttribute("tmp_cmd");
        session.removeAttribute("immediate_status");
        return mav;
    }

    @GetMapping("/update/{id}")
    public ModelAndView updateFormDisplay(@PathVariable("id") Integer id, HttpSession session, Model model) {
        ModelAndView mav = new ModelAndView("stitch/module_commercial/modif_commande");
        CommandeModel commande = commandeService.findById(id);
        List<ProduitModel> produits = produitService.findAll();
        session.setAttribute("tmp_cmd_update", commande);
        session.setAttribute("update_errors", new HashMap<>());
        session.setAttribute("entry_errors", new HashMap<>());
        System.out.println("----------------");
        System.out.println(model.getAttribute("clientDto"));
        System.out.println("----------------");
        if (!model.containsAttribute("clientDto")) {
            CommandeInput input = new CommandeInput();
            input.setClientNom(commande.getClient().getNom());
            input.setId(commande.getClient().getId());
            mav.addObject("clientDto", input);

        } else {
            mav.addObject("org.springframework.validation.BindingResult.clientDto",
                    model.getAttribute("org.springframework.validation.BindingResult.clientDto"));
            mav.addObject("clientDto", model.getAttribute("clientDto"));
        }
        List<DetailCommandeModel> details = commandeService.findDetails(id);
        Double montant = details.stream().mapToDouble(e -> e.findMontant()).sum();
        mav.addObject("details", details);
        mav.addObject("commande", commande);
        mav.addObject("total", montant);
        mav.addObject("produits", produits);
        return mav;
    }

    @PostMapping("/detail/update/add")
    public ModelAndView addDetailUpdateMode(HttpSession session, @ModelAttribute DetailCommandeModel detail,
            RedirectAttributes ra) {
        CommandeModel tmp = (CommandeModel) session.getAttribute("tmp_cmd_update");
        detail.setCommande(tmp);
        detail.setMontant(new BigDecimal(0));
        try {
            commandeService.checkDetailEntry(detail);
            commandeService.saveDetail(detail);
        } catch (InvalidDetailException ex) {
            ra.addFlashAttribute("entryError", ex.getFieldErrors());
        } catch (StockUnavailableException ex) {
            SessionDetailErrorWrapper wrapper = new SessionDetailErrorWrapper();
            wrapper.setIndex(detail.getId());
            wrapper.setMessage("Stock insuffisant");
            wrapper.setLevel("DANGER");
            ra.addFlashAttribute("saveError", wrapper);
        }
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
    public ModelAndView updateDetail(HttpSession session, @ModelAttribute DetailCommandeModel detail,
            RedirectAttributes ra) {
        CommandeModel tmp = (CommandeModel) session.getAttribute("tmp_cmd_update");
        detail.setCommande(tmp);
        detail.setMontant(new BigDecimal(0));

        Map<Integer, SessionDetailErrorWrapper> errors = (Map<Integer, SessionDetailErrorWrapper>) session
                .getAttribute("update_errors");
        Map<Integer, DetailErrorWrapper> entryErrors = (Map<Integer, DetailErrorWrapper>) session
                .getAttribute("entry_errors");

        try {
            commandeService.checkDetailEntry(detail);
            commandeService.update(detail);
        } catch (InvalidDetailException ex) {
            entryErrors.put(detail.getId(), ex.getFieldErrors());
            ra.addFlashAttribute("entryErrors", entryErrors);
        } catch (StockUnavailableException ex) {
            SessionDetailErrorWrapper wrapper = new SessionDetailErrorWrapper();
            wrapper.setIndex(detail.getId());
            wrapper.setMessage("Stock insuffisant");
            wrapper.setLevel("DANGER");
            errors.put(detail.getId(), wrapper);
            ra.addFlashAttribute("errors", errors);
        }
        ModelAndView mav = new ModelAndView("redirect:/cmd/update/" + tmp.getId());
        return mav;
    }

    @PostMapping("update")
    public ModelAndView update(
            @ModelAttribute CommandeModel commande,
            HttpSession session,
            RedirectAttributes ra,
            @Valid @ModelAttribute("clientDto") CommandeInput input, BindingResult inputResult) {
        CommandeModel tmp = (CommandeModel) session.getAttribute("tmp_cmd_update");
        ModelAndView mav = new ModelAndView("redirect:/cmd/update/" + tmp.getId());
        if (inputResult.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.clientDto", inputResult);
            ra.addFlashAttribute("clientDto", input);
            return mav;
        }
        try {
            commandeService.checkCommandeEntry(commande);
        } catch (InvalidCommandeException e) {
            ra.addFlashAttribute("clientError", e.getFieldErrors());
            ra.addFlashAttribute("clientDto", input);
            return mav;
        }
        commande.setId(tmp.getId());
        commande.setDeletedAt(tmp.getDeletedAt());
        commande.setReference(tmp.getReference());
        commande.setDateCommande(tmp.getDateCommande());
        commandeService.save(commande);
        return mav;
    }

    @ResponseBody
    @GetMapping("/api/cli")
    public List<ClientModel> listClientsJson(
            @RequestParam(name = "kw", required = false, defaultValue = "") String keyWord) {
        return clientService.findByName(keyWord);
    }

    @GetMapping("/{id}")
    public ModelAndView displayCommandeInfo(@PathVariable("id") Integer id) {
        ModelAndView mav = new ModelAndView("stitch/module_commercial/fiche_commande");
        CommandeDto info = commandeService.getCommandeInfo(id);
        mav.addObject("commande", info);
        return mav;
    }
}
