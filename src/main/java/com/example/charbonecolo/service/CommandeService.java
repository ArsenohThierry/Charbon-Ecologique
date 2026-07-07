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
import com.example.charbonecolo.dto.EntreeStockDTO;
import com.example.charbonecolo.dto.SessionDetailErrorWrapper;
import com.example.charbonecolo.dto.SortieStockDTO;
import com.example.charbonecolo.exception.InvalidCommandeException;
import com.example.charbonecolo.exception.StockUnavailableException;
import com.example.charbonecolo.model.CommandeModel;
import com.example.charbonecolo.model.CommandeStatutModel;
import com.example.charbonecolo.model.DetailCommandeModel;
import com.example.charbonecolo.model.LotProductionModel;
import com.example.charbonecolo.model.LotStatutsModel;
import com.example.charbonecolo.model.MouvementSortieDetailModel;
import com.example.charbonecolo.model.MouvementStockModel;
import com.example.charbonecolo.model.StatutCommandeModel;
import com.example.charbonecolo.model.StatutsLotProductionModel;
import com.example.charbonecolo.model.TypeMouvementStockModel;
import com.example.charbonecolo.repository.CommandeRepository;
import com.example.charbonecolo.repository.DetailCommandeRepository;
import com.example.charbonecolo.repository.LotProductionRepository;
import com.example.charbonecolo.repository.MouvementSortieDetailRepository;
import com.example.charbonecolo.repository.StatutCommandeRepository;
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

    public CommandeService(CommandeRepository commandeRepository, StatutCommandeRepository statutCommandeRepository,
            DetailCommandeRepository detailCommandeRepository, ClientService clientService, MouvementStockService mouvementStockService, MouvementSortieDetailRepository mouvementSortieDetailRepository, LotProductionRepository lotProductionRepository, StatutsLotProductionRepository statutsLotProductionRepository) {
        this.commandeRepository = commandeRepository;
        this.statutCommandeRepository = statutCommandeRepository;
        this.detailCommandeRepository = detailCommandeRepository;
        this.clientService = clientService;
        this.mouvementStockService = mouvementStockService;
        this.mouvementSortieDetailRepository = mouvementSortieDetailRepository;
        this.lotProductionRepository = lotProductionRepository;
        this.statutsLotProductionRepository = statutsLotProductionRepository;
    }

    public void stockAvailable(DetailCommandeModel detail) throws StockUnavailableException {
        int reste = mouvementStockService.getStockRestantParProduit(detail.getProduit().getId());
        System.out.println("-------------------------");
        System.out.println(reste);
        System.out.println("-------------------------");
        if(detail.getQuantite() > reste) {
            throw new StockUnavailableException();
        }
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

    public boolean isSavable(Map<Integer, SessionDetailErrorWrapper> wrapper) {
        if(wrapper == null) {
            return true;
        }
        for(Map.Entry<Integer, SessionDetailErrorWrapper> entry : wrapper.entrySet()) {
            if(entry.getValue().getLevel().equals("DANGER")) {
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
        CommandeStatutModel commandeStatutModel = new CommandeStatutModel();
        commandeStatutModel.setId(5);
        statutCommandeModel.setStatut(commandeStatutModel);
        statutCommandeModel.setDateStatutCommande(LocalDateTime.now());
        statutCommandeRepository.save(statutCommandeModel);
        List<MouvementSortieDetailModel> sortieDetails = mouvementSortieDetailRepository.findByMouvementSortie(commandeModel.getMouvementSortie());
        for(MouvementSortieDetailModel detail : sortieDetails) {
            LotProductionModel original = detail.getLotProduction();
            LotProductionModel modified = new LotProductionModel();
            modified.setDateEntreeLot(original.getDateEntreeLot());
            modified.setDateFinReelle(original.getDateFinReelle());
            modified.setProduit(original.getProduit());
            modified.setQuantiteMatiereUtilisee(original.getQuantiteMatiereUtilisee());
            modified.setQuantiteProduitPrevue(original.getQuantiteProduitPrevue());
            modified.setQuantiteProduitReelle(original.getQuantiteProduitReelle());
            modified.setRemarques("Depuis commande annulee");
            modified.setReference("NO_REF");
            modified.setTypeMatierePremiere(original.getTypeMatierePremiere());
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
        saveAllDetails(details);
    }

    @Transactional
    public void saveAllDetails(List<DetailCommandeModel> details) {
        for(DetailCommandeModel detailCommandeModel : details) {
            SortieStockDTO sortieStockDTO = new SortieStockDTO();
            sortieStockDTO.setDateSortie(LocalDate.now());
            sortieStockDTO.setIdMotif(1);
            sortieStockDTO.setIdProduit(detailCommandeModel.getProduit().getId());
            sortieStockDTO.setQuantite(detailCommandeModel.getQuantite());
            mouvementStockService.saveSortieStock(sortieStockDTO);
        }
        detailCommandeRepository.saveAll(details);
    }

        @Transactional
    public void saveDetail(DetailCommandeModel detail) {
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
