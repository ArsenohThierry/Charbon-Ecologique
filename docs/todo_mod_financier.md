# TODO Module Financier — v3
**Stack :** Spring Boot + Thymeleaf + PostgreSQL + Lombok
**Durée restante :** 1-2 jours

---

## CE QUI EXISTE DÉJÀ ✅

- Auth + login fonctionnel (`AuthController`, `RoleModel`, `UtilisateurModel`)
- `JournalFinancierService.java` — existe mais vide, à compléter
- Templates module stock dans `stitch/module_stock/` → même convention à suivre
- Pas de DTOs — on passe les valeurs directement via `model.addAttribute()`

---

## JOUR 1

### ÉTAPE 0 — SQL (ensemble, 30 min)

Un seul exécute pendant que l'autre prépare les modèles.

- [ ] Exécuter `004-correction_tables_mod_financier.sql`
- [ ] Vérifier les tables en base :

```sql
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public' ORDER BY table_name;
```

Tables attendues : `journal_financier`, `type_journal`, `origine`, `tresorerie`, `import_excel`

- [ ] Insérer les données de référence :

```sql
INSERT INTO type_journal (libelle, code) VALUES
  ('Vente', 'VTE'), ('Achat', 'ACH'),
  ('Banque', 'BNQ'), ('Caisse', 'CSS')
ON CONFLICT DO NOTHING;

INSERT INTO origine (libelle) VALUES
  ('Commande'), ('Paiement'), ('Achat'), ('Mouvement stock')
ON CONFLICT DO NOTHING;
```

---

## PERSONNE A — Données et calculs

### A1 · Modèles JPA (45 min)

> Style identique à `RoleModel` du projet + Lombok `@Data`

- [ ] `TypeJournalModel.java`

```java
package com.example.charbonecolo.model;

import jakarta.persistence.*;
;

@Data
@Entity
@Table(name = "type_journal")
public class TypeJournalModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String libelle;

    @Column(nullable = false, length = 50)
    private String code;
}
```

- [ ] `OrigineModel.java`

```java
package com.example.charbonecolo.model;

import jakarta.persistence.*;
;

@Data
@Entity
@Table(name = "origine")
public class OrigineModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String libelle;
}
```

- [ ] `JournalFinancierModel.java`

```java
package com.example.charbonecolo.model;

import jakarta.persistence.*;
;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "journal_financier")
public class JournalFinancierModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(name = "date_operation", nullable = false)
    private LocalDateTime dateOperation;

    @ManyToOne
    @JoinColumn(name = "id_type_journal", nullable = false)
    private TypeJournalModel typeJournal;

    @ManyToOne
    @JoinColumn(name = "id_origine", nullable = false)
    private OrigineModel origine;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal debit;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal credit;

    @Column(columnDefinition = "TEXT")
    private String description;
}
```

- [ ] `TresorerieModel.java`

```java
package com.example.charbonecolo.model;

import jakarta.persistence.*;
;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tresorerie")
public class TresorerieModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date_operation", nullable = false)
    private LocalDateTime dateOperation;

    @Column(name = "type_operation", nullable = false, length = 10)
    private String typeOperation; // ENTREE / SORTIE

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(length = 50)
    private String origine;

    @Column(name = "reference_origine", length = 50)
    private String referenceOrigine;

    @Column(columnDefinition = "TEXT")
    private String description;
}
```

- [ ] `ImportExcelModel.java`

```java
package com.example.charbonecolo.model;

import jakarta.persistence.*;
;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "import_excel")
public class ImportExcelModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nom_fichier", nullable = false, length = 100)
    private String nomFichier;

    @Column(name = "date_import", nullable = false)
    private LocalDateTime dateImport;

    @Column(nullable = false, length = 20)
    private String statut; // EN_COURS / TERMINE / ERREUR

    @Column(name = "nb_lignes")
    private Integer nbLignes;

    @Column(name = "message_erreur", columnDefinition = "TEXT")
    private String messageErreur;
}
```

