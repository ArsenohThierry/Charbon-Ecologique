package com.example.charbonecolo.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.charbonecolo.model.CommandeModel;
import com.example.charbonecolo.model.CommandeStatutModel;
import com.example.charbonecolo.model.LivraisonCommandeModel;
import com.example.charbonecolo.model.LivraisonModel;
import com.example.charbonecolo.model.LivraisonStatutModel;
import com.example.charbonecolo.model.StatutCommandeModel;
import com.example.charbonecolo.model.StatutLivraisonModel;
import com.example.charbonecolo.repository.CommandeRepository;
import com.example.charbonecolo.repository.LivraisonCommandeRepository;
import com.example.charbonecolo.repository.LivraisonRepository;
import com.example.charbonecolo.repository.LivraisonStatutRepository;
import com.example.charbonecolo.repository.StatutCommandeRepository;
import com.example.charbonecolo.repository.StatutLivraisonRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import com.example.charbonecolo.dto.LivraisonCriteriaWrapper;
import com.example.charbonecolo.dto.LivraisonDto;
@Service
public class LivraisonService {

    private final LivraisonRepository livraisonRepository;
    private final LivraisonCommandeRepository livraisonCommandeRepository;
    private final LivraisonStatutRepository livraisonStatutRepository;
    private final StatutLivraisonRepository statutLivraisonRepository;
    private final StatutCommandeRepository statutCommandeRepository;
    private final CommandeRepository commandeRepository;

    public LivraisonService(LivraisonRepository livraisonRepository,
                            LivraisonCommandeRepository livraisonCommandeRepository,
                            LivraisonStatutRepository livraisonStatutRepository,
                            StatutLivraisonRepository statutLivraisonRepository,
                            StatutCommandeRepository statutCommandeRepository,
                            CommandeRepository commandeRepository) {
        this.livraisonRepository = livraisonRepository;
        this.livraisonCommandeRepository = livraisonCommandeRepository;
        this.livraisonStatutRepository = livraisonStatutRepository;
        this.statutLivraisonRepository = statutLivraisonRepository;
        this.statutCommandeRepository = statutCommandeRepository;
        this.commandeRepository = commandeRepository;
    }

    public List<LivraisonModel> findAll() {
        return livraisonRepository.findAll();
    }

    public java.util.Optional<LivraisonModel> findById(Integer id) {
        return livraisonRepository.findById(id);
    }

    public List<LivraisonCommandeModel> findCommandesByLivraisonId(Integer livraisonId) {
        return livraisonCommandeRepository.findByIdLivraison(livraisonId);
    }

    public List<StatutLivraisonModel> findStatutsByLivraisonId(Integer livraisonId) {
        return statutLivraisonRepository.findByLivraisonIdOrderByDateStatutsLivraisonAsc(livraisonId);
    }

    @Transactional
    public LivraisonModel createLivraison(LivraisonModel livraison, List<Integer> commandeIds) {
        livraison.setReference(generateReference());
        LivraisonModel saved = livraisonRepository.save(livraison);

        for (Integer commandeId : commandeIds) {
            LivraisonCommandeModel link = new LivraisonCommandeModel();
            link.setIdLivraison(saved.getId());
            link.setIdCommande(commandeId);
            livraisonCommandeRepository.save(link);

            CommandeModel commande = commandeRepository.findById(commandeId)
                    .orElseThrow(() -> new RuntimeException("Commande introuvable : " + commandeId));

            CommandeStatutModel statutEnLivraison = new CommandeStatutModel();
            statutEnLivraison.setId(3);

            StatutCommandeModel sc = new StatutCommandeModel();
            sc.setCommande(commande);
            sc.setStatut(statutEnLivraison);
            statutCommandeRepository.save(sc);
        }

        LivraisonStatutModel statutEnCours = new LivraisonStatutModel();
        statutEnCours.setId(1);

        StatutLivraisonModel sl = new StatutLivraisonModel();
        sl.setLivraison(saved);
        sl.setStatut(statutEnCours);
        statutLivraisonRepository.save(sl);

        return saved;
    }

    @Transactional
    public LivraisonModel updateLivraison(LivraisonModel livraison) {
        return livraisonRepository.save(livraison);
    }

    @Transactional
    public void deleteById(Integer id) {
        livraisonCommandeRepository.deleteByIdLivraison(id);
        statutLivraisonRepository.findByLivraisonIdOrderByDateStatutsLivraisonAsc(id)
                .forEach(sl -> statutLivraisonRepository.delete(sl));
        livraisonRepository.deleteById(id);
    }

    public List<Object[]> findAvailableCommandes() {
        return commandeRepository.findCommandesDisponibles();
    }

    private String generateReference() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "LIV-" + timestamp;
    }

    public Slice<LivraisonDto> listLivraisons(Pageable pageable, LivraisonCriteriaWrapper wrapper) {
        Slice<Object[]> sliceBrut = livraisonRepository.findLivraisonsFiltrees(pageable, wrapper);

        return sliceBrut.map(ligne -> new LivraisonDto(
                (Integer) ligne[0],
                (String) ligne[1],
                (LocalDateTime) ligne[2],
                (String) ligne[3],
                (String) ligne[4],
                (String) ligne[5],
                ((Long) ligne[6]).intValue()
        ));
    }

}
