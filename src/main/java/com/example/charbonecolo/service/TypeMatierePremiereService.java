package com.example.charbonecolo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.charbonecolo.model.TypeMatierePremiereModel;
import com.example.charbonecolo.dto.ImportDataErrorWrapper;
import com.example.charbonecolo.dto.ImportResult;
import com.example.charbonecolo.model.FournisseurModel;
import com.example.charbonecolo.repository.TypeMatierePremiereRepository;

@Service
public class TypeMatierePremiereService {

    @Autowired
    private TypeMatierePremiereRepository typeMatierePremiereRepository;

    @Autowired
    private FournisseurService fournisseurService;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final int MAX_ROWS = 10_000;
    private static final Set<String> REQUIRED_HEADERS = Set.of("libelle", "prix_unitaire", "fournisseur", "actif");

    @Transactional
    public ImportResult importerTypeMatieresCsv(MultipartFile fichier) throws IOException {
        validerFichier(fichier);

        List<ImportDataErrorWrapper> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<TypeMatierePremiereModel> keep = new ArrayList<>();
        String[] columns;
        FournisseurModel fournisseur = null;
        ImportDataErrorWrapper ligneErr = null;
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

            columns = ligne.split(",");
            Map<String, Integer> indexColumns = new HashMap<>();
            Map<Integer, FournisseurModel> keepFournisseur = new HashMap<>();
            getHeadersIndex(columns, indexColumns);
            BigDecimal prix = null;
            TypeMatierePremiereModel matiere = null;

            String libelle = null;
            String prixTexte = null;
            String fournisseurTexte = null;
            String actifTexte = null;

            int numeroLigne = 2;

            while ((ligne = reader.readLine()) != null && numeroLigne <= MAX_ROWS + 1) {
                if (ligne.isBlank()) {
                    numeroLigne++;
                    continue;
                }

                columns = ligne.split(",", -1);

                if (columns.length < headerLen) {
                    ligneErr = new ImportDataErrorWrapper();
                    ligneErr.setLigne(numeroLigne);
                    ligneErr.addError("structure", "Colonnes insuffisantes");
                    errors.add(ligneErr);
                    numeroLigne++;
                    continue;
                }

                libelle = columns[indexColumns.get("libelle")].trim();
                prixTexte = columns[indexColumns.get("prix_unitaire")].trim();
                fournisseurTexte = columns[indexColumns.get("fournisseur")].trim();
                actifTexte = columns[indexColumns.get("actif")].trim();

                ligneErr = new ImportDataErrorWrapper();
                ligneErr.setLigne(numeroLigne);

                // --- libelle ---
                if (libelle.isEmpty()) {
                    ligneErr.addError("libelle", "Le libellé est vide");
                } else if (libelle.length() > 150) {
                    ligneErr.addError("libelle", "Le libellé dépasse 150 caractères");
                }

                if (prixTexte.isEmpty()) {
                    ligneErr.addError("prix_unitaire", "Le prix est vide");
                } else {
                    try {
                        prix = new BigDecimal(prixTexte);
                        if (prix.compareTo(BigDecimal.ZERO) <= 0) {
                            ligneErr.addError("prix_unitaire", "Le prix doit être supérieur à 0");
                        } else if (prix.scale() > 2) {
                            ligneErr.addError("prix_unitaire", "Le prix ne peut avoir plus de 2 décimales");
                        }
                    } catch (NumberFormatException e) {
                        ligneErr.addError("prix_unitaire", "Format numérique invalide : '" + prixTexte + "'");
                    }
                }

                BigDecimal rendement = BigDecimal.ONE;
                if (indexColumns.containsKey("rendement")) {
                    String rendementTexte = columns[indexColumns.get("rendement")].trim();
                    if (!rendementTexte.isEmpty()) {
                        try {
                            rendement = new BigDecimal(rendementTexte);
                            if (rendement.compareTo(BigDecimal.ZERO) <= 0) {
                                ligneErr.addError("rendement", "Le rendement doit être supérieur à 0");
                            } else if (rendement.scale() > 2) {
                                ligneErr.addError("rendement", "Le rendement ne peut avoir plus de 2 décimales");
                            }
                        } catch (NumberFormatException e) {
                            ligneErr.addError("rendement", "Format numérique invalide : '" + rendementTexte + "'");
                        }
                    }
                }

                fournisseur = null;
                if (fournisseurTexte.isEmpty()) {
                    ligneErr.addError("fournisseur", "Le fournisseur est vide");
                } else {
                    try {
                        Integer idx = Integer.parseInt(fournisseurTexte);
                        if (keepFournisseur.containsKey(idx)) {
                            fournisseur = keepFournisseur.get(idx);
                        } else {
                            Optional<FournisseurModel> optFournisseur = fournisseurService.getById(idx);
                            if (optFournisseur.isEmpty()) {
                                ligneErr.addError("fournisseur", "Fournisseur inconnu (ID) : '" + fournisseurTexte + "'");
                            } else {
                                fournisseur = optFournisseur.orElseThrow();
                                keepFournisseur.put(idx, fournisseur);
                            }
                        }
                    } catch (NumberFormatException e) {
                        Optional<FournisseurModel> optFournisseur = fournisseurService.getByEmail(fournisseurTexte);
                        if (optFournisseur.isEmpty()) {
                            optFournisseur = fournisseurService.getByNom(fournisseurTexte);
                        }
                        if (optFournisseur.isEmpty()) {
                            FournisseurModel nouveau = new FournisseurModel();
                            if (fournisseurTexte.contains("@")) {
                                nouveau.setEmail(fournisseurTexte);
                                nouveau.setNom(fournisseurTexte.substring(0, fournisseurTexte.indexOf('@')));
                            } else {
                                nouveau.setNom(fournisseurTexte);
                            }
                            nouveau.setActif(true);
                            fournisseur = fournisseurService.save(nouveau);
                            warnings.add("Fournisseur créé automatiquement : '" + fournisseurTexte + "'");
                        } else {
                            fournisseur = optFournisseur.orElseThrow();
                        }
                    }
                }

                if (!ligneErr.isEmpty()) {
                    errors.add(ligneErr);
                    numeroLigne++;
                    continue;
                }

                boolean actif;
                try {
                    actif = parseStatutActif(actifTexte);
                    matiere = new TypeMatierePremiereModel();
                    matiere.setLibelle(libelle);
                    matiere.setPrixUnitaire(prix);
                    matiere.setFournisseur(fournisseur);
                    matiere.setDateAjout(LocalDateTime.now());
                    matiere.setActif(actif);
                    matiere.setRendement(rendement);
                    keep.add(matiere);
                } catch (IllegalArgumentException e) {
                    ligneErr.addError("actif", e.getMessage());
                }

                numeroLigne++;
            }
        }

