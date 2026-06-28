
## — Donnees et logique metier

### Base de donnees

* Creer et modifier les tables :

  * type_journal(id,libelle,code)
  * origine(id,libelle,code)
  * journal_financier(id,reference,date_operation,id_type_journal,id_origine,debit,credit,description)
  * tresorerie(id,date_mouvement,type,montant,solde,libelle,journal_id)
  * import_excel(id,nom_fichier,date_import,nb_lignes,statut,message_log)

### Modèles

* Creer TypeJournalModel
* Creer OrigineModel
* Creer JournalFinancierModel
* Creer TresorerieModel
* Creer ImportExcelModel

### Repositories

* TypeJournalRepository
* OrigineRepository
* JournalFinancierRepository
* TresorerieRepository
* ImportExcelRepository

### Services

Creer les services :

* JournalFinancierService
* TresorerieService

Implementer :

* enregistrer()
* calculerCA()
* calculerBenefice()
* calculerSolde()
* filtrerJournal()

---

## — Interface et affichage

### Controllers

Creer :

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

Creer :

* journal.html
* bilan.html
* tresorerie.html
* kpi.html

### Affichage

* Afficher le journal financier
* Afficher la tresorerie
* Afficher le bilan
* Afficher les indicateurs KPI
* Connecter les vues aux services crees

---