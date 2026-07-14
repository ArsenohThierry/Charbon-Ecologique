# Audit Complet — Module Financier

**Projet :** Charbon Écologique  
**Répertoire :** `/home/itu/Documents/S4/MS/Charbon-Ecologique`  
**Date :** 14 juillet 2026  

---

## 1. Résumé Exécutif

Le module financier repose sur un **journal comptable** (`journal_financier`) avec colonnes `debit`/`credit`, alimenté par des événements métier (facturation, stock, salaires) et consultable via 4 pages web + 1 page facturation.

**Points forts :**
- Architecture propre avec factory method (`creerEcriture`) pour la création d'écritures
- Anti-doublon par contrainte unique `(reference, id_origine)` en BDD + vérification service
- Traçabilité inter-modules via `type_source` / `id_source`
- 6 méthodes d'écriture automatique (vente, paiement, achat, frais livraison, sortie stock, salaire)
- 20 tests d'intégration pour le service et la persistance

**Points faibles :**
- Pagination en mémoire dans `TresorerieService` (ne scale pas)
- `FactureDetailModel` utilise `Integer` pour les montants (perte de précision)
- Pas de réversibilité des écritures lors de suppressions
- Code dupliqué (logique de dates dans `BilanController`)
- `@Autowired` field injection dans `MouvementStockService`

---

## 2. Architecture et Composants

### 2.1. Structure des packages

```
com.example.charbonecolo
├── controller/          (5 controllers finance)
│   ├── JournalController.java       /finance/journal
│   ├── BilanController.java         /finance/bilan
│   ├── TresorerieController.java    /finance/tresorerie
│   ├── KpiController.java           /finance/kpi
│   └── FactureController.java       /factures
├── service/             (7 services finance)
│   ├── JournalFinancierService.java
│   ├── PaiementService.java
│   ├── FactureService.java
│   ├── TresorerieService.java
│   ├── ExportFinanceService.java
│   ├── ExportBilanService.java
│   └── EmployeService.java          (finance via salaires)
├── model/               (11 entities finance)
├── repository/          (9 repositories finance)
├── dto/                 (5 DTOs finance)
├── util/
│   └── FactureToPdf.java
└── templates/
    ├── stitch/module_finance/       (4 vues)
    └── stitch/module_commercial/    (3 vues facture)
```

### 2.2. Schéma de la base de données

```sql
type_journal (id, libelle, code)          -- VTE, ACH, BNQ, CSS, OD
origine (id, libelle, code)               -- COMMANDE, PAIEMENT, ACHAT_FOURNISSEUR,
                                           -- FRAIS_LIVRAISON, SORTIE_STOCK, PAIEMENT_SALAIRE,
                                           -- IMPORT_EXCEL, MANUEL
     │                        │
     └──────┐    ┌────────────┘
            ▼    ▼
journal_financier
├── id (BIGSERIAL PK)
├── date_operation (TIMESTAMP NOT NULL)
├── id_type_journal (FK → type_journal NOT NULL)
├── id_origine (FK → origine, nullable)
├── debit (NUMERIC(15,2) DEFAULT 0)
├── credit (NUMERIC(15,2) DEFAULT 0)
├── reference (VARCHAR(50))
├── description (TEXT)
├── type_source (VARCHAR(50))     -- ex: "FACTURE", "MOUVEMENT_STOCK", "SALAIRE_HISTORIQUE"
├── id_source (BIGINT)
└── created_at (TIMESTAMP DEFAULT CURRENT_TIMESTAMP)

-- Contrainte unique anti-doublons :
UNIQUE(reference, id_origine)

-- Vues SQL :
tresorerie          -- solde cumulé par date
chiffre_affaires    -- CA journalier (filtre VTE)
depenses            -- dépenses journalières
solde_global        -- solde global unique
```

### 2.3. Diagramme des dépendances

```
JournalController ─────→ JournalFinancierService ──→ JournalFinancierRepository
                    └──→ TypeJournalRepository              │
                    └──→ OrigineRepository                  │
                    └──→ ExportFinanceService ──→ JournalFinancierService

BilanController ──────→ JournalFinancierService
                    └──→ ExportBilanService ────→ JournalFinancierService

TresorerieController ──→ TresorerieService ──────→ JournalFinancierRepository (direct!)

KpiController ────────→ JournalFinancierService

FactureController ────→ PaiementService ─────────→ JournalFinancierService
                    └──→ FactureService ─────────→ FactureToPdf
                    └──→ CommandeRepository
                    └──→ FactureRepository

MouvementStockService → JournalFinancierService  (via @Autowired)
EmployeService ────────→ JournalFinancierService  (via constructor)
```