---

### A2 · Repositories (20 min)

- [ ] `TypeJournalRepository.java`

```java
package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.TypeJournalModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TypeJournalRepository extends JpaRepository<TypeJournalModel, Integer> {
    Optional<TypeJournalModel> findByLibelle(String libelle);
}
```

- [ ] `OrigineRepository.java`

```java
package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.OrigineModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrigineRepository extends JpaRepository<OrigineModel, Integer> {
    Optional<OrigineModel> findByLibelle(String libelle);
}
```

- [ ] `JournalFinancierRepository.java`

```java
package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.JournalFinancierModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface JournalFinancierRepository extends JpaRepository<JournalFinancierModel, Integer> {

    List<JournalFinancierModel> findByDateOperationBetween(
        LocalDateTime debut, LocalDateTime fin);

    List<JournalFinancierModel> findByTypeJournalLibelleAndDateOperationBetween(
        String libelle, LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT SUM(j.credit) FROM JournalFinancierModel j " +
           "WHERE j.typeJournal.libelle = 'Vente' " +
           "AND j.dateOperation BETWEEN :debut AND :fin")
    BigDecimal calculerCA(@Param("debut") LocalDateTime debut,
                          @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(j.credit) - SUM(j.debit) FROM JournalFinancierModel j " +
           "WHERE j.dateOperation BETWEEN :debut AND :fin")
    BigDecimal calculerBenefice(@Param("debut") LocalDateTime debut,
                                @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(j.credit) FROM JournalFinancierModel j " +
           "WHERE j.dateOperation BETWEEN :debut AND :fin")
    BigDecimal calculerTotalEntrees(@Param("debut") LocalDateTime debut,
                                    @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(j.debit) FROM JournalFinancierModel j " +
           "WHERE j.dateOperation BETWEEN :debut AND :fin")
    BigDecimal calculerTotalSorties(@Param("debut") LocalDateTime debut,
                                    @Param("fin") LocalDateTime fin);

    @Query(value = "SELECT DATE_TRUNC('month', date_operation) as mois, " +
                   "SUM(credit) as ca FROM journal_financier " +
                   "JOIN type_journal tj ON id_type_journal = tj.id " +
                   "WHERE tj.libelle = 'Vente' " +
                   "GROUP BY mois ORDER BY mois", nativeQuery = true)
    List<Object[]> evolutionCA();
}
```

- [ ] `TresorerieRepository.java`

```java
package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.TresorerieModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;

public interface TresorerieRepository extends JpaRepository<TresorerieModel, Integer> {

    @Query("SELECT SUM(CASE WHEN t.typeOperation = 'ENTREE' " +
           "THEN t.montant ELSE -t.montant END) FROM TresorerieModel t")
    BigDecimal calculerSolde();
}
```

- [ ] `ImportExcelRepository.java` — extend JpaRepository simple

---

### A3 · Compléter JournalFinancierService (1h)

> Le fichier existe — le vider et réécrire :

