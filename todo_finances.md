# Intégration module Finances — Todo

## État actuel

Le module finances (`/finance/*`) est **totalement indépendant** : les écritures comptables ne sont saisies que manuellement depuis la page Journal. Les événements des autres modules (création de facture, entrée stock, paiement) n'écrivent rien dans `journal_financier`.

## Objectif

Auto-générer les écritures comptables dans `journal_financier` à partir des événements métier.

---

## 1. Écriture "Vente" à la création d'une facture

**Fichier :** `src/main/java/com/example/charbonecolo/service/PaiementService.java`  
**Méthode :** `creerFacture(commandeId, fraisLivraison, methodePaiementId)`

Injecter `JournalFinancierService`, `TypeJournalRepository`, `OrigineRepository`.

Créer une écriture après `paiementRepository.save(paiement)` :

| Champ | Valeur |
|-------|--------|
| `dateOperation` | `LocalDateTime.now()` |
| `typeJournal` | `typeJournalRepository.findByCode("VTE")` |
| `origine` | `origineRepository.findByCode("COMMANDE")` |
| `debit` | `montantTotal` (montant commande) |
| `credit` | `0` |
| `reference` | `commande.getReference()` |
| `description` | `"Facture n°" + facture.getReference() + " — " + commande.getClient().getNom()` |

---

## 2. Écriture "Frais de livraison" si > 0

Même méthode, juste après l'écriture Vente :

| Champ | Valeur |
|-------|--------|
| `dateOperation` | `LocalDateTime.now()` |
| `typeJournal` | `typeJournalRepository.findByCode("ACH")` |
| `origine` | `origineRepository.findByCode("FRAIS_LIVRAISON")` |
| `debit` | `0` |
| `credit` | `fraisLivraison` |
| `reference` | `commande.getReference()` |
| `description` | `"Frais de livraison — Commande " + commande.getReference()` |

---

## 3. Écriture "Paiement" à la création d'une facture

Même méthode, en fin de `creerFacture()`.

Déterminer le type selon la méthode de paiement :

| Méthode | typeJournal |
|---------|-------------|
| Espèces | `"CSS"` (Caisse) |
| Mobile money, Carte bancaire, Virement | `"BNQ"` (Banque) |

| Champ | Valeur |
|-------|--------|
| `dateOperation` | `LocalDateTime.now()` |
| `typeJournal` | selon méthode (CSS ou BNQ) |
| `origine` | `origineRepository.findByCode("PAIEMENT")` |
| `debit` | `montantTotal` |
| `credit` | `0` |
| `reference` | `paiement.getReference()` |
| `description` | `"Paiement " + methode + " — Commande " + commande.getReference()` |

---

## 4. Écriture "Achat" à l'entrée en stock

### 4a. Ajouter `prixAchat` au formulaire d'entrée stock

**Fichier :** `src/main/java/com/example/charbonecolo/dto/EntreeStockDTO.java`
- Ajouter `private BigDecimal prixAchat;`

**Fichier :** `src/main/resources/templates/stitch/module_stock/entree_stock.html`
- Ajouter un champ input :
```html
<input type="number" name="prixAchat" step="0.01" min="0" />
```

**Fichier :** `modele MouvementStockModel.java` (optionnel)
- Ajouter `@Column(name = "prix_achat") private BigDecimal prixAchat;`
- Ou utiliser `TypeMatierePremiereModel.prixUnitaire` directement (approximatif)

### 4b. Créer l'écriture

**Fichier :** `src/main/java/com/example/charbonecolo/service/MouvementStockService.java`  
**Méthode :** `saveEntreeStock(EntreeStockDTO entry)`

Injecter `JournalFinancierService`, `TypeJournalRepository`, `OrigineRepository`.

Créer l'écriture après le save du mouvement :

| Champ | Valeur |
|-------|--------|
| `dateOperation` | `dateEntree` |
| `typeJournal` | `typeJournalRepository.findByCode("ACH")` |
| `origine` | `origineRepository.findByCode("ACHAT_FOURNISSEUR")` |
| `debit` | `0` |
| `credit` | `quantite × prixAchat` (ou `quantite × typeMatierePremiere.prixUnitaire`) |
| `reference` | `lot.getReference()` |
| `description` | `"Achat " + lot.getTypeMatierePremiere().getLibelle() + " — " + lot.getTypeMatierePremiere().getFournisseur().getNom()` |

Même logique dans `updateEntreeStock()` si le prix a changé.

---

## 5. Vérifications & Risques

### Idempotence
- Pour éviter les doublons en cas de double submit : vérifier si une écriture existe déjà avec la même `reference` + `origine` avant d'en créer une nouvelle.
- Ou ajouter une contrainte d'unicité sur `(reference, id_origine)` dans `journal_financier`.

### Type Vente → Code VTE
- `JournalFinancierRepository.calculerCA()` filtre sur `code IN ('VTE', 'VENTE')`.
- Vérifier que le type "Vente" en base a bien `code = 'VTE'` (c'est le cas dans le seed).

### Effet de bord
- Toutes les écritures sont créées dans la **même transaction** que l'événement parent.
- Si l'écriture journal échoue, l'ensemble (facture + écriture) est rollbacké → cohérent.

---

## Ordre de priorité

1. **PaiementService.creerFacture()** — Vente + Frais livraison + Paiement (le plus impactant : le CA et la trésorerie deviennent automatiques)
2. **MouvementStockService.saveEntreeStock()** — Achat (nécessite l'ajout du champ prix)
3. **MouvementStockService.updateEntreeStock()** — Mise à jour de l'achat si prix modifié
