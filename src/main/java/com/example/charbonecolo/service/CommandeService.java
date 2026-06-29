package com.example.charbonecolo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.charbonecolo.dto.CommandeDto;
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
    public Page<CommandeDto> listCommandes(Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        return commandeRepository.findCustomCommandes(pageable);
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

    @Transactional
    public void saveAllDetails(List<DetailCommandeModel> details) {
        detailCommandeRepository.saveAll(details);
    }
}