```java
package com.example.charbonecolo.service;

import com.example.charbonecolo.model.*;
import com.example.charbonecolo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalFinancierService {

    private final JournalFinancierRepository journalRepo;
    private final TresorerieRepository tresorerieRepo;
    private final TypeJournalRepository typeJournalRepo;
    private final OrigineRepository origineRepo;

    // Appelé par les autres modules après validation d'une opération
    public void enregistrer(String typeLibelle, String origineLibelle,
                            String reference, BigDecimal debit,
                            BigDecimal credit, String description) {
        TypeJournalModel type = typeJournalRepo.findByLibelle(typeLibelle)
            .orElseThrow(() -> new RuntimeException("Type journal introuvable : " + typeLibelle));
        OrigineModel origine = origineRepo.findByLibelle(origineLibelle)
            .orElseThrow(() -> new RuntimeException("Origine introuvable : " + origineLibelle));

        JournalFinancierModel ligne = new JournalFinancierModel();
        ligne.setReference(reference);
        ligne.setDateOperation(LocalDateTime.now());
        ligne.setTypeJournal(type);
        ligne.setOrigine(origine);
        ligne.setDebit(debit != null ? debit : BigDecimal.ZERO);
        ligne.setCredit(credit != null ? credit : BigDecimal.ZERO);
        ligne.setDescription(description);
        journalRepo.save(ligne);
    }

    public List<JournalFinancierModel> filtrerJournal(
            String type, LocalDateTime debut, LocalDateTime fin) {
        if (type != null && !type.isBlank()) {
            return journalRepo.findByTypeJournalLibelleAndDateOperationBetween(type, debut, fin);
        }
        return journalRepo.findByDateOperationBetween(debut, fin);
    }

    public BigDecimal calculerCA(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal v = journalRepo.calculerCA(debut, fin);
        return v != null ? v : BigDecimal.ZERO;
    }

    public BigDecimal calculerBenefice(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal v = journalRepo.calculerBenefice(debut, fin);
        return v != null ? v : BigDecimal.ZERO;
    }

    public BigDecimal calculerTotalEntrees(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal v = journalRepo.calculerTotalEntrees(debut, fin);
        return v != null ? v : BigDecimal.ZERO;
    }

    public BigDecimal calculerTotalSorties(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal v = journalRepo.calculerTotalSorties(debut, fin);
        return v != null ? v : BigDecimal.ZERO;
    }

    public BigDecimal calculerSolde() {
        BigDecimal v = tresorerieRepo.calculerSolde();
        return v != null ? v : BigDecimal.ZERO;
    }

    public List<Object[]> evolutionCA() {
        return journalRepo.evolutionCA();
    }
}
```

- [ ] `TresorerieService.java`

```java
@Service
@RequiredArgsConstructor
public class TresorerieService {
    private final TresorerieRepository tresorerieRepo;

    public List<TresorerieModel> findAll() {
        return tresorerieRepo.findAll();
    }

    public void enregistrer(String typeOp, BigDecimal montant,
                             String origine, String reference, String description) {
        TresorerieModel t = new TresorerieModel();
        t.setDateOperation(LocalDateTime.now());
        t.setTypeOperation(typeOp);
        t.setMontant(montant);
        t.setOrigine(origine);
        t.setReferenceOrigine(reference);
        t.setDescription(description);
        tresorerieRepo.save(t);
    }
}
```

- [ ] `ImportExcelService.java` — lire .xlsx ligne par ligne → appeler `enregistrer()` sur chaque ligne

---

### A4 · Controllers (45 min)

- [ ] `TresorerieController.java`

```java
@Controller
@RequestMapping("/finance")
@RequiredArgsConstructor
public class TresorerieController {

    private final JournalFinancierService financeService;
    private final TresorerieService tresorerieService;

    @GetMapping("/tresorerie")
    public String afficher(Model model) {
        model.addAttribute("solde", financeService.calculerSolde());
        model.addAttribute("mouvements", tresorerieService.findAll());
        return "stitch/module_finance/tresorerie";
    }
}
```

- [ ] `ImportController.java`

```java
@Controller
@RequestMapping("/finance")
@RequiredArgsConstructor
public class ImportController {

    private final ImportExcelService importService;

    @GetMapping("/import")
    public String afficher() {
        return "stitch/module_finance/import";
    }

    @PostMapping("/import")
    public String importer(@RequestParam("fichier") MultipartFile fichier,
                           RedirectAttributes ra) {
        try {
            importService.importer(fichier);
            ra.addFlashAttribute("succes", "Import réussi");
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/finance/import";
    }
}
```

---

## PERSONNE B — Affichage et reporting

### B1 · Controllers reporting (1h30)

- [ ] `JournalController.java`

