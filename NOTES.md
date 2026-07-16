# fonctionnalité "Lots & Statuts de production"

## SQL
- **sql/012-etapes_production_lot.sql** *(créé)*
  Ajoute la colonne ordre à lot_statuts, insère les étapes intermédiaires
  (Broyage, Melange, Pressage, Sechage), ajoute la colonne date_fin à
  statuts_lot_production.

## Modèles (model/)
- **LotStatutsModel.java**
  Ajout du champ ordre (+ getter/setter).
- **StatutsLotProductionModel.java**
  Ajout du champ dateFin (+ getter/setter).

## Repositories (repository/)
- **LotStatutsRepository.java**
  Ajout de findAllByOrderByOrdreAsc().
- **LotProductionRepository.java**
  Ajout de rechercher(...) : requête native avec critères optionnels
  (référence, produit, matière, dates, statut).
- **StatutsLotProductionRepository.java**
  Ajout de findByLotProductionOrderByDateStatutAsc(...) (historique complet
  d'un lot, utilisé pour afficher la date de fin de chaque statut).

## Services (service/)
- **LotStatutsService.java**
  getAllLotsStatuts() retourne désormais la liste triée par ordre.
- **LotProductionService.java**
  - rechercherLots(...) : recherche côté serveur.
  - trier(...) : tri en mémoire (colonnes cliquables)
  - getStatutActuel(...) : statut courant d'un lot.
  - getHistoriqueStatuts(...) : historique des statuts (pour afficher les dates de fin).
  - validerFinStatutCourant(...) : validation de fin de statut + avancement au statut suivant, avec vérifications (date obligatoire, postérieure au statut en cours, pas dans le futur, pas de saut d'étape).

## Controllers (controller/)
- **LotProductionController.java**
  - GET /stock/lot/liste : ajout des paramètres de recherche, tri (tri, direction) et pagination (page, taille).
  - GET /stock/lot/statut/{id} : page de suivi de production.
  - POST /stock/lot/statut/{id}/valider : validation de fin de statut.
  - Fix bug : updateLot() utilisait saveLotProduction() (logique de création) au lieu de updateLotProduction(), ce qui réinitialisait le statut du lot à chaque modification.

## Templates (templates/stitch/module_stock/)
- **liste_lot.html**
  - Barre de filtres transformée en vrai formulaire GET (recherche serveur).
  - En-têtes de colonnes cliquables pour le tri (▲/▼).
  - Pagination (Précédent/Suivant + compteur).
  - Lien "Suivi production" par ligne.
- **detail_1_lot.html**
  Page de suivi de production : liste ordonnée des statuts, badges
  Terminé/En cours/À venir, date de fin par statut, formulaire de validation
  de fin de statut en cours.