---

## 3. Modèle de Données — Entités JPA

### 3.1. JournalFinancierModel (`journal_financier`)

| Champ | Type | Contraintes | Notes |
|-------|------|-------------|-------|
| `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` | |
| `dateOperation` | `LocalDateTime` | `NOT NULL` | Date de l'opération |
| `typeJournal` | `TypeJournalModel` | `@ManyToOne(EAGER) NOT NULL` | VTE/ACH/BNQ/CSS/OD |
| `origine` | `OrigineModel` | `@ManyToOne(EAGER)` nullable | COMMANDE/PAIEMENT/... |
| `debit` | `BigDecimal` | `NOT NULL, precision=15, scale=2` | Montant au débit |
| `credit` | `BigDecimal` | `NOT NULL, precision=15, scale=2` | Montant au crédit |
| `reference` | `String` | `length=500` | Référence de l'opération |
| `description` | `String` | `TEXT` | Description libre |
| `typeSource` | `String` | `length=50` | MODULE source (FACTURE, etc.) |
| `idSource` | `Long` | | ID de l'entité source |
| `createdAt` | `LocalDateTime` | `NOT NULL, updatable=false` | Auto-set via `@PrePersist` |

### 3.2. FactureDetailModel (`facture_detail`)

| Champ | Type | Remarque |
|-------|------|----------|
| `montant` | **`Integer`** | ⚠️ Perte de précision — devrait être `BigDecimal` |
| `pu` | **`Integer`** | ⚠️ Même problème |
| `quantite` | `Integer` | OK |
| `libelle` | `String` | OK |

### 3.3. Tables de lookup

| Table | Champs | Données seed |
|-------|--------|--------------|
| `type_journal` | id, libelle, code | VTE, ACH, BNQ, CSS, OD |
| `origine` | id, libelle, code | COMMANDE, PAIEMENT, ACHAT_FOURNISSEUR, FRAIS_LIVRAISON, SORTIE_STOCK, PAIEMENT_SALAIRE, IMPORT_EXCEL, MANUEL |
| `methode_paiement` | id, libelle | Espèces, Mobile money, Carte bancaire, Virement |

---

## 4. Inventaire des Endpoints

### 4.1. Module Finance

| Méthode | URL | Controller | Fonction |
|---------|-----|------------|----------|
| `GET` | `/finance/journal` | JournalController | Journal avec filtres/pagination |
| `POST` | `/finance/journal` | JournalController | Saisie manuelle d'une écriture |
| `GET` | `/finance/journal/export-csv` | JournalController | Export CSV du journal |
| `GET` | `/finance/journal/export-excel` | JournalController | Export Excel du journal |
| `GET` | `/finance/bilan` | BilanController | Bilan financier (CA, bénéfice, entrées, sorties) |
| `GET` | `/finance/bilan/export-excel` | BilanController | Export bilan Excel |
| `GET` | `/finance/bilan/export-pdf` | BilanController | Export bilan PDF |
| `GET` | `/finance/tresorerie` | TresorerieController | Trésorerie avec solde cumulé |
| `GET` | `/finance/kpi` | KpiController | Tableau de bord KPI |
| `GET` | `/finance/kpi/data?mois=&annee=` | KpiController | API JSON des KPI |

### 4.2. Module Facturation

| Méthode | URL | Controller | Fonction |
|---------|-----|------------|----------|
| `GET` | `/factures` | FactureController | Liste des factures (filtres, tri, pagination) |
| `GET` | `/factures/new?commandeId=N` | FactureController | Formulaire de création de facture |
| `POST` | `/factures/save` | FactureController | Création facture + écritures auto |
| `GET` | `/factures/{id}` | FactureController | Détail d'une facture |
| `GET` | `/factures/export/{id}` | FactureController | Export PDF d'une facture |

---

## 5. Flux d'Écritures Comptables

### 5.1. Ce qui génère des écritures ✅