        if (!errors.isEmpty()) {
            return new ImportResult(0, errors.size(), errors.size(), errors, warnings);
        }

        if (!keep.isEmpty()) {
            typeMatierePremiereRepository.saveAll(keep);
        }

        return new ImportResult(keep.size(), 0, 0, null, warnings);
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

    private boolean parseStatutActif(String actifTexte) {
        if (actifTexte == null || actifTexte.isBlank())
            return true;
        if ("true".equalsIgnoreCase(actifTexte) || "1".equals(actifTexte) || "oui".equalsIgnoreCase(actifTexte)) {
            return true;
        }
        if ("false".equalsIgnoreCase(actifTexte) || "0".equals(actifTexte) || "non".equalsIgnoreCase(actifTexte)) {
            return false;
        }
        throw new IllegalArgumentException("actif invalide : '" + actifTexte + "' (attendu: true/false, 1/0, oui/non)");
    }

    public List<TypeMatierePremiereModel> getAll() {
        return typeMatierePremiereRepository.findAll();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveMatiere(TypeMatierePremiereModel model, Integer idFournisseur) {
        FournisseurModel fournisseur = fournisseurService.getById(idFournisseur).get();
        model.setFournisseur(fournisseur);

        if (model.getId() == null) {
            model.setDateAjout(LocalDateTime.now());
            model.setActif(true);
        }

        typeMatierePremiereRepository.save(model);
    }

    public Page<TypeMatierePremiereModel> searchTypeMatieres(
            String libelle, BigDecimal prixMin, BigDecimal prixMax,
            Integer idFournisseur, LocalDateTime dateDebut, LocalDateTime dateFin,
            Boolean actif, Pageable pageable) {

        return typeMatierePremiereRepository.findByCriteria(
                libelle, libelle == null, libelle != null && libelle.isEmpty(),
                prixMin, prixMin == null,
                prixMax, prixMax == null,
                idFournisseur, idFournisseur == null,
                dateDebut, dateDebut == null,
                dateFin, dateFin == null,
                actif, actif == null,
                pageable);
    }

    public TypeMatierePremiereModel getById(Integer id) {
        return typeMatierePremiereRepository.findById(id).get();
    }

    @Transactional
    public void deleteById(Integer id) {

        typeMatierePremiereRepository.deleteById(id);
    }
}