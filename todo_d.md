# Fournisseur
## Ajout De fournisseur
### Regle de gestion
- On peut ajouter un nouveau fournisseur a condition que son email ou son numero de telephone n'ait pas une valeur vide "" , et qu'il ne soit pas deja utilise
- Si jamais l'email/telephone n'ait pas une valeur vide, il doit correspondre au format autorisee
- Gestion d'erreurs

## Modification de Fournisseur
### Regle de gestion
- On peut modifier un fournisseur si il est accompagne d'un id et qu'il respecte les regles de gestion de 
l'ajout
- Gestion d'erreurs

## Suppression de Fournisseur
### Regle de  gestion
- On peut supprimer un fournisseur a condition qu'il est reference: qu'il a un id, et qu'il n'est pas deja ete supprime (que delete_at soit null)
- Gestion d'erreurs

## Import de Fournisseur par fichier csv
### Regle de gestion
- Un fichier avec l'extension .csv uniquement
- Le fichier ne doit pas etre vide et ne doit pas depasser 10000 lignes
- Un fichier de 5Mo Maximum autorisee
- Les en-tetes du fichier doivent etre equivalants a ceux decides
- Pour chaque ligne, le nombre de colonnes doit correspondre au nombre d'en-tetes
- On applique les memes regles de gestion comme pour l'ajout de fournisseur pour chaque ligne
- Gestion d'erreurs

# TypeMatierePremiere
## Ajout de TypeMP
### Regle de gestion
- On ne peut pas ajouter de TypeMP sans fournisseur: c'est a dire, un fournisseur existant(avec un id) et non supprime
- Gestion d'erreurs

## Modification de TypeMP
- Le TypeMP doit etre referencee : doit avoir un id pour pouvoir etre modifie
- Les memes regles que l'ajout 
- Gestion d'erreurs

## Suppresion de TypeMP
- On peut supprimer un TypeMP a condition qu'il est reference: qu'il a un id, et qu'il n'est pas deja ete supprime (que delete_at soit null)
- Gestion d'erreurs

# Produits

[x] CRUD complet des produits
[x] Liste des produits
[x] Gestion des erreurs

# LOTS DE PRODUCTION

[x] CRUD complet des lots de production

[x] Gestion des statuts de production
- Historique
- Validation d'un statut
- Passage automatique au statut suivant

[x] Liste des lots
- Recherche multicritère
- Tri
- Pagination par differents criteres

[x] Gestion des erreurs

## RÈGLES DE GESTION
- La création d'un lot initialise automatiquement son premier statut de production.
- La validation d'un statut clôt le statut courant et crée automatiquement le statut suivant pour le lot concerne
- De plus, il faut respecter l'ordre des statuts imposes en base de donnees
- Le passage en statut `Termine` doit permettre au module Stock de faire des entrees/sortie de stock sur ce lot
- La date de fin d'un statut doit être cohérente (postérieure à la date de début et ne doit pas etre uen date future)