#### A. Création de facture — `PaiementService.creerFacture()` (ligne 111)

Déclenché par `POST /factures/save`. Crée **3 écritures** dans la même transaction :

| # | Méthode appelée | Type | Origine | Débit | Crédit | Condition |
|---|-----------------|------|---------|-------|--------|-----------|
| 1 | `enregistrerVente()` :194 | VTE | COMMANDE | montantCmd | 0 | Toujours |
| 2 | `enregistrerFraisLivraison()` :204 | ACH | FRAIS_LIVRAISON | 0 | fraisLivraison | Si frais > 0 |
| 3 | `enregistrerPaiement()` :219 | CSS ou BNQ | PAIEMENT | 0 | total | Toujours |

**Note :** Le type de la 3ème écriture est déterminé dynamiquement : `"Espèces" → CSS`, sinon → `BNQ`.

#### B. Entrée de stock — `MouvementStockService.saveEntreeStock()` (ligne 147)

| # | Méthode appelée | Type | Origine | Débit | Crédit | Condition |
|---|-----------------|------|---------|-------|--------|-----------|
| 1 | `enregistrerAchat()` :199 | ACH | ACHAT_FOURNISSEUR | 0 | prixAchat × quantite | Si prixAchat > 0 |

#### C. Mise à jour d'entrée de stock — `MouvementStockService.updateEntreeStock()` (ligne 211)

| # | Méthode appelée | Type | Origine | Débit | Crédit | Condition |
|---|-----------------|------|---------|-------|--------|-----------|
| 1 | `enregistrerAchat()` :244 | ACH | ACHAT_FOURNISSEUR | 0 | prixAchat × quantite | Si prixAchat > 0 |

#### D. Sortie de stock — `MouvementStockService.saveSortieStock()` (ligne 258)

| # | Méthode appelée | Type | Origine | Débit | Crédit | Condition |
|---|-----------------|------|---------|-------|--------|-----------|
| 1 | `enregistrerSortieStock()` :324 | OD | SORTIE_STOCK | 0 | pu × quantite | Toujours |

#### E. Paiement salaire — `EmployeService.salarier()` (ligne 97)

| # | Méthode appelée | Type | Origine | Débit | Crédit | Condition |
|---|-----------------|------|---------|-------|--------|-----------|
| 1 | `enregistrerPaiementSalaire()` :115 | CSS | PAIEMENT_SALAIRE | 0 | total | Toujours |

#### F. Saisie manuelle — `JournalController.POST /finance/journal` (ligne 83)

Permet de créer une écriture libre via formulaire (tout type, toute origine).

### 5.2. Ce qui NE génère PAS d'écritures ❌

| Événement | Service concerné | Impact |
|-----------|-----------------|--------|
| **Mise à jour de sortie de stock** | `MouvementStockService.updateSortieStock()` :335 | Sortie modifiée mais écriture non mise à jour |
| **Suppression de mouvement de stock** | `MouvementStockService.deleteMouvementStock()` :408 | Écriture orpheline — pas de réversibilité |
| **Annulation de commande** | `CommandeService` | Pas de contre-écriture |
| **Avoir / Remboursement** | Non modélisé | Module absent |
| **Écriture d'ajustement inventaire** | Non modélisé | Module absent |

### 5.3. Tableau récapitulatif des écritures

```
Événement                          │ VTE │ ACH │ CSS/BNQ │ OD  │ Total
───────────────────────────────────┼─────┼─────┼─────────┼─────┼──────
Création facture                   │  D  │  C  │    C    │     │  3
Entrée de stock                    │     │  C  │         │     │  1
Sortie de stock                    │     │     │         │  C  │  1
Paiement salaire                   │     │     │    C    │     │  1
Saisie manuelle                    │     │     │         │     │ 0-n
───────────────────────────────────┴─────┴─────┴─────────┴─────┴──────
D = Debit (encaissement)   C = Crédit (décaissement)
```

---

## 6. Analyse des Services

### 6.1. JournalFinancierService (385 lignes)

**Responsabilité :** CRUD du journal + API d'écriture pour les autres modules.

