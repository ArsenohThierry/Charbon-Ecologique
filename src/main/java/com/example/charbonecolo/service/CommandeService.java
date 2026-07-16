package com.example.charbonecolo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.web.server.autoconfigure.ServerProperties.Reactive.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.charbonecolo.dto.CommandeDto;
import com.example.charbonecolo.dto.CriteriaWrapper;
import com.example.charbonecolo.dto.DetailErrorWrapper;
import com.example.charbonecolo.dto.EntreeStockDTO;
import com.example.charbonecolo.dto.LivraisonAnnuleeDto;
import com.example.charbonecolo.dto.SessionDetailErrorWrapper;
import com.example.charbonecolo.dto.SortieStockDTO;
import com.example.charbonecolo.exception.InvalidCommandeException;
import com.example.charbonecolo.exception.InvalidDetailException;
import com.example.charbonecolo.exception.StockUnavailableException;
import com.example.charbonecolo.model.CommandeModel;
import com.example.charbonecolo.model.CommandeStatutModel;
import com.example.charbonecolo.model.DetailCommandeModel;
import com.example.charbonecolo.model.FactureModel;
import com.example.charbonecolo.model.FournisseurModel;
import com.example.charbonecolo.model.LivraisonModel;
import com.example.charbonecolo.model.LivraisonResteModel;
import com.example.charbonecolo.model.LivraisonStatutModel;
import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.model.LotStatutsModel;
import com.example.charbonecolo.model.MouvementSortieDetailModel;
import com.example.charbonecolo.model.MouvementStockModel;
import com.example.charbonecolo.model.PaiementModel;
import com.example.charbonecolo.model.StatutCommandeModel;
import com.example.charbonecolo.model.StatutLivraisonModel;
import com.example.charbonecolo.model.StatutsLotProductionModel;
import com.example.charbonecolo.model.TypeMatierePremiereModel;
import com.example.charbonecolo.model.TypeMouvementStockModel;
import com.example.charbonecolo.repository.CommandeRepository;
import com.example.charbonecolo.repository.DetailCommandeRepository;
import com.example.charbonecolo.repository.FactureRepository;
import com.example.charbonecolo.repository.LivraisonRepository;
import com.example.charbonecolo.repository.LivraisonResteRepository;
import com.example.charbonecolo.repository.LotProductionRepository;
import com.example.charbonecolo.repository.MouvementSortieDetailRepository;
import com.example.charbonecolo.repository.PaiementRepository;
import com.example.charbonecolo.repository.StatutCommandeRepository;
import com.example.charbonecolo.repository.StatutLivraisonRepository;
import com.example.charbonecolo.repository.StatutsLotProductionRepository;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final StatutCommandeRepository statutCommandeRepository;
    private final DetailCommandeRepository detailCommandeRepository;
    private final ClientService clientService;
    private final MouvementStockService mouvementStockService;
    private final MouvementSortieDetailRepository mouvementSortieDetailRepository;
    private final LotProductionRepository lotProductionRepository;
    private final StatutsLotProductionRepository statutsLotProductionRepository;
    private final LivraisonService livraisonService;
    private final LivraisonResteRepository livraisonResteRepository;
    private final LivraisonRepository livraisonRepository;
    private final FactureRepository factureRepository;
    private final PaiementRepository paiementRepository;
    private final StatutLivraisonRepository statutLivraisonRepository;

    public CommandeService(CommandeRepository commandeRepository, StatutCommandeRepository statutCommandeRepository,
            DetailCommandeRepository detailCommandeRepository, ClientService clientService,
            MouvementStockService mouvementStockService,
            MouvementSortieDetailRepository mouvementSortieDetailRepository,
            LotProductionRepository lotProductionRepository,
            StatutsLotProductionRepository statutsLotProductionRepository, LivraisonService livraisonService,
            LivraisonResteRepository livraisonResteRepository, LivraisonRepository livraisonRepository, FactureRepository factureRepository, PaiementRepository paiementRepository, StatutLivraisonRepository statutLivraisonRepository) {
        this.commandeRepository = commandeRepository;
        this.statutCommandeRepository = statutCommandeRepository;
        this.detailCommandeRepository = detailCommandeRepository;
        this.clientService = clientService;
        this.mouvementStockService = mouvementStockService;
        this.mouvementSortieDetailRepository = mouvementSortieDetailRepository;
        this.lotProductionRepository = lotProductionRepository;
        this.statutsLotProductionRepository = statutsLotProductionRepository;
        this.livraisonService = livraisonService;
        this.livraisonResteRepository = livraisonResteRepository;
        this.livraisonRepository = livraisonRepository;
        this.factureRepository = factureRepository;
        this.paiementRepository = paiementRepository;
        this.statutLivraisonRepository = statutLivraisonRepository;
    }

    public void stockAvailable(DetailCommandeModel detail) throws StockUnavailableException {
        int reste = mouvementStockService.getStockRestantParProduit(detail.getProduit().getId());
        System.out.println("-------------------------");
        System.out.println(reste);
        System.out.println("-------------------------");
        if (detail.getQuantite() > reste) {
            throw new StockUnavailableException();
        }
    }

    public Integer getIdFactureOf(Integer idCommande) {
        PaiementModel paiement = paiementRepository.findByCommandeId(idCommande).orElse(null);
        if(paiement != null) {
            FactureModel facture = factureRepository.findByPaiementId(paiement.getId()).orElse(null);
            if(facture != null) {
                return facture.getId();
            }
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Slice<CommandeDto> listCommandes(Pageable pageable, CriteriaWrapper wrapper) {
        Slice<Object[]> sliceBrut = commandeRepository.findCustomCommandes(pageable, wrapper);

        Slice<CommandeDto> ret = sliceBrut.map(ligne -> new CommandeDto(
                (Integer) ligne[0],
                (String) ligne[1],
                (LocalDateTime) ligne[2],
                (String) ligne[4],
                ((BigDecimal) ligne[5]).doubleValue(),
                (Integer) ligne[6],
                (String) ligne[7]));

        List<CommandeDto> listRet = ret.getContent();

        List<Integer> commandeIds = listRet.stream().map(CommandeDto::getId).toList();

        if (!commandeIds.isEmpty()) {
            List<DetailCommandeModel> details = detailCommandeRepository.findAllByCommandeIdIn(commandeIds);

            Map<Integer, List<DetailCommandeModel>> detailsParCommande = details.stream()
                    .collect(Collectors.groupingBy(d -> d.getCommande().getId()));

            listRet.forEach(dto -> {
                List<DetailCommandeModel> listDetails = detailsParCommande.getOrDefault(dto.getId(), List.of());
                dto.setDetails(listDetails);
            });
        }

        return ret;
    }

    public void checkDetailEntry(DetailCommandeModel detail) throws InvalidDetailException {
        DetailErrorWrapper wrapper = null;
        if (detail.getProduit() == null || detail.getProduit().getId() == null) {
            wrapper = new DetailErrorWrapper();
            wrapper.setProduitError("Le champ produit est requis");
        }
        if (detail.getQuantite() == null) {
            if (wrapper == null) {
                wrapper = new DetailErrorWrapper();
            }
            wrapper.setQuantiteError("Le champ quantite est requis");
        }
        if (wrapper != null) {
            InvalidDetailException ex = new InvalidDetailException();
            ex.setFieldErrors(wrapper);
            throw ex;
        }
    }

    public boolean isSavable(Map<Integer, SessionDetailErrorWrapper> wrapper) {
        if (wrapper == null) {
            return true;
        }
        for (Map.Entry<Integer, SessionDetailErrorWrapper> entry : wrapper.entrySet()) {
            if (entry.getValue().getLevel().equals("DANGER")) {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public void cancelCommande(Integer idCommande) {
        StatutCommandeModel statutCommandeModel = new StatutCommandeModel();
        CommandeModel commandeModel = commandeRepository.findById(idCommande).orElse(null);
        statutCommandeModel.setCommande(commandeModel);
        List<DetailCommandeModel> details = detailCommandeRepository.findByCommandeId(idCommande);
        CommandeStatutModel commandeStatutModel = new CommandeStatutModel();
        StatutCommandeModel lastStatus = statutCommandeRepository.getLastStatutOf(idCommande);
        if (lastStatus.getStatut().getId() != 1) {
            List<MouvementSortieDetailModel> sortieDetails = mouvementSortieDetailRepository
                    .findByMouvementSortie(commandeModel.getMouvementSortie());
            for (DetailCommandeModel detail : details) {
                LotProductionModel modified = new LotProductionModel();
                modified.setDateEntreeLot(LocalDateTime.now());
                modified.setDateFinReelle(LocalDateTime.now());
                modified.setProduit(detail.getProduit());
                modified.setQuantiteMatiereUtilisee(new BigDecimal(0));
                modified.setQuantiteProduitPrevues(0);
                modified.setQuantiteProduitReelle(0);
                modified.setRemarques("Depuis commande annulee");
                modified.setReference(genererReferenceLot() + "-ANN");
                TypeMatierePremiereModel typeMatierePremiereModel = new TypeMatierePremiereModel();
                typeMatierePremiereModel.setId(999);
                typeMatierePremiereModel.setFournisseur(new FournisseurModel());
                typeMatierePremiereModel.getFournisseur().setId(999);
                modified.setTypeMatierePremiere(typeMatierePremiereModel);
                LotProductionModel lotSaved = lotProductionRepository.save(modified);
                StatutsLotProductionModel statutsLotProductionModel = new StatutsLotProductionModel();
                LotStatutsModel lotStatutsModel = new LotStatutsModel();
                lotStatutsModel.setId(2);
                statutsLotProductionModel.setLotStatuts(lotStatutsModel);
                statutsLotProductionModel.setLotProduction(lotSaved);
                statutsLotProductionModel.setDateStatut(LocalDateTime.now());
                statutsLotProductionRepository.save(statutsLotProductionModel);
                EntreeStockDTO entreeStockDTO = new EntreeStockDTO();
                entreeStockDTO.setDateEntree(LocalDate.now());
                entreeStockDTO.setIdLot(lotSaved.getId());
                entreeStockDTO.setQuantite(detail.getQuantite());
                mouvementStockService.saveEntreeStock(entreeStockDTO);
            }
        } else if(lastStatus.getStatut().getId() == 3) {
            LivraisonModel livraison = livraisonRepository.findByCommandeId(idCommande).get();
            StatutLivraisonModel statutLivraisonModel = new StatutLivraisonModel();
            LivraisonStatutModel livraisonStatutModel = new LivraisonStatutModel();
            livraisonStatutModel.setId(3);
            statutLivraisonModel.setDateStatutsLivraison(LocalDateTime.now());
            statutLivraisonModel.setLivraison(livraison);
            statutLivraisonModel.setStatut(livraisonStatutModel);
            statutLivraisonRepository.save(statutLivraisonModel);
        }
        commandeStatutModel.setId(5);
        statutCommandeModel.setStatut(commandeStatutModel);
        statutCommandeModel.setDateStatutCommande(LocalDateTime.now());
        statutCommandeRepository.save(statutCommandeModel);
    }

    private boolean livraisonsAnnuleesCovering(DetailCommandeModel detail) {
        List<LivraisonAnnuleeDto> canceled = livraisonService.getLivraisonsAnnulees();
        Integer quantityInStock = mouvementStockService.getStockRestantParProduit(detail.getProduit().getId());
        Integer quantity = quantityInStock;
        for (LivraisonAnnuleeDto livraisonAnnulee : canceled) {
            LivraisonResteModel model = livraisonResteRepository
                    .findByProduitIdAndLivraisonId(detail.getProduit().getId(), livraisonAnnulee.getIdLivraison())
                    .orElse(null);
            if (model != null) {
                quantity += model.getReste();
            }
        }
        return quantity >= detail.getQuantite();
    }

    public boolean canPassToEnAttente(DetailCommandeModel detail) {
        if (enAttenteExisting()) {
            return false;
        }
        if (!livraisonsAnnuleesCovering(detail)) {
            return false;
        }
        return true;
    }

    public boolean enAttenteExisting() {
        return statutCommandeRepository.existsAnyCommandeWithCurrentEnAttente();
    }

    @Transactional
    public void saveEnAttente(CommandeModel commande, List<DetailCommandeModel> details) {
        if (commande.getClient() != null && commande.getClient().getId() != null) {
            commande.setClient(clientService.findById(commande.getClient().getId()));
        }
        CommandeModel saved = commandeRepository.save(commande);
        StatutCommandeModel statut = new StatutCommandeModel();
        statut.setCommande(saved);
        CommandeStatutModel commandeStatutModel = new CommandeStatutModel();
        commandeStatutModel.setId(1);
        statut.setStatut(commandeStatutModel);
        statutCommandeRepository.save(statut);
        details.forEach(d -> d.setCommande(saved));
        details.forEach(d -> d.setMontant(BigDecimal.valueOf(d.getQuantite() * d.getProduit().getPu().doubleValue())));
        saveAllDetailsEnAttente(details,
                commande.getDateCommande() != null ? commande.getDateCommande().toLocalDate() : LocalDate.now());
    }

    @Transactional
    public void convertEnAttenteToCommande(CommandeModel commande) {
        CommandeStatutModel commandeStatutModel = new CommandeStatutModel();
        commandeStatutModel.setId(2);
        StatutCommandeModel statutCommandeModel = new StatutCommandeModel();
        statutCommandeModel.setDateStatutCommande(LocalDateTime.now());
        statutCommandeModel.setCommande(commande);
        statutCommandeModel.setStatut(commandeStatutModel);
        statutCommandeRepository.save(statutCommandeModel);
        List<DetailCommandeModel> details = detailCommandeRepository.findByCommandeId(commande.getId());
        details.forEach(d -> d.setCommande(commande));
        details.forEach(d -> d.setMontant(BigDecimal.valueOf(d.getQuantite() * d.getProduit().getPu().doubleValue())));
        saveAllDetails(details);
        
    }

    public boolean canConvertToCommande(CommandeModel commande) {
        StatutCommandeModel lastStatus = statutCommandeRepository.getLastStatutOf(commande.getId());
        if (lastStatus.getStatut().getId() != 1) {
            return false;
        }
        List<DetailCommandeModel> details = detailCommandeRepository.findByCommandeId(commande.getId());
        for (DetailCommandeModel detail : details) {
            Integer quantityInStock = mouvementStockService.getStockRestantParProduit(detail.getProduit().getId());
            if (quantityInStock < detail.getQuantite()) {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public void save(CommandeModel commande, List<DetailCommandeModel> details) {
        if (commande.getClient() != null && commande.getClient().getId() != null) {
            commande.setClient(clientService.findById(commande.getClient().getId()));
        }
        CommandeModel saved = commandeRepository.save(commande);
        StatutCommandeModel statut = new StatutCommandeModel();
        statut.setCommande(saved);
        CommandeStatutModel commandeStatutModel = new CommandeStatutModel();
        commandeStatutModel.setId(2);
        statut.setStatut(commandeStatutModel);
        statutCommandeRepository.save(statut);
        details.forEach(d -> d.setCommande(saved));
        details.forEach(d -> d.setMontant(BigDecimal.valueOf(d.getQuantite() * d.getProduit().getPu().doubleValue())));
        saveAllDetails(details,
                commande.getDateCommande() != null ? commande.getDateCommande().toLocalDate() : LocalDate.now());
    }

    @Transactional
    public void saveAllDetails(List<DetailCommandeModel> details) {
        saveAllDetails(details, LocalDate.now());
    }

    @Transactional
    public void saveAllDetails(List<DetailCommandeModel> details, LocalDate dateSortie) {
        for (DetailCommandeModel detailCommandeModel : details) {
            SortieStockDTO sortieStockDTO = new SortieStockDTO();
            sortieStockDTO.setDateSortie(dateSortie);
            sortieStockDTO.setIdMotif(1);
            sortieStockDTO.setIdProduit(detailCommandeModel.getProduit().getId());
            sortieStockDTO.setQuantite(detailCommandeModel.getQuantite());
            mouvementStockService.saveSortieStock(sortieStockDTO);
        }
        detailCommandeRepository.saveAll(details);
    }

    @Transactional
    public void saveAllDetailsEnAttente(List<DetailCommandeModel> details, LocalDate dateSortie) {
        // for (DetailCommandeModel detailCommandeModel : details) {
        // SortieStockDTO sortieStockDTO = new SortieStockDTO();
        // sortieStockDTO.setDateSortie(dateSortie);
        // sortieStockDTO.setIdMotif(1);
        // sortieStockDTO.setIdProduit(detailCommandeModel.getProduit().getId());
        // sortieStockDTO.setQuantite(detailCommandeModel.getQuantite());
        // mouvementStockService.saveSortieStock(sortieStockDTO);
        // }
        detailCommandeRepository.saveAll(details);
    }

    @Transactional
    public void saveDetail(DetailCommandeModel detail) throws StockUnavailableException {
        stockAvailable(detail);
        SortieStockDTO sortieStockDTO = new SortieStockDTO();
        sortieStockDTO.setDateSortie(LocalDate.now());
        sortieStockDTO.setIdMotif(1);
        sortieStockDTO.setIdProduit(detail.getProduit().getId());
        sortieStockDTO.setQuantite(detail.getQuantite());
        mouvementStockService.saveSortieStock(sortieStockDTO);
        detailCommandeRepository.save(detail);
    }

    private String genererReferenceLot() {
        // Compte le nombre de lots existants et génère le suivant
        long count = lotProductionRepository.count();
        return String.format("LOT-%03d", count + 1);
    }

    @Transactional
    public void update(DetailCommandeModel detail) throws StockUnavailableException, InvalidDetailException {
        DetailCommandeModel oldDetail = detailCommandeRepository.findById(detail.getId()).orElse(null);
        Integer quantity = Math.abs(oldDetail.getQuantite() - detail.getQuantite());
        DetailCommandeModel temp = new DetailCommandeModel();
        temp.setQuantite(quantity);
        temp.setProduit(oldDetail.getProduit());
        if (oldDetail != null) {
            if (detail.getQuantite() > oldDetail.getQuantite()) {
                if (statutCommandeRepository.getLastStatutOf(oldDetail.getCommande().getId()).getStatut()
                        .getId() == 3) {
                    LivraisonModel livraisonModel = livraisonRepository.findByCommandeId(detail.getCommande().getId())
                            .orElse(null);
                    LivraisonResteModel livraisonResteModel = livraisonResteRepository
                            .findByProduitIdAndLivraisonId(detail.getProduit().getId(), livraisonModel.getId())
                            .orElse(null);
                    if (livraisonResteModel != null) {
                        if (livraisonResteModel.getReste() < quantity) {
                            InvalidDetailException ex = new InvalidDetailException();
                            DetailErrorWrapper wrapper = new DetailErrorWrapper();
                            wrapper.setQuantiteError("Stock en livraison insuffisant.");
                            ex.setFieldErrors(wrapper);
                            throw ex;
                        }
                        livraisonResteModel.setReste(livraisonResteModel.getReste() - quantity);
                        livraisonResteRepository.save(livraisonResteModel);
                    }
                }
                stockAvailable(temp);
                SortieStockDTO sortieStockDTO = new SortieStockDTO();
                sortieStockDTO.setDateSortie(LocalDate.now());
                sortieStockDTO.setIdMotif(1);
                sortieStockDTO.setIdProduit(detail.getProduit().getId());
                sortieStockDTO.setQuantite(quantity);
                mouvementStockService.saveSortieStock(sortieStockDTO);
            } else if (detail.getQuantite() < oldDetail.getQuantite()) {
                if (statutCommandeRepository.getLastStatutOf(oldDetail.getCommande().getId()).getStatut()
                        .getId() == 3) {
                    LivraisonModel livraisonModel = livraisonRepository.findByCommandeId(detail.getCommande().getId())
                            .orElse(null);
                    LivraisonResteModel livraisonResteModel = livraisonResteRepository
                            .findByProduitIdAndLivraisonId(detail.getProduit().getId(), livraisonModel.getId())
                            .orElse(null);
                    if (livraisonResteModel != null) {
                        livraisonResteModel.setReste(livraisonResteModel.getReste() + quantity);
                        livraisonResteRepository.save(livraisonResteModel);
                    }
                } else {
                    LotProductionModel modified = new LotProductionModel();
                    modified.setDateEntreeLot(LocalDateTime.now());
                    modified.setDateFinReelle(LocalDateTime.now());
                    modified.setProduit(detail.getProduit());
                    modified.setQuantiteMatiereUtilisee(new BigDecimal(0));
                    modified.setQuantiteProduitPrevues(0);
                    modified.setQuantiteProduitReelle(0);
                    modified.setRemarques("Depuis commande modifiee");
                    modified.setReference(genererReferenceLot() + "-MODIF");
                    TypeMatierePremiereModel typeMatierePremiereModel = new TypeMatierePremiereModel();
                    typeMatierePremiereModel.setId(999);
                    typeMatierePremiereModel.setFournisseur(new FournisseurModel());
                    typeMatierePremiereModel.getFournisseur().setId(999);
                    modified.setTypeMatierePremiere(typeMatierePremiereModel);
                    LotProductionModel lotSaved = lotProductionRepository.save(modified);
                    StatutsLotProductionModel statutsLotProductionModel = new StatutsLotProductionModel();
                    LotStatutsModel lotStatutsModel = new LotStatutsModel();
                    lotStatutsModel.setId(2);
                    statutsLotProductionModel.setLotStatuts(lotStatutsModel);
                    statutsLotProductionModel.setLotProduction(lotSaved);
                    statutsLotProductionModel.setDateStatut(LocalDateTime.now());
                    statutsLotProductionRepository.save(statutsLotProductionModel);
                    EntreeStockDTO entreeStockDTO = new EntreeStockDTO();
                    entreeStockDTO.setDateEntree(LocalDate.now());
                    entreeStockDTO.setIdLot(lotSaved.getId());
                    entreeStockDTO.setQuantite(quantity);
                    mouvementStockService.saveEntreeStock(entreeStockDTO);
                }
            }
        }
        detailCommandeRepository.save(detail);
    }

    public CommandeDto getCommandeInfo(Integer id) {
        CommandeDto ret = new CommandeDto();
        CommandeModel commande = findById(id);
        List<DetailCommandeModel> details = findDetails(id);
        ret.setClientNom(commande.getClient().getNom());
        ret.setDateCommande(commande.getDateCommande());
        ret.setDetails(details);
        ret.setId(id);
        Double amount = details.stream().mapToDouble(e -> e.findMontant()).sum();
        ret.setMontant(amount);
        ret.setReference(commande.getReference());
        StatutCommandeModel lastStatut = statutCommandeRepository.getLastStatutOf(id);
        ret.setIdCommandeStatuts(lastStatut.getStatut().getId());
        ret.setStatutLibelle(lastStatut.getStatut().getLibelle());
        return ret;
    }

    @Transactional
    public CommandeModel findById(Integer id) {
        return commandeRepository.findById(id).orElse(null);
    }

    @Transactional
    public List<DetailCommandeModel> findDetails(Integer id) {
        return detailCommandeRepository.findByCommandeId(id);
    }

    @Transactional
    public void deleteDetail(Integer id) {
        DetailCommandeModel detail = detailCommandeRepository.findById(id).orElse(null);
        LotProductionModel modified = new LotProductionModel();
        modified.setDateEntreeLot(LocalDateTime.now());
        modified.setDateFinReelle(LocalDateTime.now());
        modified.setProduit(detail.getProduit());
        modified.setQuantiteMatiereUtilisee(new BigDecimal(0));
        modified.setQuantiteProduitPrevues(0);
        modified.setQuantiteProduitReelle(0);
        modified.setRemarques("Depuis commande modifiee");
        modified.setReference(genererReferenceLot() + "-MODIF");
        TypeMatierePremiereModel typeMatierePremiereModel = new TypeMatierePremiereModel();
        typeMatierePremiereModel.setId(999);
        typeMatierePremiereModel.setFournisseur(new FournisseurModel());
        typeMatierePremiereModel.getFournisseur().setId(999);
        modified.setTypeMatierePremiere(typeMatierePremiereModel);
        LotProductionModel lotSaved = lotProductionRepository.save(modified);
        StatutsLotProductionModel statutsLotProductionModel = new StatutsLotProductionModel();
        LotStatutsModel lotStatutsModel = new LotStatutsModel();
        lotStatutsModel.setId(2);
        statutsLotProductionModel.setLotStatuts(lotStatutsModel);
        statutsLotProductionModel.setLotProduction(lotSaved);
        statutsLotProductionModel.setDateStatut(LocalDateTime.now());
        statutsLotProductionRepository.save(statutsLotProductionModel);
        EntreeStockDTO entreeStockDTO = new EntreeStockDTO();
        entreeStockDTO.setDateEntree(LocalDate.now());
        entreeStockDTO.setIdLot(lotSaved.getId());
        entreeStockDTO.setQuantite(detail.getQuantite());
        mouvementStockService.saveEntreeStock(entreeStockDTO);
        detailCommandeRepository.deleteById(id);
    }

    public void checkCommandeEntry(CommandeModel commande) throws InvalidCommandeException {
        boolean ok = true;
        Map<String, String> fieldErrors = new HashMap<>();
        if (commande.getClient() == null || commande.getClient().getId() == null) {
            ok = false;
            fieldErrors.put("client", "Client introuvable.");
        }
        if (!ok) {
            InvalidCommandeException ex = new InvalidCommandeException();
            ex.setFieldErrors(fieldErrors);
            throw ex;
        }
    }

    @Transactional
    public void save(CommandeModel commande) {
        commandeRepository.save(commande);
    }
}