```java
@Controller
@RequestMapping("/finance")
@RequiredArgsConstructor
public class JournalController {

    private final JournalFinancierService financeService;
    private final TypeJournalRepository typeJournalRepo;

    @GetMapping("/journal")
    public String afficher(
            @RequestParam(required = false) String type,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        LocalDateTime d = debut != null ? debut.atStartOfDay()
                                        : LocalDateTime.now().minusMonths(1);
        LocalDateTime f = fin != null ? fin.atTime(23, 59, 59)
                                      : LocalDateTime.now();

        model.addAttribute("lignes", financeService.filtrerJournal(type, d, f));
        model.addAttribute("types", typeJournalRepo.findAll());
        model.addAttribute("typeSelectionne", type);
        return "stitch/module_finance/journal";
    }

    @GetMapping("/journal/export-csv")
    public ResponseEntity<byte[]> exportCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("Date,Type,Référence,Description,Débit,Crédit\n");
        financeService.filtrerJournal(null,
            LocalDateTime.now().minusYears(1), LocalDateTime.now())
            .forEach(l -> sb
                .append(l.getDateOperation()).append(",")
                .append(l.getTypeJournal().getLibelle()).append(",")
                .append(l.getReference()).append(",")
                .append(l.getDescription()).append(",")
                .append(l.getDebit()).append(",")
                .append(l.getCredit()).append("\n"));
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=journal.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(bytes);
    }
}
```

- [ ] `BilanController.java`

```java
@Controller
@RequestMapping("/finance")
@RequiredArgsConstructor
public class BilanController {

    private final JournalFinancierService financeService;

    @GetMapping("/bilan")
    public String afficher(
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        LocalDateTime d = debut != null ? debut.atStartOfDay()
                                        : LocalDateTime.now().withDayOfYear(1);
        LocalDateTime f = fin != null ? fin.atTime(23, 59, 59)
                                      : LocalDateTime.now();

        model.addAttribute("ca",          financeService.calculerCA(d, f));
        model.addAttribute("totalEntrees",financeService.calculerTotalEntrees(d, f));
        model.addAttribute("totalSorties",financeService.calculerTotalSorties(d, f));
        model.addAttribute("benefice",    financeService.calculerBenefice(d, f));
        model.addAttribute("solde",       financeService.calculerSolde());
        return "stitch/module_finance/bilan";
    }
}
```

- [ ] `KpiController.java`

```java
@Controller
@RequestMapping("/finance")
@RequiredArgsConstructor
public class KpiController {

    private final JournalFinancierService financeService;

    @GetMapping("/kpi")
    public String afficher(Model model) {
        LocalDateTime debut = LocalDateTime.now().withDayOfYear(1);
        LocalDateTime fin   = LocalDateTime.now();

        model.addAttribute("ca",         financeService.calculerCA(debut, fin));
        model.addAttribute("benefice",   financeService.calculerBenefice(debut, fin));
        model.addAttribute("solde",      financeService.calculerSolde());
        model.addAttribute("evolutionCA",financeService.evolutionCA());
        return "stitch/module_finance/kpi";
    }
}
```

---

### B2 · Templates Thymeleaf (2h)

Créer dans `templates/stitch/module_finance/` :

- [ ] `journal.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Journal financier</title></head>
<body>
  <form method="get" th:action="@{/finance/journal}">
    <select name="type">
      <option value="">Tous</option>
      <option th:each="t : ${types}"
              th:value="${t.libelle}"
              th:text="${t.libelle}"
              th:selected="${t.libelle == typeSelectionne}"/>
    </select>
    <input type="date" name="debut"/>
    <input type="date" name="fin"/>
    <button type="submit">Filtrer</button>
    <a th:href="@{/finance/journal/export-csv}">Export CSV</a>
  </form>

  <table>
    <thead>
      <tr>
        <th>Date</th><th>Type</th><th>Référence</th>
        <th>Description</th><th>Débit</th><th>Crédit</th>
      </tr>
    </thead>
    <tbody>
      <tr th:if="${#lists.isEmpty(lignes)}">
        <td colspan="6">Aucune donnée</td>
      </tr>
      <tr th:each="l : ${lignes}">
        <td th:text="${l.dateOperation}"/>
        <td th:text="${l.typeJournal.libelle}"/>
        <td th:text="${l.reference}"/>
        <td th:text="${l.description}"/>
        <td th:text="${l.debit}"/>
        <td th:text="${l.credit}"/>
      </tr>
    </tbody>
  </table>
</body>
</html>
```

