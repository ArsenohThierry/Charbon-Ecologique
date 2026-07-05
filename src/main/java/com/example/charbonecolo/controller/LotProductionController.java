package com.example.charbonecolo.controller;

import com.example.charbonecolo.exception.BusinessException;
import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.model.StatutsLotProductionModel;
import com.example.charbonecolo.service.LotProductionService;
import com.example.charbonecolo.service.LotStatutsService;
import com.example.charbonecolo.service.ProduitService;
import com.example.charbonecolo.service.TypeMatierePremiereService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("stock/lot")
public class LotProductionController {

    private final LotProductionService lotProductionService;
    private final ProduitService produitService;
    private final TypeMatierePremiereService typeMatierePremiereService;
    private final LotStatutsService lotsStatutsService;

    public LotProductionController(LotProductionService lotProductionService,
            ProduitService produitService,
            TypeMatierePremiereService typeMatierePremiereService,
            LotStatutsService lotStatutsService) {
        this.lotProductionService = lotProductionService;
        this.produitService = produitService;
        this.typeMatierePremiereService = typeMatierePremiereService;
        this.lotsStatutsService = lotStatutsService;
    }

    @GetMapping("/liste")
    public String listLots(
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) Integer idProduit,
            @RequestParam(required = false) Integer idTypeMatierePremiere,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Integer idLotStatut,
            @RequestParam(required = false, defaultValue = "date") String tri,
            @RequestParam(required = false, defaultValue = "desc") String direction,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int taille,
            Model model) {
        boolean rechercheActive = reference != null || idProduit != null || idTypeMatierePremiere != null
                || dateDebut != null || dateFin != null || idLotStatut != null;

        List<LotProductionModel> lots = 
                rechercheActive ? lotProductionService.rechercherLots(reference, idProduit, idTypeMatierePremiere, dateDebut, dateFin, idLotStatut)
                : lotProductionService.getAllLotProductions();

        Map<Integer, String> statusMap = lotProductionService.getLatestStatutsForAllLots();
        lotProductionService.trier(lots, tri, direction, statusMap);

        // Pagination
        int totalLots = lots.size();
        int tailleEffective = Math.max(1, taille);
        int totalPages = Math.max(1, (int) Math.ceil(totalLots / (double) tailleEffective));
        int pageEffective = Math.min(Math.max(0, page), totalPages - 1);
        int debutIndex = pageEffective * tailleEffective;
        int finIndex = Math.min(debutIndex + tailleEffective, totalLots);
        List<LotProductionModel> lotsPage = (debutIndex < finIndex)
                ? lots.subList(debutIndex, finIndex)
                : List.of();

        model.addAttribute("lots", lotsPage);
        model.addAttribute("statusMap", statusMap);
        model.addAttribute("lotStatuts", lotsStatutsService.getAllLotsStatuts());
        model.addAttribute("produits", produitService.findAll());
        model.addAttribute("matieres", typeMatierePremiereService.getAll());

        // Reflète les critères saisis pour pré-remplir le formulaire de recherche
        model.addAttribute("critReference", reference);
        model.addAttribute("critIdProduit", idProduit);
        model.addAttribute("critIdTypeMatierePremiere", idTypeMatierePremiere);
        model.addAttribute("critDateDebut", dateDebut);
        model.addAttribute("critDateFin", dateFin);
        model.addAttribute("critIdLotStatut", idLotStatut);

        // Tri + pagination (pour les en-têtes cliquables et les liens de pagination)
        model.addAttribute("critTri", tri);
        model.addAttribute("critDirection", direction);
        model.addAttribute("critTaille", tailleEffective);
        model.addAttribute("page", pageEffective);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalLots", totalLots);

        return "stitch/module_stock/liste_lot";
    }

    @GetMapping("/nouveau")
    public String nouveauLot(Model model) {
        model.addAttribute("lotProduction", new LotProductionModel());
        model.addAttribute("produits", produitService.findAll());
        model.addAttribute("matieres", typeMatierePremiereService.getAll());
        return "stitch/module_stock/nouveau_lot";
    }

    @PostMapping("/nouveau")
    public String creerLot(
            @RequestParam Integer idTypeMatierePremiere,
            @RequestParam Integer idProduit,
            @RequestParam BigDecimal quantiteMatiereUtilisee,
            @RequestParam Integer quantiteProduitPrevues,
            @RequestParam(required = false) String dateEntreeLot,
            @RequestParam(required = false) String remarques,
            Model model) {
        LotProductionModel lot = new LotProductionModel();
        // Résolution manuelle des objets depuis les ids
        lot.setTypeMatierePremiere(
                typeMatierePremiereService.getById(idTypeMatierePremiere));
        lot.setProduit(
                produitService.findById(idProduit));
        lot.setQuantiteMatiereUtilisee(quantiteMatiereUtilisee);
        lot.setQuantiteProduitPrevues(quantiteProduitPrevues);
        lot.setRemarques(remarques);
        if (dateEntreeLot != null && !dateEntreeLot.isBlank()) {
            lot.setDateEntreeLot(LocalDateTime.parse(dateEntreeLot));
        } else {
            lot.setDateEntreeLot(LocalDateTime.now());
        }
        lotProductionService.saveLotProduction(lot);
        return "redirect:/stock/lot/liste";
    }

    @GetMapping("/modifier/{id}")
    public String modifierLot(@PathVariable Integer id, Model model) {
        Optional<LotProductionModel> lot = lotProductionService.getLotProductionById(id);
        if (lot.isEmpty())
            return "redirect:/stock/lot/liste";
        model.addAttribute("lotProduction", lot.get());
        model.addAttribute("produits", produitService.findAll());
        model.addAttribute("matieres", typeMatierePremiereService.getAll());
        return "stitch/module_stock/nouveau_lot";
    }

    @PostMapping("/modifier/{id}")
    public String updateLot(@PathVariable Integer id,
            @RequestParam Integer idTypeMatierePremiere,
            @RequestParam Integer idProduit,
            @RequestParam BigDecimal quantiteMatiereUtilisee,
            @RequestParam Integer quantiteProduitPrevues,
            @RequestParam(required = false) String dateEntreeLot,
            @RequestParam(required = false) String remarques) {
        LotProductionModel lot = lotProductionService.getLotProductionById(id).orElseThrow();
        lot.setTypeMatierePremiere(typeMatierePremiereService.getById(idTypeMatierePremiere));
        lot.setProduit(produitService.findById(idProduit));
        lot.setQuantiteMatiereUtilisee(quantiteMatiereUtilisee);
        lot.setQuantiteProduitPrevues(quantiteProduitPrevues);
        lot.setRemarques(remarques);
        if (dateEntreeLot != null && !dateEntreeLot.isBlank()) {
            lot.setDateEntreeLot(LocalDateTime.parse(dateEntreeLot));
        }
        lotProductionService.updateLotProduction(lot);
        return "redirect:/stock/lot/liste";
    }

    @PostMapping("/supprimer/{id}")
    public String supprimerLot(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            lotProductionService.deleteLotProduction(id);
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("error",
                    "Impossible de supprimer ce lot : il est référencé par des statuts ou mouvements.");
        }
        return "redirect:/stock/lot/liste";
    }

    // ── Suivi de production (liste des statuts + validation de la date de fin) ──
    @GetMapping("/statut/{id}")
    public String suiviProduction(@PathVariable Integer id, Model model) {
        LotProductionModel lot = lotProductionService.getLotProductionById(id)
                .orElseThrow(() -> new BusinessException("Lot de production introuvable."));

        Optional<StatutsLotProductionModel> statutActuel = lotProductionService.getStatutActuel(lot);

        model.addAttribute("lot", lot);
        model.addAttribute("lotStatuts", lotsStatutsService.getAllLotsStatuts());
        model.addAttribute("statutActuelId", statutActuel.map(s -> s.getLotStatuts().getId()).orElse(null));
        model.addAttribute("statutActuelLibelle",
                statutActuel.map(s -> s.getLotStatuts().getLibelle()).orElse("Inconnu"));
        model.addAttribute("statutActuelOrdre", statutActuel.map(s -> s.getLotStatuts().getOrdre()).orElse(null));
        model.addAttribute("dateDebutStatutActuel",
                statutActuel.map(StatutsLotProductionModel::getDateStatut).orElse(null));
        model.addAttribute("historiqueStatuts", lotProductionService.getHistoriqueStatuts(lot));
        return "stitch/module_stock/detail_1_lot";
    }

    @PostMapping("/statut/{id}/valider")
    public String validerStatut(@PathVariable Integer id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            RedirectAttributes ra) {
        try {
            // On ne reçoit qu'une DATE depuis le formulaire : le statut suivant est
            // toujours déterminé côté serveur (voir LotProductionService), jamais par
            // une valeur postée par le client, même si la page HTML est modifiée.
            lotProductionService.validerFinStatutCourant(id, dateFin.atStartOfDay());
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/stock/lot/statut/" + id;
    }
}