| Méthode | Visibilité | Rôle |
|---------|------------|------|
| `enregistrer()` | public | Sauvegarde universelle avec anti-doublon |
| `creerEcriture()` | **private** | Factory method — construit le modèle |
| `enregistrerVente()` | public | Écriture VTE (débit) |
| `enregistrerPaiement()` | public | Écriture CSS/BNQ (débit) |
| `enregistrerAchat()` | public | Écriture ACH (crédit) |
| `enregistrerFraisLivraison()` | public | Écriture ACH (crédit) |
| `enregistrerSortieStock()` | public | Écriture OD (crédit) |
| `enregistrerPaiementSalaire()` | public | Écriture CSS (crédit) |
| `calculerCA()` | public | SUM(debit) WHERE code VTE |
| `calculerBenefice()` | public | SUM(debit) - SUM(credit) |
| `calculerSolde()` | public | Solde global |
| `filtrerJournal()` | public | Filtre par dates |
| `filtrerParType()` | public | Filtre par type |
| `evolutionMensuelleCA()` | public | CA des 12 derniers mois |
| `verifierDoublon()` | public | Vérifie existence |
| `rechercherParSource()` | public | Traçabilité inter-modules |

### 6.2. PaiementService (264 lignes)

**Responsabilité :** Orchestration facture + paiement + écritures.

- **`creerFacture()`** : Méthode centrale (`@Transactional`). Crée PaiementModel, FactureModel, FactureDetailModel, met à jour le statut commande, puis crée 3 écritures comptables.
- Utilise **`JdbcTemplate`** (SQL brut) mélangé avec les repositories JPA — pattern d'accès aux données incohérent.
- Les références sont basées sur `LocalDateTime.now()` (risque de collision à la même seconde).

### 6.3. TresorerieService (137 lignes)

**Responsabilité :** Historique de trésorerie avec solde cumulé.

- **Contourne `JournalFinancierService`** — injecte directement `JournalFinancierRepository`.
- **`rechercher()`** : Charge **TOUS** les enregistrements en mémoire, filtre en Java, pagination manuelle via `subList`.
- **`construireHistorique()`** : Parcourt tous les journaux pour calculer le solde cumulé.

### 6.4. MouvementStockService (684 lignes)

**Responsabilité :** Gestion des mouvements de stock (entrées/sorties FIFO).

- **`saveEntreeStock()`** : Crée une écriture ACH si prix d'achat > 0.
- **`saveSortieStock()`** : Crée une écriture OD avec le prix de vente unitaire.
- **`updateSortieStock()`** : ❌ Pas d'écriture comptable.
- **`deleteMouvementStock()`** : ❌ Pas de réversibilité.
- Utilise **`@Autowired` field injection** (13 dépendances) au lieu de constructor injection.

### 6.5. EmployeService (135 lignes)

**Responsabilité :** Gestion des employés et des salaires.

- **`salarier()`** : Crée un historique de salaire + écriture CSS (débit salaire).
- ✅ Intégré au journal financier.

---

## 7. Tests

### 7.1. JournalFinancierServiceTest (248 lignes)

| Test | Ce qu'il valide |
|------|-----------------|
| `testEnregistrerVente` | Écriture VTE (debit=500K) |
| `testEnregistrerPaiementCarteBancaire` | Paiement BNQ |
| `testEnregistrerPaiementEspeces` | Paiement CSS |
| `testEnregistrerAchat` | Achat ACH (credit=250K) |
| `testEnregistrerFraisLivraison` | Frais livraison ACH |
| `testDetectionDoublons` | Vérification doublon |
| `testEnregistrerVenteDoublonLanceException` | Exception sur doublon |
| `testCalculerCA` | CA du mois courant |
| `testCalculerSolde` | Solde global |
| `testRechercherParSource` | Recherche par source |
| `testFiltrerParTypeCode` | Filtre par code type |
| `testFiltrerParOrigineCode` | Filtre par code origine |
| `testExisteDeja` | Existence ref+origine |

### 7.2. JournalFinancierPersistenceTest (217 lignes)

| Test | Ce qu'il valide |
|------|-----------------|
| `testCreationEcriture` | CRUD + auto createdAt |
| `testRefusDoublon` | Contrainte unique |
| `testLectureEcritures` | findAll + findByReferenceAndOrigine |
| `testValidationContraintesDebitCredit` | Un seul des deux non nul |
| `testCreatedAtAuto` | Auto-population createdAt |
| `testTypeSourceEtIdSource` | Recherche par source |
| `testRechercheParReference` | Recherche partielle |
| `testRechercheParTypeJournalCode` | Filtre par code |

