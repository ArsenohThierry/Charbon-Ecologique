- CRUD des fournisseurs 
- Liste des fournisseurs
    template : src/main/resources/templates/stitch/module_stock/fournisseurs.html

- Matieres premiers :
    1 - CRUD et Liste des TYPES DE MATIERES PREMIERES
        template : src/main/resources/templates/stitch/module_stock/types_mat_prem.html

    2 - CRUD des Matieres premieres ( insertion dns le mouvement stock des matieres premieres ) : ex : 3kg de maiss le 26/06/2026
        template : src/main/resources/templates/stitch/module_stock/stock_mat_prem.html ( fixeo kely mba centrena le div)

    3 - Liste des MOUVEMENTS Stock de matieres premieres 
    
    template : src/main/resources/templates/stitch/module_stock/historique_mouvements_mp.html

- CRUD et liste des Types de charbons a vendre ( ex : Charbon Rond , Charbon 200g , ...)
    template : src/main/resources/templates/stitch/module_stock/produits.html



A FAIRE : 
- Cloner le repo git
- Creer une nouvelle branche ( ex : features-CRUD-production)
- Publier la branche 
- Commencer ce que j ai dit eo ambony
- Push refa mahavita 1 ( Push sur la bonne branche )
- BIEN TESTER 
- Continuer su les autres , ...
- Push sur la branche (features-CRUD-production ) 
- BIEN TESTER
- Pull request vers dev 
- attendre 
- ...

## SUITE :

- Mouvement des stocks ( entree de charbons produits  ) :
    1 - Lots de production :
        - CRUD et Liste des lots :
            templates :
                - src/main/resources/templates/stitch/module_stock/liste_lot_production.html
                - src/main/resources/templates/stitch/module_stock/saisir_nouveau_lot.html
                - src/main/resources/templates/stitch/module_stock/detail_1_lot.html