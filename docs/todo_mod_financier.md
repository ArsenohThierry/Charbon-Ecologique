
## — Données et logique métier

### Base de données

* Créer et modifier les tables :

  * type_journal(id,libelle,code)
  * origine(id,libelle,code)
  * journal_financier(id,reference,date_operation,id_type_journal,id_origine,debit,credit,description)
  * tresorerie(id,date_mouvement,type,montant,solde,libelle,journal_id)
  * import_excel(id,nom_fichier,date_import,nb_lignes,statut,message_log)

### Modèles

* Créer TypeJournalModel
* Créer OrigineModel
* Créer JournalFinancierModel
* Créer TresorerieModel
* Créer ImportExcelModel

### Repositories

* TypeJournalRepository
* OrigineRepository
* JournalFinancierRepository
* TresorerieRepository
* ImportExcelRepository

### Services

Créer les services :

* JournalFinancierService
* TresorerieService

Implémenter :

* enregistrer()
* calculerCA()
* calculerBenefice()
* calculerSolde()
* filtrerJournal()

---

## — Interface et affichage

### Controllers

Créer :

* JournalController
* BilanController
* TresorerieController
* KpiController

Ajouter les routes :

* /finance/journal
* /finance/bilan
* /finance/tresorerie
* /finance/kpi

### Pages Thymeleaf

Créer :

* journal.html
* bilan.html
* tresorerie.html
* kpi.html

### Affichage

* Afficher le journal financier
* Afficher la trésorerie
* Afficher le bilan
* Afficher les indicateurs KPI
* Connecter les vues aux services créés

---