### 7.3. Couverture manquante

| Composant | Test manquant |
|-----------|---------------|
| `PaiementService.creerFacture()` | ❌ Aucun test du flux principal |
| `TresorerieService` | ❌ Aucun test |
| `ExportFinanceService` | ❌ Aucun test |
| `ExportBilanService` | ❌ Aucun test |
| `FactureService.exportToPdf()` | ❌ Aucun test |
| `EmployeService.salarier()` | ❌ Aucun test de l'écriture salaire |
| `MouvementStockService` (écritures) | ❌ Aucun test de l'intégration journal |

---

## 8. Alea Possibles avec Réponses

### 🟢 Difficulté FACILE

---

#### Alea 1 : `FactureDetailModel` utilise `Integer` pour les montants

**Problème :** `montant` et `pu` sont des `Integer` au lieu de `BigDecimal`. Si un prix unitaire a des décimales (ex: 1500.50 Ar), la valeur est tronquée.

**Fichier :** `model/FactureDetailModel.java` (lignes 23-30)

**Risque :** Erreurs d'arrondi cumulées sur les factures.

**Réponse :** Remplacer `Integer` par `BigDecimal` pour `montant`, `pu`. Adapter le `FactureController.detailFacture()` (ligne 148 : `mapToInt` → `map` + `reduce`).

---

#### Alea 2 : Code mort dans `MouvementStockService`

**Problème :** Les champs `sortieType`, `sorties`, `entreeType`, `entrees`, `ruptures`, `faibles` (lignes 87-92) ne sont jamais utilisés. Bloc commenté lignes 604-612.

**Fichier :** `service/MouvementStockService.java`

**Risque :** Confusion lors de la maintenance.

**Réponse :** Supprimer les champs inutilisés et le code commenté.

---

#### Alea 3 : `FactureService.exportToPdf()` — NPE possible

**Problème :** `factureRepository.findById(id).orElse(null)` (ligne 23) transmet `null` à `FactureToPdf` sans vérification.

**Fichier :** `service/FactureService.java` (ligne 23)

**Risque :** `NullPointerException` si la facture n'existe pas.

**Réponse :** Ajouter un `if (found == null) return null;` ou lancer une `ResourceNotFoundException`.

---

#### Alea 4 : Code commenté dans `KpiController`

**Problème :** Bloc de code commenté (lignes 61-71 dans la version source, code mort).

**Fichier :** `controller/KpiController.java`

**Risque :** Code mort polluant le codebase.

**Réponse :** Supprimer le code commenté.

---

#### Alea 5 : `@PostConstruct initDonnees()` avec IDs hardcodés

**Problème :** `PaiementService.initDonnees()` (ligne 53) utilise `setId(6)` pour le statut "payee" et s'exécute à chaque démarrage.

**Fichier :** `service/PaiementService.java` (lignes 53-69)

**Risque :** Cassé si l'ID 6 est attribué à un autre statut.

**Réponse :** Utiliser une recherche par `libelle` au lieu d'un ID hardcodé, ou migrer vers un `data.sql` géré par Spring Boot.

---

### 🟡 Difficulté MOYENNE

---

#### Alea 6 : `TresorerieService` contourne le service layer

**Problème :** `TresorerieService` injecte `JournalFinancierRepository` directement (ligne 22) au lieu de passer par `JournalFinancierService`. Cela duplique la logique de `calculerSolde()`.

**Fichier :** `service/TresorerieService.java` (ligne 22)

**Risque :** Violation de l'architecture en couches, logique dupliquée.

**Réponse :** Injecter `JournalFinancierService` au lieu du repository. Utiliser `journalService.findAll()` pour l'historique et `journalService.calculerSolde()` pour le solde.

---

#### Alea 7 : Pagination en mémoire dans `TresorerieService`

**Problème :** `rechercher()` (ligne 36) charge **tous** les enregistrements du journal en mémoire, filtre en Java, puis pagin manuellement via `subList`. `calculerTotalEntrees()` et `calculerTotalSorties()` appellent `construireHistorique()` qui charge tout juste pour sommer une colonne.

