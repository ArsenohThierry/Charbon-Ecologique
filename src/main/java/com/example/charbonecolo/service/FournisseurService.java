package com.example.charbonecolo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.charbonecolo.model.FournisseurModel;
import com.example.charbonecolo.repository.FournisseurRepository;
import com.example.charbonecolo.util.ImportResult;

@Service
public class FournisseurService {

    @Autowired
    private FournisseurRepository fournisseurRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final int MAX_ROWS = 10_000;
    private static final Set<String> REQUIRED_HEADERS = Set.of("nom", "email", "telephone", "adresse", "actif");
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PHONE_REGEX = "^[0-9+\\s-]{7,20}$";

    @Transactional
    public ImportResult importerFournisseursCsv(MultipartFile fichier) throws IOException {
        validerFichier(fichier);

        List<String> erreurs = new ArrayList<>();
        List<String> avertissements = new ArrayList<>();
        List<FournisseurModel> aSauvegarder = new ArrayList<>();

        try (BufferedReader lecteur = new BufferedReader(
                new InputStreamReader(fichier.getInputStream(), StandardCharsets.UTF_8))) {

            String ligne = lecteur.readLine();
            if (ligne == null) {
                throw new IllegalArgumentException("Fichier vide");
            }

            String[] enTetes = ligne.split(",");
            validerEnTetes(enTetes);

            
            int numeroLigne = 1;
            String[] colonnes = null;
            FournisseurModel fournisseurModel;
            while ((ligne = lecteur.readLine()) != null && numeroLigne <= MAX_ROWS + 1) {
                if (ligne.isBlank()) {
                    numeroLigne++;
                    continue;
                }

                colonnes = ligne.split(",", -1);

                if (colonnes.length < enTetes.length) {
                    erreurs.add("Ligne " + numeroLigne + " : colonnes insuffisantes");
                    numeroLigne++;
                    continue;
                }

                try {
                    fournisseurModel = analyserLigne(colonnes);
                    aSauvegarder.add(fournisseurModel);
                } catch (IllegalArgumentException e) {
                    erreurs.add("Ligne " + numeroLigne + " : " + e.getMessage());
                }
                numeroLigne++;
            }
        } 

        if (erreurs.isEmpty() && !aSauvegarder.isEmpty()) {
            fournisseurRepository.saveAll(aSauvegarder);
        }

        return new ImportResult(
            aSauvegarder.size(),
            erreurs.size(),
            avertissements.size(),
            erreurs,
            avertissements
        );
    }

    private void validerFichier(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide.");
        }
        if (fichier.getSize() > MAX_ROWS) {
            throw new IllegalArgumentException("Fichier trop volumineux (max 5 Mo).");
        }
        String nom = fichier.getOriginalFilename();
        if (nom == null || !nom.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Le fichier doit être au format .csv");
        }
    }

    private void validerEnTetes(String[] enTetes) {
        List<String> normalises = Arrays.stream(enTetes)
            .map(String::trim)
            .map(String::toLowerCase)
            .toList();

        for (String requis : REQUIRED_HEADERS) {
            if (!normalises.contains(requis)) {
                throw new IllegalArgumentException("Colonne manquante : '" + requis + "'");
            }
        }
    }

     private FournisseurModel analyserLigne(String[] colonnes) {
        String nom = colonnes[0].trim();
        String email = colonnes[1].trim();
        String telephone = colonnes[2].trim();
        String adresse = colonnes[3].trim();
        String actifTexte = colonnes[4].trim();

        if (nom.isEmpty()) throw new IllegalArgumentException("nom vide");
        if (email.isEmpty()) throw new IllegalArgumentException("email vide");
        if (telephone.isEmpty()) throw new IllegalArgumentException("telephone vide");
        if (adresse.isEmpty()) throw new IllegalArgumentException("adresse vide");
        if (actifTexte.isEmpty()) throw new IllegalArgumentException("actif vide");

        if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("email invalide : '" + email + "'");
        }

        if (!telephone.matches(PHONE_REGEX)) {
            throw new IllegalArgumentException("telephone invalide : '" + telephone + "'");
        }

        boolean actif;
        if ("true".equalsIgnoreCase(actifTexte) || "1".equals(actifTexte)) {
            actif = true;
        } else if ("false".equalsIgnoreCase(actifTexte) || "0".equals(actifTexte) ) {
            actif = false;
        } else {
            throw new IllegalArgumentException("actif invalide : '" + actifTexte + "'");
        }

        FournisseurModel fournisseur = new FournisseurModel();
        fournisseur.setNom(nom);
        fournisseur.setEmail(email);
        fournisseur.setTelephone(telephone);
        fournisseur.setAdresse(adresse);
        fournisseur.setDate_creation(LocalDateTime.now());
        fournisseur.setActif(actif);

        return fournisseur;
    }

    public List<FournisseurModel> getAll() {
        return fournisseurRepository.findAll();
    }

    public Page<FournisseurModel> searchFournisseurs(
            String nom, String email, String telephone,
            String adresse, Boolean actif, Pageable pageable) {
        return fournisseurRepository.findByCriteria(
                nom, email, telephone, adresse, actif, pageable);
    }

    @Transactional
    public void persistFournisseur(FournisseurModel fournisseurModel) {
        fournisseurRepository.save(fournisseurModel);
    }

    public FournisseurModel getById(Integer id) {
        return fournisseurRepository.findById(id).get();
    }

    @Transactional
    public void deleteById(Integer id) {
        fournisseurRepository.deleteById(id);
    }

    public long getNombreFournisseursActifs() {
        return fournisseurRepository.countByActifTrue();
    }
}
