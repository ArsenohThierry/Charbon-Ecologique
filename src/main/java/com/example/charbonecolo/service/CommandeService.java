package com.example.charbonecolo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.charbonecolo.dto.CommandeDto;
import com.example.charbonecolo.dto.DetailErrorWrapper;
import com.example.charbonecolo.exception.InvalidCommandeException;
import com.example.charbonecolo.exception.InvalidDetailException;
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

    public CommandeService(CommandeRepository commandeRepository, StatutCommandeRepository statutCommandeRepository,
            DetailCommandeRepository detailCommandeRepository) {
        this.commandeRepository = commandeRepository;
        this.statutCommandeRepository = statutCommandeRepository;
        this.detailCommandeRepository = detailCommandeRepository;
    }

    @Transactional(readOnly = true)
    public Page<CommandeDto> listCommandes(Pageable pageable, String keyWord) {
        Page<Object[]> pageBrute = commandeRepository.findCustomCommandes(pageable, keyWord);

        Page<CommandeDto> ret = pageBrute.map(ligne -> new CommandeDto(
                (Integer) ligne[0],
                (String) ligne[1],
                (LocalDateTime) ligne[2],
                (String) ligne[4],
                ((BigDecimal) ligne[5]).doubleValue(),
                (Integer) ligne[6],
                (String) ligne[7]));
        List<CommandeDto> listRet = ret.getContent();
        List<Integer> commandeIds = listRet.stream().map(CommandeDto::getId).toList();
        List<DetailCommandeModel> details = detailCommandeRepository.findAllByCommandeIdIn(commandeIds);
        Map<Integer, List<DetailCommandeModel>> detailsParCommande = details.stream()
                .collect(Collectors.groupingBy(d -> d.getCommande().getId()));

        listRet.forEach(dto -> {
            List<DetailCommandeModel> listDetails = detailsParCommande.getOrDefault(dto.getId(), List.of());
            dto.setDetails(listDetails);
        });
        return ret;
    }

    @Transactional
    public void save(CommandeModel commande, List<DetailCommandeModel> details) {
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

    public void checkCommandeEntry(CommandeModel commande) throws InvalidCommandeException {
        boolean ok = true;
        Map<String, String> fieldErrors = new HashMap<>();
        if(commande.getClient() == null || commande.getClient().getId() == null) {
            ok = false;
            fieldErrors.put("client", "Client introuvable.");
        }
        if(!ok) {
            InvalidCommandeException ex = new InvalidCommandeException();
            ex.setFieldErrors(fieldErrors);
            throw ex;
        }
    }

    public void checkDetailEntry(DetailCommandeModel detail) throws InvalidDetailException {
        boolean ok = false;
        DetailErrorWrapper wrapper = null;
        if(detail.getQuantite() <= 0) {
            wrapper = new DetailErrorWrapper();
            wrapper.setQuantiteError("La quantite doit etre superieure a 0");
            ok = false;
        }
        if(detail.getProduit() == null || detail.getProduit().getId() == null) {
            if(wrapper == null) 
                wrapper = new DetailErrorWrapper();
            wrapper.setProduitError("Le champ produit est requis.");
            ok = false;
        }
        if(!ok) {
            InvalidDetailException ex = new InvalidDetailException();
            wrapper.setId(detail.getId());
            ex.setFieldErrors(wrapper);
            throw ex;
        }
    }

    @Transactional
    public void save(CommandeModel commande) {
        commandeRepository.save(commande);
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
        return  detailCommandeRepository.findByCommandeId(id);
    }

    @Transactional 
    public void deleteDetail(Integer id) {
        detailCommandeRepository.deleteById(id);
    }
}
