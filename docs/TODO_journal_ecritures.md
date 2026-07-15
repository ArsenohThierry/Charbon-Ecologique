# TODO - Enregistrement automatique des écritures journal

## Contexte

Ajouter l'enregistrement automatique d'écritures dans le journal financier lors de :
- Une **sortie de stock**
- Un **paiement de salaire**

La logique existante de sortie de stock et de paiement de salaire n'est pas modifiée.

---

## Fichiers Java modifiés

| # | Fichier | Modification |
|---|---------|-------------|
| 1 | `JournalFinancierService.java` | Ajout `enregistrerSortieStock()` — écriture OD, origine `SORTIE_STOCK`, credit |
| 2 | `JournalFinancierService.java` | Ajout `enregistrerPaiementSalaire()` — écriture CSS, origine `PAIEMENT_SALAIRE`, credit |
| 3 | `EmployeService.java` | Injection de `JournalFinancierService` + appel `enregistrerPaiementSalaire()` dans `salarier()` |
| 4 | `MouvementStockService.java` | Appel `enregistrerSortieStock()` dans `saveSortieStock()` (après le FIFO, logique inchangée) |

## Fichiers SQL modifiés

| # | Fichier | Modification |
|---|---------|-------------|
| 5 | `Charbon.sql` | Ajout origines `SORTIE_STOCK` et `PAIEMENT_SALAIRE` dans les INSERT |
| 6 | `028-origine-sortie-stock.sql` | Ajout des 2 origines (migration) |

## Reste à faire (manuel)

| # | Action |
|---|--------|
| 7 | **Exécuter** `028-origine-sortie-stock.sql` sur la base PostgreSQL pour ajouter les 2 origines |
