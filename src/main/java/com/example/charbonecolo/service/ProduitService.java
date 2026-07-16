package com.example.charbonecolo.service;

import com.example.charbonecolo.model.ProduitModel;
import com.example.charbonecolo.repository.ProduitRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ProduitService {

    private final ProduitRepository produitRepository;

    public ProduitService(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    public List<ProduitModel> findAll() {
        return produitRepository.findAll();
    }

    public ProduitModel findById(Integer id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit introuvable : id=" + id));
    }

    public void save(ProduitModel produit) {
        produitRepository.save(produit);
    }

    public void deleteById(Integer id) {
        if (!produitRepository.existsById(id)) {
            throw new EntityNotFoundException("Produit introuvable : id=" + id);
        }
        produitRepository.deleteById(id);
    }

    // ── Recherche (critères optionnels) ────────────────────────────────
    // Recherche par nom (partiel, insensible à la casse) et/ou fourchette de prix unitaire.
    public List<ProduitModel> rechercher(String nom, Double puMin, Double puMax) {
        String nomFiltre = (nom != null && !nom.isBlank()) ? nom.trim() : null;
        return produitRepository.rechercher(nomFiltre, puMin, puMax);
    }

    // ── Tri (colonnes cliquables) : whitelisté, trié en mémoire ─────────
    public void trier(List<ProduitModel> produits, String tri, String direction) {
        Comparator<ProduitModel> comparateur;
        switch (tri == null ? "" : tri) {
            case "id":
                comparateur = Comparator.comparing(ProduitModel::getId, Comparator.nullsFirst(Comparator.naturalOrder()));
                break;
            case "pu":
                comparateur = Comparator.comparing(ProduitModel::getPu, Comparator.nullsFirst(Comparator.naturalOrder()));
                break;
            case "nom":
            default:
                comparateur = Comparator.comparing(p -> p.getNom() == null ? "" : p.getNom(),
                        String.CASE_INSENSITIVE_ORDER);
                break;
        }
        if ("desc".equalsIgnoreCase(direction)) {
            comparateur = comparateur.reversed();
        }
        produits.sort(comparateur);
    }
}