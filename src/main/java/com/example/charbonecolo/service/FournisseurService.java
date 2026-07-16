package com.example.charbonecolo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.charbonecolo.dto.ImportDataErrorWrapper;
import com.example.charbonecolo.dto.ImportResult;
import com.example.charbonecolo.model.FournisseurModel;
import com.example.charbonecolo.repository.FournisseurRepository;

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

        List<ImportDataErrorWrapper> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<FournisseurModel> keep = new ArrayList<>();

        Set<String> emailsVus = new HashSet<>();
        Set<String> telephonesVus = new HashSet<>();
        boolean actif;
        String nom = null;
        String email = null;
        String telephone = null;
        String adresse = null;
        String actifTexte = null;
        ImportDataErrorWrapper ligneErr = null;
        FournisseurModel fournisseur = null;
        String[] columns = null;
        int headerLen = REQUIRED_HEADERS.size();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(fichier.getInputStream(), StandardCharsets.UTF_8))) {

            String ligne = reader.readLine();
            if (ligne == null || ligne.trim().isEmpty()) {
                ligneErr = new ImportDataErrorWrapper();
                ligneErr.setLigne(0);
                ligneErr.addError("fichier", "Les en-tetes sont manquantes");
                errors.add(ligneErr);
                return new ImportResult(0, errors.size(), warnings.size(),
                        errors, warnings);
            }

            columns = ligne.split(",", -1);
            Map<String, Integer> indexColumns = new HashMap<>();
            getHeadersIndex(columns, indexColumns);

            int numeroLigne = 2; // Ligne 1 = headers

            while ((ligne = reader.readLine()) != null && numeroLigne <= MAX_ROWS + 1) {
                if (ligne.isBlank()) {
                    numeroLigne++;
                    continue;
                }

                columns = ligne.split(",");

                if (columns.length < headerLen) {
                    ligneErr = new ImportDataErrorWrapper();
                    ligneErr.setLigne(numeroLigne);
                    ligneErr.addError("structure", "Colonnes insuffisantes");
                    errors.add(ligneErr);
                    numeroLigne++;
                    continue;
                }

                fournisseur = new FournisseurModel();
                nom = columns[indexColumns.get("nom")].trim();
                email = columns[indexColumns.get("email")].trim();
                telephone = columns[indexColumns.get("telephone")].trim();
                adresse = columns[indexColumns.get("adresse")].trim();
                actifTexte = columns[indexColumns.get("actif")].trim();

                ligneErr = new ImportDataErrorWrapper();
                ligneErr.setLigne(numeroLigne);

                if (nom.isEmpty()) {
                    ligneErr.addError("nom", "Le nom est vide");
                }

                if (email.isEmpty() && telephone.isEmpty()) {
                    ligneErr.addError("contact", "Email et téléphone vides (au moins un requis)");
                } else {
                    if (!email.isEmpty()) {
                        if (!email.matches(EMAIL_REGEX)) {
                            ligneErr.addError("email", "Format email invalide : '" + email + "'");
                        }
                        if (!emailsVus.add(email) || fournisseurRepository.existsByEmail(email)) {
                            ligneErr.addError("email", "Email en doublon dans le fichier : '" + email + "'");
                        }
                    }
                    if (!telephone.isEmpty()) {
                        if (!telephone.matches(PHONE_REGEX)) {
                            ligneErr.addError("telephone", "Format téléphone invalide : '" + telephone + "'");
                        }
                        if (!telephonesVus.add(telephone) || fournisseurRepository.existsByTelephone(telephone)) {
                            ligneErr.addError("telephone",
                                    "Téléphone en doublon dans le fichier : '" + telephone + "'");
                        }
                    }
                }

                try {
                    parseStatutActif(actifTexte);
                } catch (IllegalArgumentException e) {
                    ligneErr.addError("actif", e.getMessage());
                }

                if (!ligneErr.isEmpty()) {
                    errors.add(ligneErr);
                    numeroLigne++;
                    continue;
                }

                actif = parseStatutActif(actifTexte);

                fournisseur.setNom(nom);
                fournisseur.setEmail(email);
                fournisseur.setTelephone(telephone);
                fournisseur.setAdresse(adresse);
                fournisseur.setDate_creation(LocalDateTime.now());
                fournisseur.setActif(actif);
                keep.add(fournisseur);
                numeroLigne++;
            }
        }

        if (!errors.isEmpty()) {
            return new ImportResult(0, errors.size(), warnings.size(),
                    errors, warnings);
        }

        if (keep.size() > 0) {
            fournisseurRepository.saveAll(keep);
        }

        return new ImportResult(
                keep.size(),
                0,
                warnings.size(),
                null,
                warnings);
    }

    private void validerFichier(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide.");
        }
        if (fichier.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Fichier trop volumineux (max 5 Mo).");
        }
        String nom = fichier.getOriginalFilename();
        if (nom == null || !nom.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Le fichier doit être au format .csv");
        }
    }

    private void getHeadersIndex(String[] columns, Map<String, Integer> indexes) {
        for (String s : REQUIRED_HEADERS) {
            boolean found = false;
            for (int i = 0; i < columns.length; i++) {
                if (columns[i] != null && s.equalsIgnoreCase(columns[i].trim())) {
                    indexes.put(s, i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Colonne manquante : " + s);
            }
        }
    }

    private boolean parseStatutActif(String actifTexte) {
        if ("true".equalsIgnoreCase(actifTexte) || "1".equals(actifTexte) || "oui".equalsIgnoreCase(actifTexte)) {
            return true;
        }
        if ("false".equalsIgnoreCase(actifTexte) || "0".equals(actifTexte) || "non".equalsIgnoreCase(actifTexte)) {
            return false;
        }
        throw new IllegalArgumentException("actif invalide : '" + actifTexte + "' (attendu: true/false, 1/0, oui/non)");
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
        if (fournisseurModel.getId() != null) {
            FournisseurModel existing = fournisseurRepository.findById(fournisseurModel.getId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Fournisseur introuvable pour la reference : " + fournisseurModel.getId()));

            existing.setNom(fournisseurModel.getNom());
            existing.setEmail(fournisseurModel.getEmail());
            existing.setTelephone(fournisseurModel.getTelephone());
            existing.setAdresse(fournisseurModel.getAdresse());
        } else {
            fournisseurModel.setDate_creation(LocalDateTime.now());

            Map<String, String> errors = new HashMap<>();

            String phone = fournisseurModel.getTelephone();
            if (!phone.isEmpty() && !phone.matches(PHONE_REGEX)) {
                errors.put("telephone", String.format("Format de telephone %s invalide", phone));
            }

            fournisseurRepository.save(fournisseurModel);
        }
    }

    public Optional<FournisseurModel> getById(Integer id) {
        return fournisseurRepository.findById(id);
    }

    public Optional<FournisseurModel> getByEmail(String email) {
        return fournisseurRepository.findByEmail(email.trim());
    }

    public Optional<FournisseurModel> getByNom(String nom) {
        return fournisseurRepository.findFirstByNomIgnoreCase(nom.trim());
    }

    @Transactional
    public FournisseurModel save(FournisseurModel fournisseur) {
        if (fournisseur.getId() == null) {
            fournisseur.setDate_creation(LocalDateTime.now());
        }
        return fournisseurRepository.save(fournisseur);
    }

    @Transactional
    public void deleteById(Integer id) {
        FournisseurModel f = getById(id).get();
        if (f.getDelete_at() == null) {
            fournisseurRepository.deleteById(id);
        }
    }

    public long getNombreFournisseursActifs() {
        return fournisseurRepository.countByActifTrue();
    }
}
