# Base de données et couche d'accès aux données

## 1. Base de données

* [ ] Créer une migration `024-update_module_finance.sql`
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

# Services métier et API du module financier

## 1. Service

Modifier :

* [ ] `JournalFinancierService`

Créer les méthodes :

* [ ] `enregistrerVente(...)`
* [ ] `enregistrerPaiement(...)`
* [ ] `enregistrerAchat(...)`
* [ ] `enregistrerFraisLivraison(...)` 
* [ ] `verifierDoublon(...)`
---

## 2. Contrôleurs

Vérifier ou compléter :

* [ ] `JournalController`
* [ ] `TresorerieController`
* [ ] `BilanController`

---