**Fichier :** `service/TresorerieService.java` (lignes 36-72)

**Risque :** Problème de performance et de mémoire avec beaucoup d'écritures. `OutOfMemoryError` possible.

**Réponse :** Implémenter les filtres et la pagination directement en JPQL/Native SQL dans le repository. Utiliser `SUM(debit)` / `SUM(credit)` en requête SQL pour les totaux.

---

#### Alea 8 : Duplication de code dans `BilanController`

**Problème :** La logique de default/conversion de dates (LocalDate → LocalDateTime) est copiée 3 fois : `afficherBilan()` (lignes 36-40), `exportExcel()` (lignes 57-61), `exportPdf()` (lignes 77-80).

**Fichier :** `controller/BilanController.java`

**Risque :** violation DRY, risque d'incohérence lors des modifications.

**Réponse :** Extraire une méthode privée `resolveDates(debut, fin)` qui retourne un couple `LocalDateTime[]`.

---

#### Alea 9 : `@Autowired` field injection dans `MouvementStockService`

**Problème :** 13 dépendances injectées via `@Autowired` sur les champs (lignes 59-85) au lieu de constructor injection.

**Fichier :** `service/MouvementStockService.java`

**Risque :** Moins testable, dépendances cachées, impossible de les rendre `final`.

**Réponse :** Refactorer en constructor injection. Utiliser Lombok `@RequiredArgsConstructor` si disponible.

---

#### Alea 10 : Risque de collision de références dans `PaiementService`

**Problème :** Les références sont générées via `LocalDateTime.now()` : `"PAI-" + yyyyMMddHHmmss` (ligne 114). Si deux factures sont créées à la même seconde, les références sont identiques.

**Fichier :** `service/PaiementService.java` (lignes 114-115)

**Risque :** Erreur de doublon sur la contrainte unique `(reference, id_origine)`.

**Réponse :** Ajouter un identifiant unique (UUID court, ou compteur séquentiel) aux références, ou utiliser `System.nanoTime()`.

---

#### Alea 11 : Pas de `@Transactional` sur les services d'export

**Problème :** `FactureService`, `ExportFinanceService`, `ExportBilanService` n'ont pas d'annotation `@Transactional`.

**Fichier :** `service/FactureService.java`, `service/ExportFinanceService.java`, `service/ExportBilanService.java`

**Risque :** Les données pourraient changer pendant l'export si d'autres transactions sont en cours.

**Réponse :** Ajouter `@Transactional(readOnly = true)` sur ces services.

---

#### Alea 12 : Méthode de paiement non liée au paiement en JPA

**Problème :** `MethodePaiementModel` existe comme table de lookup mais n'est pas liée à `PaiementModel` via une relation JPA. La méthode de paiement est déterminée par requête SQL brute dans `PaiementService` (ligne 214).

**Fichier :** `model/PaiementModel.java`, `service/PaiementService.java`

**Risque :** La méthode de paiement n'est pas traçable dans le modèle objet.

**Réponse :** Ajouter un `@ManyToOne` vers `MethodePaiementModel` dans `PaiementModel`.

---

### 🔴 Difficulté DIFFICILE

---

#### Alea 13 : Mise à jour de sortie de stock sans écriture comptable

**Problème :** `updateSortieStock()` (ligne 335) modifie les détails de la sortie mais ne met **pas à jour** l'écriture `SORTIE_STOCK` correspondante dans le journal.

**Fichier :** `service/MouvementStockService.java` (ligne 335)

**Risque :** Incohérence entre le stock physique et le journal financier si une sortie est modifiée.

**Réponse :** 
1. Rechercher l'écriture existante via `rechercherParSource("MOUVEMENT_STOCK", mouvementId)`
2. La supprimer et en créer une nouvelle avec les montants actualisés
3. Ou ajouter un champ `montant` sur `MouvementStockModel` pour pouvoir recalculer

---

#### Alea 14 : Suppression de mouvement de stock sans réversibilité

**Problème :** `deleteMouvementStock()` (ligne 408) supprime le mouvement mais ne supprime **pas** l'écriture comptable associée.

**Fichier :** `service/MouvementStockService.java` (ligne 408)

**Risque :** Écritures orphelines dans le journal financier. Le bilan et la trésorerie sont faussés.

