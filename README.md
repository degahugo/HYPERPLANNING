# Hyperplanning

Application Desktop de gestion d'emploi du temps universitaire développée en JavaFX.

---

## Contexte

Projet académique réalisé dans le cadre du module de Gestion de Projet et Modélisation.

Objectif : concevoir et développer une application type "Hyperplanning" permettant la gestion complète d’un planning universitaire.

---

## Équipe

- Hugo DEGA
- Fatou CISSE
- Dylan MARTIN
- Judyanne DUROGENE
- Yassinne BOULIDAT

---

## Stack Technique

- Java 21
- JavaFX
- Maven
- PostgreSQL
- Docker
- JPA

## Organisation et Structure de travail 

- Architecture MVC
- Méthodologie Scrum
- Git / Github
- Jira
- Discord
 
---

## Fonctionnalités principales (MVP)

### Accès & Sécurité
- Authentification sécurisée
- Gestion des rôles

### Planification
- Création / Modification / Suppression de cours
- Affectation salles
- Affectation enseignants
- Détection de conflits

### Suivi académique
- Gestion des absences
- Notifications

---

## Architecture

Le projet suit une architecture :

- `model` : entités métier
- `view` : interfaces JavaFX
- `controller` : gestion des interactions
- `service` : logique métier
- `dao` : accès base de données

---

##  Organisation Git

- main / work → version stable (Dans le cas actuel, la branche work est la branche contenant la dernière version du code)
- feature/... → développement


---

## Gestion de projet

Méthodologie Agile  
Sprints de 1 semaine  
Livraison finale prévue debut avril 2026.

# HyperPlanning


## Arborescence

```
hyperplanning/
|
+-- src/main/java/fr/univ/hyperplanning/
|   |
|   +-- app/
|   |   +-- controllers/        Les controleurs JavaFX (logique des ecrans)
|   |   +-- MainApp.java        Point d'entree de l'application
|   |
|   +-- dao/                    Acces a la base de donnees (lecture, ecriture)
|   |
|   +-- infra/
|   |   +-- config/             Configuration : connexion BDD, donnees de test
|   |   +-- util/               Outils techniques partagés
|   |
|   +-- model/
|       +-- enums/              Valeurs fixes (roles, types, statuts...)
|       +-- gestion/            Absence, Conflit, DemandeModification...
|       +-- pedagogie/          Cours, Groupe, Salle, Module...
|       +-- utilisateurs/       Utilisateur, Etudiant, Enseignant...
|
+-- src/main/resources/
|   +-- fxml/                   interface graphique
|   +-- META-INF/               Configuration de la base de donnees
|
+-- Dockerfile                  Construction de l'application
+-- docker-compose.yml          Lance l'application + la base de donnees
+-- run.sh                      Script de demarrage
+-- logs.sh                     Script pour voir les logs 
```


## Demarrage 


Prerequis : Docker (Desktop) installé et ouvert ainsi qu'un environnement javafx fonctionnel.

## Démarrage à partir des lignes de commandes (recommandé)

```bash
sdk use java 21.0.10-tem
docker compose down -v

docker compose up -d postgres
docker compose ps

mvn clean compile
mvn javafx:run
```

- PostgreSQL 15+ avec :
    - Base : hyperplanning
    - User : sa
    - Password : secret

## Alternative (lancement à partir du script de démarrage) 

```bash
./run.sh
```


## Lancement à partir du jar (pas fonctionnel)
```bash 
java -jar hyperplanning-1.0-SNAPSHOT.jar
```
Dans le cas ou le .jar n'est pas présent : 
````bash
mvn package -DskipTests
````
cette commande permet de créer le .jar hyperplanning-1.0-SNAPSHOT.jar qui est le fat jar de l'application*


Lors du démarrage après l'affichage de la fenètre graphique pour charger les données construites automatiquement par la base de données une attente d'environ 60 secondes (jusqu'à la fin de la construction des données) est essentiel pour tester l'application avec son jeu de donnée.





La base de donnees demarre automatiquement, les donnees de test
sont inserees au premier lancement, et l'application s'ouvre.


## Liste des différents type d'utilisateurs présent dans la base de données : 

— Administrateur : nom d’utilisateur : AdminSystem1, mot de passe : admin1234  
— Enseignant : nom d’utilisateur : DupontJean2, mot de passe : ens1234  
— Intervenant : nom d’utilisateur : MartinSara4, mot de passe : int1234  
— Etudiant : nom d’utilisateur : DurandAlice5, mot de passe : etu1234  

## Commandes utiles

Arreter le projet (les donnees sont conservees) :
```bash
docker compose down
```

Arreter le projet et effacer toutes les donnees :
```bash
docker compose down -v
```

Voir les logs en direct (dans un 2eme terminal pendant que run.sh tourne) :
```bash
./logs.sh
```

Voir le contenu de la base de donnees :
```bash
docker exec -it $(docker compose ps -q postgres) psql -U sa -d hyperplanning
```

Commandes : 
```
\dt                          lister toutes les tables
SELECT * FROM utilisateurs;  voir le contenu d'une table
\q                           quitter
```

## Workflow Git : 
### Gitflow (Vincent Driessen)   
Objectif : maintenir une branche stable (main) et des branches de développement (feature/...) ou bien (UC/...) pour chaque fonctionnalité / cas d'usages.

````
main
 └── work
      ├── feature/auth
      ├── feature/absence
      ├── feature/salles
      └── ...

````
