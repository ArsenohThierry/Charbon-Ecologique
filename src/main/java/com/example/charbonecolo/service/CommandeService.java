package com.example.charbonecolo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<CommandeDto> listCommandes(Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        List<CommandeModel> commandes = commandeRepository.findBy(pageable);

        if (commandes.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> commandeIds = commandes.stream().map(CommandeModel::getId).toList();

        List<DetailCommandeModel> allDetails = detailCommandeRepository.findAllByCommandeIdIn(commandeIds);

        List<StatutCommandeModel> allStatuts = statutCommandeRepository
                .findAllByCommandeIdInOrderByDateStatutCommandeDesc(commandeIds);

        Map<Integer, List<DetailCommandeModel>> detailsMap = allDetails.stream()
                .collect(Collectors.groupingBy(d -> d.getCommande().getId()));
        Map<Integer, StatutCommandeModel> lastStatutMap = allStatuts.stream()
                .collect(Collectors.toMap(
                        s -> s.getCommande().getId(),
                        s -> s,
                        (statut1, statut2) -> statut1 
                ));

        List<CommandeDto> ret = new ArrayList<>();
        for (CommandeModel c : commandes) {
            CommandeDto dto = new CommandeDto();
            dto.setCommande(c);

            dto.setCurrentStatut(lastStatutMap.get(c.getId()));

            List<DetailCommandeModel> details = detailsMap.getOrDefault(c.getId(), Collections.emptyList());
            dto.setDetails(details);

            double sum = details.stream()
                    .mapToDouble(e -> e.getProduit().getPu().doubleValue() * e.getQuantite())
                    .sum();
            dto.setMontant(sum);

            ret.add(dto);
        }

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

    @Transactional
    public void saveAllDetails(List<DetailCommandeModel> details) {
        detailCommandeRepository.saveAll(details);
    }
}
