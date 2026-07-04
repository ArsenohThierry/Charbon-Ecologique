package com.example.charbonecolo.service;

import java.math.BigDecimal;
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
import com.example.charbonecolo.dto.SessionDetailErrorWrapper;
import com.example.charbonecolo.exception.InvalidCommandeException;
import com.example.charbonecolo.exception.StockUnavailableException;
import com.example.charbonecolo.model.CommandeModel;
import com.example.charbonecolo.model.CommandeStatutModel;
import com.example.charbonecolo.model.DetailCommandeModel;
import com.example.charbonecolo.model.StatutCommandeModel;
import com.example.charbonecolo.repository.CommandeRepository;
import com.example.charbonecolo.repository.DetailCommandeRepository;
import com.example.charbonecolo.repository.StatutCommandeRepository;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final StatutCommandeRepository statutCommandeRepository;
    private final DetailCommandeRepository detailCommandeRepository;
    private final ClientService clientService;
    private final MouvementStockService mouvementStockService;

    public CommandeService(CommandeRepository commandeRepository, StatutCommandeRepository statutCommandeRepository,
            DetailCommandeRepository detailCommandeRepository, ClientService clientService, MouvementStockService mouvementStockService) {
        this.commandeRepository = commandeRepository;
        this.statutCommandeRepository = statutCommandeRepository;
        this.detailCommandeRepository = detailCommandeRepository;
        this.clientService = clientService;
        this.mouvementStockService = mouvementStockService;
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
        detailCommandeRepository.saveAll(details);
    }

        @Transactional
    public void saveDetail(DetailCommandeModel detail) {
        detailCommandeRepository.save(detail);
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
