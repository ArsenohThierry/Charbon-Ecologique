#	Problème	Où
1	Pas de couche Service	Aucun @Service — la logique auth est dans le controller
2	Un seul controller	AuthController seulement — les 40+ routes à venir sont à créer
3	Un seul repository	UtilisateurRepository seulement — les 30+ tables n'ont pas d'entités JPA
4	Auth maison sans Spring Security	Session-based avec HttpSession, pas de hash, pas de CSRF, pas de protection d'URL
5	Mots de passe en clair	if (motPasse.equals(password)) dans le controller
6	Mockups statiques uniquement	fournisseurs.html etc. ont des données en dur, pas de th:each/th:text
7	Aucun test	Le test généré CharbonecoloApplicationTests ne fait rien
8	Pas de gestion d'erreur globale	Pas de @ControllerAdvice ou ErrorController
9	Tailwind via CDN	OK pour prototype mais limité hors-ligne