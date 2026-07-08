package com.example.charbonecolo.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.charbonecolo.dto.LivraisonAnnuleeDto;
import com.example.charbonecolo.dto.LivraisonCriteriaWrapper;
import com.example.charbonecolo.dto.LivraisonDto;
import com.example.charbonecolo.model.CommandeModel;
import com.example.charbonecolo.model.CommandeStatutModel;
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

    public String findCommandeReferenceByLivraisonId(Integer livraisonId) {
        LivraisonModel livraison = livraisonRepository.findById(livraisonId).orElse(null);
        if (livraison != null && livraison.getCommande() != null) {
            return livraison.getCommande().getReference();
        }
        return null;
    }

    public Integer findCommandeIdByLivraisonId(Integer livraisonId) {
        LivraisonModel livraison = livraisonRepository.findById(livraisonId).orElse(null);
        if (livraison != null && livraison.getCommande() != null) {
            return livraison.getCommande().getId();
        }
        return null;
    }

    public List<StatutLivraisonModel> findStatutsByLivraisonId(Integer livraisonId) {
        return statutLivraisonRepository.findByLivraisonIdOrderByDateStatutsLivraisonAsc(livraisonId);
    }

    @Transactional
    public LivraisonModel createLivraison(LivraisonModel livraison, Integer commandeId) {
        livraison.setReference(generateReference());

        CommandeModel commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + commandeId));
        livraison.setCommande(commande);

        LivraisonModel saved = livraisonRepository.save(livraison);

        // Statut commande → "en livraison" (3)
        CommandeStatutModel statutEnLivraison = new CommandeStatutModel();
        statutEnLivraison.setId(3);
        StatutCommandeModel sc = new StatutCommandeModel();
        sc.setCommande(commande);
        sc.setStatut(statutEnLivraison);
        statutCommandeRepository.save(sc);

        // Statut livraison → "En cours" (1)
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
        // Conserver la commande existante
        LivraisonModel existante = livraisonRepository.findById(livraison.getId())
                .orElseThrow(() -> new RuntimeException("Livraison introuvable : " + livraison.getId()));
        livraison.setCommande(existante.getCommande());
        livraison.setReference(existante.getReference());
        return livraisonRepository.save(livraison);
    }

    @Transactional
    public void livrerLivraison(Integer id) {
        LivraisonModel livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable : " + id));

        LivraisonStatutModel statutEnLivraison = new LivraisonStatutModel();
        statutEnLivraison.setId(4); // "En livraison"

        StatutLivraisonModel sl = new StatutLivraisonModel();
        sl.setLivraison(livraison);
        sl.setStatut(statutEnLivraison);
        statutLivraisonRepository.save(sl);
        CommandeStatutModel commandeStatutModel = new CommandeStatutModel();
        commandeStatutModel.setId(3);
        StatutCommandeModel statutCommande = new StatutCommandeModel();
        statutCommande.setStatut(commandeStatutModel);
        statutCommande.setCommande(livraison.getCommande());
        statutCommande.setDateStatutCommande(LocalDateTime.now());
        statutCommandeRepository.save(statutCommande);
    }

    @Transactional
    public void terminerLivraison(Integer id) {
        LivraisonModel livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable : " + id));

        LivraisonStatutModel statutTermine = new LivraisonStatutModel();
        statutTermine.setId(2); // "Terminé"

        StatutLivraisonModel sl = new StatutLivraisonModel();
        sl.setLivraison(livraison);
        sl.setStatut(statutTermine);
        statutLivraisonRepository.save(sl);

        // Statut commande → "livre" (4)
        if (livraison.getCommande() != null) {
            CommandeStatutModel statutLivre = new CommandeStatutModel();
            statutLivre.setId(4);
            StatutCommandeModel sc = new StatutCommandeModel();
            sc.setCommande(livraison.getCommande());
            sc.setStatut(statutLivre);
            statutCommandeRepository.save(sc);
        }
    }

    @Transactional
    public void reporterLivraison(Integer id, LocalDateTime nouvelleDate) {
        LivraisonModel livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable : " + id));
        livraison.setDateLivraison(nouvelleDate);
        livraisonRepository.save(livraison);
    }

    @Transactional
    public void annulerLivraison(Integer id) {
        LivraisonModel livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livraison introuvable : " + id));

        LivraisonStatutModel statutAnnule = new LivraisonStatutModel();
        statutAnnule.setId(3); // "Annulé"

        StatutLivraisonModel sl = new StatutLivraisonModel();
        sl.setLivraison(livraison);
        sl.setStatut(statutAnnule);
        statutLivraisonRepository.save(sl);

        // Remettre la commande en statut "commande" (2) pour qu'elle soit re-sélectionnable
        if (livraison.getCommande() != null) {
            CommandeStatutModel statutCommande = new CommandeStatutModel();
            statutCommande.setId(2);
            StatutCommandeModel sc = new StatutCommandeModel();
            sc.setCommande(livraison.getCommande());
            sc.setStatut(statutCommande);
            statutCommandeRepository.save(sc);
        }
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

    public Slice<LivraisonDto> listLivraisons(Pageable pageable, LivraisonCriteriaWrapper wrapper) {
        Slice<Object[]> sliceBrut = livraisonRepository.findLivraisonsFiltrees(pageable, wrapper);

        return sliceBrut.map(ligne -> new LivraisonDto(
                (Integer) ligne[0],
                (String) ligne[1],
                (LocalDateTime) ligne[2],
                (String) ligne[3],
                (String) ligne[4],
                (String) ligne[5],
                (String) ligne[6],
                (Integer) ligne[7]
        ));
    }

    public List<LivraisonAnnuleeDto> getLivraisonsAnnulees() {
        List<Object[]> resultats = livraisonRepository.findLivraisonsAnnulees();
        List<LivraisonAnnuleeDto> dtos = new ArrayList<>();
        for (Object[] ligne : resultats) {
            dtos.add(new LivraisonAnnuleeDto(
                    (Integer) ligne[0],
                    (String) ligne[1],
                    (Integer) ligne[2],
                    (String) ligne[3]
            ));
        }
        return dtos;
    }

    private String generateReference() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "LIV-" + timestamp;
    }
}