package com.example.charbonecolo.config;

public final class Constantes {

    private Constantes() {
        // Constructeur privé pour empêcher l'instanciation
    }

    // Types de journal
    public static final String JOURNAL_VENTE = "Vente";
    public static final String JOURNAL_ACHAT = "Achat";
    public static final String JOURNAL_BANQUE = "Banque";
    public static final String JOURNAL_CAISSE = "Caisse";

    // Origines des écritures
    public static final String ORIGINE_COMMANDE = "Commande";
    public static final String ORIGINE_PAIEMENT = "Paiement";
    public static final String ORIGINE_ACHAT = "Achat";
    public static final String ORIGINE_MOUVEMENT_STOCK = "Mouvement stock";

    // Opérations de trésorerie
    public static final String TRESORERIE_ENTREE = "ENTREE";
    public static final String TRESORERIE_SORTIE = "SORTIE";
}
