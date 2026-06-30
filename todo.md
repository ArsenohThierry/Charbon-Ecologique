TODO — Pages nécessaires minimum (selon la base et les commentaires)
=====================================================================

1. AUTHENTIFICATION
   [x] Login           — POST /login, GET /login
   [x] Dashboard       — GET /dashboard, affichage personnalisé par rôle
   [x] Logout          — GET /logout

2. APPROVISIONNEMENTS & MATIÈRES PREMIÈRES
   [ ] Fournisseurs
       [x]- Liste fournisseurs (GET /fournisseurs)
       [x]- Ajouter fournisseur (GET+POST /fournisseurs/ajouter)
       [WIP]- Modifier fournisseur (GET+POST /fournisseurs/modifier/{id})
   [ ] Types matières premières
       [x]- Liste (GET /matieres-premieres)
       [x]- Ajouter (GET+POST /matieres-premieres/ajouter)
   [ ] Mouvement stock MP — "Pendant l'insertion des matieres premieres en stock"
       [x]- Entrée stock MP (GET+POST /matieres-premieres/entree)
       [x]- Historique mouvements MP (GET /matieres-premieres/mouvements)

3. PRODUCTION
   [ ] Produits (types de charbon vendables) — "ex: Charbon rond, rectange"
       [x] - Liste (GET /produits)
       [x] - Ajouter (GET+POST /produits/ajouter)
   [ ] Lots de production
       - Liste lots (GET /lots)
       - Créer lot (GET+POST /lots/creer)
       - Détail / suivi lot avec checkbox étapes (GET /lots/{id})
   [ ] Checklist étapes / statuts lot — "...quand tous les etapes sont coches, on met la date et on valide"
       - Interface de suivi: cases à cocher, bouton valider
       - Ajoute statut "Terminé" quand toutes les étapes faites
   [ ] Produits finis — "On insere dans cette table quand le statut d'un lot est Termine"
       - Liste produits finis en attente (GET /produits-finis)
       - Ajouter au stock (POST /produits-finis/valider)

4. STOCK
   [ ] Vue état du stock — "pas de table qui stocke la quantite actuelle, on verifie par requetes"
       - Dashboard stock avec totaux par produit (GET /stock)
   [ ] Mouvements stock — "Entree ou Sortie"
       - Liste mouvements (GET /stock/mouvements)
       - Ajouter mouvement (GET+POST /stock/mouvements/ajouter)
   [ ] Seuils / Alertes — "Qtt faible, Qtt Epuisee, Qtt suffisant"
       - Config seuils (GET+POST /stock/seuils)
       - Alertes visuelles sur le stock

5. VENTES & COMMANDES
   [ ] Clients
       - Liste (GET /clients)
       - Ajouter (GET+POST /clients/ajouter)
   [ ] Commandes — "le client peut acheter differentes types de charbons"
       - Liste (GET /commandes)
       - Créer commande avec plusieurs produits (GET+POST /commandes/creer)
       - Détail commande (GET /commandes/{id})
   [ ] Statuts commandes — "commande, en livraison, livre, annule, en attente"
       - Suivi statuts sur la fiche commande

6. LIVRAISONS
   [ ] Livreurs
       - Liste (GET /livreurs)
       - Ajouter (GET+POST /livreurs/ajouter)
   [ ] Livraisons — "pouvoir selectionner le(les) commandes, puis valider"
       - Liste (GET /livraisons)
       - Créer livraison (GET+POST /livraisons/creer)
       - Détail livraison (GET /livraisons/{id})
   [ ] Statuts livraisons — suivi sur la fiche livraison

7. PAIEMENTS & FACTURES
   [ ] Paiements — "Un paiement s'effectue apres livraison ou par mobile money"
       - Liste (GET /paiements)
       - Enregistrer paiement (GET+POST /paiements/ajouter)
   [ ] Factures — "On cree une facture apres que le montant total ait ete paye"
       - Liste (GET /factures)
       - Générer facture (GET+POST /factures/creer)
       - Détail facture (GET /factures/{id})

8. COMPTABILITÉ
   [ ] Journal financier — "vente, achat, banque, caisse"
       - Liste écritures (GET /journal)
       - Ajouter écriture (GET+POST /journal/ajouter)

9. ADMIN
   [ ] Utilisateurs
       - Liste (GET /admin/utilisateurs)
       - Ajouter (GET+POST /admin/utilisateurs/ajouter)
   [ ] Rôles
       - Gestion rôles (GET+POST /admin/roles)

=====================================================================
LÉGENDE
  [x] = déjà fait    [ ] = à faire
  Les commentaires entre guillemets sont extraits du fichier SQL.
=====================================================================