**Réponse :**
1. Avant la suppression, rechercher les écritures liées via `rechercherParSource()`
2. Les supprimer ou créer des écritures de réversibilité (contre-écritures)
3. Ajouter une vérification : si des écritures existent, demander confirmation

---

#### Alea 15 : Pas de réversibilité des écritures de facturation

**Problème :** Si une facture est annulée ou supprimée, les 3 écritures comptables (VTE, FRAIS_LIVRAISON, PAIEMENT) restent dans le journal.

**Fichier :** `service/PaiementService.java`

**Risque :** Le CA et la trésorerie incluent des factures annulées.

**Réponse :**
1. Implémenter un mécanisme d'annulation avec contre-écritures (avoir)
2. Ou marquer les écritures comme "annulées" avec un statut
3. Modifier les requêtes CA/solde pour exclure les écritures annulées

---

#### Alea 16 : Absence de journal de paie global

**Problème :** `EmployeService.salarier()` crée une écriture par employé, mais il n'y a pas de synthèse mensuelle (bulletin de paie global + écriture unique).

**Fichier :** `service/EmployeService.java`

**Risque :** Le journal est pollué par N écritures salaires par mois. Pas de vue synthétique.

**Réponse :**
1. Créer un service `PaieService` qui regroupe les salaires du mois
2. Générer une écriture globale (total des salaires) au lieu d'une par employé
3. Ajouter un tableau de bord RH avec les totaux par période

---

#### Alea 17 : Le prix de sortie de stock utilise le prix de vente, pas le coût d'achat

**Problème :** `saveSortieStock()` (ligne 316-322) calcule le montant de l'écriture OD avec `produit.getPu()` (prix de vente unitaire). Cela enregistre un **revenu** au prix de vente, pas un **coût** au prix d'achat.

**Fichier :** `service/MouvementStockService.java` (lignes 316-322)

**Risque :** Le bilan financier surestime les sorties (en comptabilisant du revenu comme une sortie).

**Réponse :**
1. Utiliser le prix d'achat moyen ou le prix d'achat du lot FIFO sorti
2. Ou créer deux types d'écritures : une pour le coût (ACH) et une pour le revenu (VTE)
3. Calculer la marge bénéficiaire réelle (revenu - coût)

---

#### Alea 18 : Pas de plan comptable structuré

**Problème :** Le journal utilise des types simples (VTE, ACH, BNQ, CSS, OD) sans hiérarchie de comptes. Pas de distinction entre comptes de trésorerie, comptes de charges, comptes de produits.

**Fichier :** `sql/005-module_finance.sql`

**Risque :** Impossible de générer un vrai bilan comptable (actif/passif), un compte de résultat, ou un journal de trésorerie conforme aux normes comptables.

**Réponse :**
1. Créer une table `compte_comptable` avec hiérarchie (classe 1-7)
2. Lier chaque écriture à un compte comptable
3. Refactorer les calculs pour utiliser la structure de comptes
4. C'est un projet en soi — à prioriser selon les besoins réglementaires

---

## 9. Synthèse des Priorités

| Priorité | Alea | Difficulté | Impact |
|----------|------|------------|--------|
| 1 | #1 — `Integer` → `BigDecimal` | 🟢 Facile | Précision financière |
| 2 | #3 — NPE export PDF | 🟢 Facile | Stabilité |
| 3 | #10 — Collision références | 🟡 Moyen | Intégrité données |
| 4 | #6 — Tresorerie bypass service | 🟡 Moyen | Architecture |
| 5 | #7 — Pagination mémoire | 🟡 Moyen | Performance |
| 6 | #8 — Code dupliqué dates | 🟡 Moyen | Maintenabilité |
| 7 | #12 — MethodePaiement non liée | 🟡 Moyen | Modèle données |
| 8 | #13 — Update sortie sans écriture | 🔴 Difficile | Intégrité comptable |
| 9 | #14 — Delete sans réversibilité | 🔴 Difficile | Intégrité comptable |
| 10 | #15 — Annulation facture | 🔴 Difficile | Conformité |
| 11 | #17 — Prix sortie = prix vente | 🔴 Difficile | exactitude bilan |
| 12 | #18 — Pas de plan comptable | 🔴 Difficile | Conformité réglementaire |

---

*Audit réalisé le 14 juillet 2026*
