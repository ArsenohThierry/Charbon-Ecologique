# Guide de test - Module Financier

# 1. Création d'une commande de test

## Accès

Aller sur :

```
http://localhost:8080/cmd/new
```

## Informations à remplir

- Client
- Produits commandés
- Quantités
- Informations de livraison

Puis sauvegarder.

Après validation, l'application doit rediriger vers une page similaire :

```
/cmd/{id}
```

Exemple :

```
http://localhost:8080/cmd/15
```

Noter l'identifiant de la commande.

---

# 2. Création d'une facture

## Accès à la liste des factures

```
http://localhost:8080/factures
```

Créer une facture depuis une commande existante.

---

## Création directe

URL :

```
http://localhost:8080/factures/new?commandeId={id}
```

Exemple :

```
http://localhost:8080/factures/new?commandeId=15
```

---

## Données à saisir

| Champ | Exemple |
|---|---|
| Commande | CMD-001 |
| Frais livraison | 5000 |
| Méthode paiement | ESPECE / MOBILE / BANQUE |

Puis valider.

---

# 3. Traitement automatique attendu

Lors de l'enregistrement :

```
Commande
    |
    v
FactureController
    |
    v
PaiementService.creerFacture()
    |
    +--> Création paiement
    |
    +--> Création facture
    |
    +--> JournalFinancierService
              |
              v
       Création écriture comptable
```

---

# 4. Vérification du journal financier

## Accès interface

Ouvrir :

```
http://localhost:8080/finance/journal
```

---

## Vérifications attendues

Une nouvelle ligne doit apparaître avec :

| Champ | Vérification |
|-|-|
| Date opération | Date de création |
| Type journal | VTE / BNQ / CSS |
| Débit | Montant facture |
| Crédit | Montant facture |
| Référence | Référence commande/facture |
| Type source | FACTURE |
| ID source | ID de la facture |

---

# 5. Recherche dans le journal

Utiliser les filtres :

- Référence
- Type journal
- Origine
- Date

Exemple :

```
Reference : CMD-001
```

Résultat attendu :

```
CMD-001
FACTURE
VTE
Débit : 50000
Crédit : 50000
```

---

# 6. Vérification directe en base de données

Connexion PostgreSQL :

```sql
SELECT *
FROM journal_financier
WHERE reference = 'CMD-EXEMPLE-001'
ORDER BY date_operation DESC;
```

Remplacer :

```
CMD-EXEMPLE-001
```

par la référence réelle.

---

# 7. Export du journal financier

Tester l'export CSV :

URL :

```
http://localhost:8080/finance/journal/export-csv
```

Vérifier :

- ouverture correcte dans Excel
- présence des écritures
- format des montants
- dates correctes

---

# 8. Tests supplémentaires

## Test doublon

Créer deux fois une facture avec :

```
Même référence
+
Même origine
```

Résultat attendu :

```
Erreur :
Une écriture existe déjà pour cette référence
```

Le système doit empêcher la duplication.

---

## Test suppression / rollback

Provoquer une erreur pendant la création.

Vérifier :

- aucune facture partielle en base
- aucun paiement incomplet
- aucune écriture financière orpheline

---

# 11. Vérification des logs

Pendant le test, surveiller la console Spring Boot.

Exemple de succès :

```
Facture créée avec succès
Paiement enregistré
Écriture financière générée
```

Exemple d'erreur :

```
Exception lors de la création facture
Transaction rollback
```

---

# 12. Checklist de validation

| Test | Résultat |
|-|-|
| Application démarre | ☐ |
| Connexion utilisateur | ☐ |
| Création commande | ☐ |
| Création facture | ☐ |
| Création paiement | ☐ |
| Génération écriture journal | ☐ |
| Recherche journal | ☐ |
| Export CSV | ☐ |
| Gestion doublon | ☐ |
| Rollback transaction | ☐ |

---

# Conclusion

Le test est validé lorsque :

- Une commande peut être transformée en facture.
- Un paiement est généré correctement.
- Une écriture financière apparaît automatiquement.
- Les données du journal correspondent au montant de la facture.
- Les erreurs empêchent les écritures incohérentes.