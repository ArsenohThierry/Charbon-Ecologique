# TODO - Module Financier (Répartition pour 2 personnes)

## Objectif

Mettre en place le module financier afin qu'il puisse recevoir des écritures provenant des autres modules (Commercial, Paiement, Stock, etc.) sans développer ces modules.

---

# Personne 1 — Base de données et couche d'accès aux données

## 1. Base de données

* [ ] Créer une migration `025-update_module_finance.sql`
* [ ] Ajouter `type_source` dans `journal_financier`
* [ ] Ajouter `id_source`
* [ ] Ajouter `created_at`
* [ ] Ajouter la contrainte UNIQUE `(reference, id_origine)`
* [ ] Ajouter les index nécessaires
* [ ] Vérifier les données de `type_journal`
* [ ] Vérifier les données de `origine`

---

## 2. Modèles

Modifier :

* [ ] `JournalFinancierModel`

Ajouter :

* [ ] `typeSource`
* [ ] `idSource`
* [ ] `createdAt`

---

## 3. Repository

Modifier :

* [ ] `JournalFinancierRepository`

Ajouter :

* [ ] recherche par référence
* [ ] vérification des doublons
* [ ] recherche par typeSource si nécessaire

---

## 4. Tests de persistance

* [ ] création d'une écriture
* [ ] refus d'un doublon
* [ ] lecture des écritures
* [ ] validation des contraintes SQL

---

# Personne 2 — Services métier et API du module financier

## 1. Service

Modifier :

* [ ] `JournalFinancierService`

Créer les méthodes :

* [ ] `enregistrerVente(...)`
* [ ] `enregistrerPaiement(...)`
* [ ] `enregistrerAchat(...)`
* [ ] `enregistrerFraisLivraison(...)`
* [ ] `verifierDoublon(...)`

Toutes les écritures devront passer par ce service.

---

## 2. Contrôleurs

Vérifier ou compléter :

* [ ] `JournalController`
* [ ] `TresorerieController`
* [ ] `BilanController`

---

## 3. Calculs

Vérifier :

* [ ] Trésorerie
* [ ] Chiffre d'affaires
* [ ] Dépenses
* [ ] Solde global

---

## 4. Tests fonctionnels

Tester chaque méthode du service :

* [ ] Vente
* [ ] Paiement
* [ ] Achat
* [ ] Frais de livraison
* [ ] Détection des doublons

---

# Validation finale (ensemble)

* [ ] Toutes les migrations SQL s'exécutent correctement.
* [ ] Les modèles correspondent au schéma de la base.
* [ ] Les services enregistrent correctement les écritures.
* [ ] Les doublons sont empêchés.
* [ ] Les contrôleurs renvoient les bonnes informations.
* [ ] Les vues SQL (`tresorerie`, `chiffre_affaires`, `depenses`, `solde_global`) fonctionnent toujours.
* [ ] Les autres modules pourront appeler le `JournalFinancierService` sans modification du module financier.

---

## Répartition Git

### Personne 1

* Base de données
* Model
* Repository

Branche :

`feature/finance-database`

### Personne 2

* Service
* Controller
* Tests

Branche :

`feature/finance-service`

Fusion après validation des deux parties.
