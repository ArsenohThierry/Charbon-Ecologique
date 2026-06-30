## Donnees et logique metier

### Base de donnees

* type_journal(id, libelle, code)
* origine(id, libelle, code)
* journal_financier(id, reference, date_operation, id_type_journal, id_origine, debit, credit, description)
* import_excel(id, nom_fichier, date_import, nb_lignes, statut, message_log)

### Modele recommande

Autres modules :

* commandes
* paiements
* fournisseurs
* factures
* achats fournisseur

Ces modules generent des ecritures dans `journal_financier`.

### Services

* `JournalFinancierService`
  * enregistrer une ecriture
  * calculer le chiffre d'affaires
  * calculer les entrees
  * calculer les sorties
  * calculer le benefice
  * filtrer le journal

* `TresorerieService`
  * calculer le solde courant avec `SUM(debit) - SUM(credit)`
  * calculer le total des entrees avec `SUM(debit)`
  * calculer le total des sorties avec `SUM(credit)`
  * construire l'historique avec solde cumule

---

## Interface et affichage

### Controllers

Routes:

* `/finance/journal`
* `/finance/bilan`
* `/finance/tresorerie`
* `/finance/kpi`

### Pages Thymeleaf

Pages:

* `journal.html`
* `bilan.html`
* `tresorerie.html`
* `kpi.html`

### Affichage

* Afficher le journal financier avec debit et credit.
* Afficher la tresorerie comme vue calculee du journal financier.
* Afficher le bilan.
* Afficher les indicateurs KPI.
* Connecter les vues aux services crees.

---

## Requetes utiles

Solde actuel :

```sql
SELECT COALESCE(SUM(debit), 0) - COALESCE(SUM(credit), 0) AS solde
FROM journal_financier;
```

Historique de tresorerie :

```sql
SELECT
    reference,
    date_operation,
    description,
    debit,
    credit,
    SUM(debit - credit) OVER(ORDER BY date_operation, id) AS solde
FROM journal_financier
ORDER BY date_operation, id;
```