- [ ] `bilan.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Bilan</title></head>
<body>
  <form method="get" th:action="@{/finance/bilan}">
    <input type="date" name="debut"/>
    <input type="date" name="fin"/>
    <button type="submit">Générer</button>
  </form>

  <table>
    <tr><td>Chiffre d'affaires</td><td th:text="${ca}"/></tr>
    <tr><td>Total entrées</td>      <td th:text="${totalEntrees}"/></tr>
    <tr><td>Total sorties</td>      <td th:text="${totalSorties}"/></tr>
    <tr><td>Bénéfice net</td>       <td th:text="${benefice}"/></tr>
    <tr><td>Solde trésorerie</td>   <td th:text="${solde}"/></tr>
  </table>

  <a th:href="@{/finance/bilan/export-excel}">Export Excel</a>
</body>
</html>
```

- [ ] `kpi.html` — 4 cartes : CA / Bénéfice / Solde / tableau évolution CA par mois
- [ ] `tresorerie.html` — solde en haut + liste `mouvements`
- [ ] `import.html` — formulaire `enctype="multipart/form-data"` + affichage `succes` / `erreur`

---

## JOUR 2 — Matin (ensemble, 1h)

- [ ] Dans `PaiementService` — ajouter appel après paiement validé :

```java
journalFinancierService.enregistrer(
    "Vente", "Paiement", paiement.getReference(),
    BigDecimal.ZERO, paiement.getMontantTotal(),
    "Paiement commande " + paiement.getCommande().getReference()
);
```

- [ ] Dans `TypeMatierePremiereService` ou achat — ajouter appel après achat :

```java
journalFinancierService.enregistrer(
    "Achat", "Achat", reference,
    montant, BigDecimal.ZERO,
    "Achat matière première"
);
```

- [ ] Tester les calculs avec données réelles
- [ ] Ajouter liens `/finance/*` dans `dashboard.html`

---

## JOUR 2 — Après-midi

**Personne A**
- [ ] Gestion erreurs `ImportExcelService` → mettre statut `ERREUR` + message
- [ ] Tester import avec un vrai fichier .xlsx

**Personne B**
- [ ] Export Excel bilan avec Apache POI
- [ ] Tableau évolution CA par mois dans `kpi.html`
- [ ] Cas vides : afficher message si aucune donnée dans chaque template

---

## Priorité absolue si manque de temps

| # | Tâche | Qui |
|---|---|---|
| 🔴 1 | Tables SQL + données référence | Ensemble |
| 🔴 2 | 5 modèles JPA | A |
| 🔴 3 | Repositories | A |
| 🔴 4 | JournalFinancierService complété | A |
| 🔴 5 | journal.html + JournalController | B |
| 🔴 6 | bilan.html + BilanController | B |
| 🟠 7 | kpi.html + KpiController | B |
| 🟠 8 | Connexion PaiementService → journal | Ensemble |
| 🟡 9 | tresorerie.html + TresorerieController | A |
| 🟡 10 | Export CSV | B |
| 🟢 11 | Import Excel | A |
| 🟢 12 | Export Excel bilan | B |
| 🟢 13 | Évolution CA par mois | B |

> **Minimum vital pour la soutenance :**
> Journal filtrable + Bilan (CA, bénéfice, solde) + KPI = 3 pages qui tournent.