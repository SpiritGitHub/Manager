# ⚡ ÉnergiVille - Tycoon Énergétique

<div align="center">

**Un jeu de gestion de ville axé sur la production et la distribution d'énergie**

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge&logo=java)
![Maven](https://img.shields.io/badge/Maven-3.8+-red?style=for-the-badge&logo=apache-maven)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

</div>

---

## 📋 Table des Matières

- [À Propos](#-à-propos)
- [Fonctionnalités](#-fonctionnalités)
- [Captures d'Écran](#-captures-décran)
- [Prérequis](#-prérequis)
- [Installation](#-installation)
- [Comment Jouer](#-comment-jouer)
- [Structure du Projet](#-structure-du-projet)
- [Mécaniques de Jeu](#-mécaniques-de-jeu)
- [Technologies Utilisées](#-technologies-utilisées)
- [Développement](#-développement)
- [Contribution](#-contribution)
- [Licence](#-licence)

---

## 🎮 À Propos

**ÉnergiVille** est un jeu de simulation et de gestion où vous incarnez le maire d'une ville en pleine croissance. Votre mission : développer une infrastructure énergétique durable tout en maintenant le bonheur de vos citoyens et l'équilibre financier de votre ville.

Gérez la production d'énergie, construisez des résidences, développez des infrastructures et faites face aux défis d'une ville moderne : pollution, demande énergétique croissante, et satisfaction des habitants.

### 🎯 Objectifs du Jeu

- **Développer votre ville** de niveau 1 à niveau 10
- **Équilibrer** production d'énergie et demande
- **Maintenir** le bonheur des citoyens au-dessus de 50%
- **Gérer** vos finances pour éviter la faillite
- **Réduire** la pollution en investissant dans les énergies renouvelables

---

## ✨ Fonctionnalités

### 🏗️ Construction et Gestion

- **Résidences** : Maisons, Appartements, Gratte-ciels (3 niveaux)
- **Centrales Électriques** :
  - 🔥 Centrale à Charbon (polluante mais économique)
  - ⚛️ Centrale Nucléaire (puissante mais coûteuse)
  - ☀️ Panneaux Solaires (propres, production variable)
  - 💨 Éoliennes (renouvelables, dépendantes du vent)
- **Infrastructures** : Routes, Parcs, Écoles, Hôpitaux

### 📊 Système de Simulation

- **Gestion du Temps** : Simulation jour/nuit avec vitesse ajustable (1x, 2x, 5x)
- **Économie Dynamique** : Revenus, dépenses, maintenance
- **Population** : Croissance organique basée sur le bonheur et les services
- **Énergie** : Production, demande, distribution intelligente
- **Pollution** : Impact sur le bonheur et l'environnement

### 🎨 Interface Utilisateur

- **Vue Carte Interactive** : Grille de construction avec zoom et défilement
- **Panneau de Statistiques** : Argent, population, énergie, bonheur en temps réel
- **Menu de Construction** : Sélection intuitive des bâtiments
- **Notifications** : Alertes et événements importants
- **Système de Sauvegarde** : Sauvegardez et chargez vos parties

### 🏆 Système de Progression

- **10 Niveaux de Ville** : De hameau à métropole
- **Succès Débloquables** : Objectifs et réalisations
- **Objectifs Dynamiques** : Défis adaptés à votre progression
- **3 Niveaux de Difficulté** : Facile, Normal, Difficile

---

## 📸 Captures d'Écran

> _Section à compléter avec des captures d'écran du jeu_

---

## 🔧 Prérequis

Avant de commencer, assurez-vous d'avoir installé :

- **Java Development Kit (JDK) 21** ou supérieur

  - [Télécharger JDK](https://www.oracle.com/java/technologies/downloads/)
  - Vérifiez avec : `java -version`

- **Apache Maven 3.8+**

  - [Télécharger Maven](https://maven.apache.org/download.cgi)
  - Vérifiez avec : `mvn -version`

- **JavaFX 21** (géré automatiquement par Maven)

### Configuration Système Recommandée

- **OS** : Windows 10/11, macOS 10.14+, Linux
- **RAM** : 4 GB minimum, 8 GB recommandé
- **Résolution** : 1024x768 minimum

---

## 📥 Installation

### 1. Cloner le Dépôt

```bash
git clone https://github.com/SpiritGitHub/Manager.git
cd Manager
```

### 2. Compiler le Projet

```bash
mvn clean compile
```

### 3. Lancer le Jeu

```bash
mvn javafx:run
```

### Alternative : Créer un JAR Exécutable

```bash
mvn clean package
java -jar target/Manager-1.0-SNAPSHOT.jar
```

---

## 🎮 Comment Jouer

### Démarrage

1. **Lancez le jeu** avec `mvn javafx:run`
2. **Choisissez votre difficulté** (détermine l'argent de départ)
3. **Commencez à construire** votre ville !

### Contrôles

- **Clic Gauche** : Sélectionner et placer des bâtiments
- **Clic Droit** : Annuler la sélection
- **Molette** : Zoom (si implémenté)
- **Barre d'Espace** : Pause/Reprendre
- **Échap** : Menu principal

### Conseils pour Débutants

1. **Commencez petit** : Construisez quelques résidences et une centrale à charbon
2. **Surveillez vos finances** : Ne dépensez pas tout votre argent d'un coup
3. **Équilibrez l'énergie** : Production ≥ Demande
4. **Pensez long terme** : Investissez dans les énergies renouvelables
5. **Maintenez le bonheur** : Construisez des parcs et des infrastructures

### Conditions de Victoire/Défaite

**🏆 Victoire** : Atteindre le niveau 10 de ville avec un bonheur > 70%

**💀 Défaite** :

- Bonheur < 5% pendant 3 mois
- Dette > 50 000€
- Population = 0

---

## 📁 Structure du Projet

```
Manager/
├── src/
│   └── main/
│       └── java/
│           └── org/
│               └── td/
│                   ├── Main.java                    # Point d'entrée
│                   ├── controller/                  # Contrôleurs
│                   │   ├── GameController.java      # Contrôleur principal
│                   │   ├── BuildingController.java  # Gestion des bâtiments
│                   │   ├── TimeController.java      # Gestion du temps
│                   │   └── ...
│                   ├── model/                       # Modèle de données
│                   │   ├── GameState.java           # État global du jeu
│                   │   ├── entities/                # Entités du jeu
│                   │   │   ├── City.java            # Ville principale
│                   │   │   ├── Building.java        # Classe de base
│                   │   │   ├── Residence.java       # Résidences
│                   │   │   ├── PowerPlant.java      # Centrales
│                   │   │   └── ...
│                   │   ├── enums/                   # Énumérations
│                   │   │   ├── BuildingType.java
│                   │   │   ├── GameSpeed.java
│                   │   │   └── ...
│                   │   └── simulation/              # Moteurs de simulation
│                   │       ├── EconomyManager.java  # Économie
│                   │       ├── EnergySimulator.java # Énergie
│                   │       ├── PopulationManager.java
│                   │       └── TimeManager.java
│                   ├── view/                        # Interface utilisateur
│                   │   ├── MainView.java            # Vue principale
│                   │   ├── CityMapView.java         # Carte de la ville
│                   │   ├── StatsPanel.java          # Panneau de stats
│                   │   ├── BuildingMenu.java        # Menu de construction
│                   │   ├── BuildingListPanel.java   # Liste des bâtiments
│                   │   └── ...
│                   └── utils/                       # Utilitaires
│                       ├── GameConfig.java          # Configuration
│                       ├── UIColors.java            # Couleurs UI
│                       └── UIStyles.java            # Styles UI
├── saves/                                           # Sauvegardes (gitignored)
├── pom.xml                                          # Configuration Maven
├── .gitignore
└── README.md
```

---

## 🎯 Mécaniques de Jeu

### 🏙️ Système de Ville

La ville évolue sur **10 niveaux**, chacun débloquant :

- Plus d'espace constructible
- De nouveaux types de bâtiments
- Des résidences de niveau supérieur

**Conditions de montée de niveau** :

- Population minimale atteinte
- Bonheur > 50%
- Pas de dette importante

### ⚡ Gestion de l'Énergie

**Production** :

- Chaque centrale produit une quantité d'énergie variable
- Les énergies renouvelables dépendent des conditions (jour/nuit, météo)
- La maintenance affecte l'efficacité

**Demande** :

- Chaque résidence consomme de l'énergie
- La demande varie selon l'heure (pics matin/soir)
- Les infrastructures ont aussi une consommation

**Distribution** :

- L'énergie est distribuée automatiquement
- En cas de pénurie : coupures et baisse de bonheur

### 💰 Économie

**Revenus** :

- Taxes des résidences (basées sur population)
- Vente d'électricité
- Bonus de niveau

**Dépenses** :

- Maintenance des bâtiments (mensuelle)
- Coûts opérationnels des centrales
- Salaires et services

### 😊 Bonheur des Citoyens

Facteurs positifs :

- ✅ Énergie suffisante
- ✅ Faible pollution
- ✅ Infrastructures (parcs, écoles, hôpitaux)
- ✅ Revenus stables

Facteurs négatifs :

- ❌ Coupures de courant
- ❌ Pollution élevée
- ❌ Manque de services
- ❌ Surpopulation

### 🌍 Pollution

- Les centrales à charbon polluent beaucoup
- Les centrales nucléaires polluent peu
- Les énergies renouvelables ne polluent pas
- La pollution réduit le bonheur et la santé

### ⏰ Gestion du Temps

- **1 heure de jeu** = quelques secondes réelles
- **Cycle jour/nuit** : 24 heures
- **Mois** : 30 jours
- **Événements mensuels** : Rapports, maintenance, évolution

---

## 🛠️ Technologies Utilisées

### Langage et Framework

- **Java 21** - Langage de programmation
- **JavaFX 21** - Framework d'interface graphique
- **Maven** - Gestion des dépendances et build

### Architecture

- **MVC (Model-View-Controller)** - Pattern architectural
- **Observer Pattern** - Pour les événements et notifications
- **Singleton Pattern** - Pour les gestionnaires globaux

### Bibliothèques

- `javafx-controls` - Composants UI
- `javafx-graphics` - Rendu graphique
- Java Serialization - Système de sauvegarde

---

## 👨‍💻 Développement

### Compiler et Tester

```bash
# Nettoyer et compiler
mvn clean compile

# Exécuter les tests (si disponibles)
mvn test

# Créer un package
mvn package

# Lancer en mode développement
mvn javafx:run
```

### Structure du Code

Le projet suit une architecture **MVC stricte** :

1. **Model** (`org.td.model`) : Logique métier et données
2. **View** (`org.td.view`) : Interface utilisateur JavaFX
3. **Controller** (`org.td.controller`) : Coordination et événements

### Ajouter une Nouvelle Fonctionnalité

1. **Modèle** : Créer/modifier les entités dans `model/entities/`
2. **Simulation** : Ajouter la logique dans `model/simulation/`
3. **Contrôleur** : Gérer les interactions dans `controller/`
4. **Vue** : Créer l'interface dans `view/`
5. **Configuration** : Ajuster les constantes dans `utils/GameConfig.java`

### Conventions de Code

- **Langue** : Commentaires et noms en français
- **Style** : CamelCase pour les classes, camelCase pour les méthodes
- **Documentation** : Javadoc pour les classes et méthodes publiques

---

## 🤝 Contribution

Les contributions sont les bienvenues ! Voici comment participer :

1. **Fork** le projet
2. **Créez** une branche (`git checkout -b feature/AmazingFeature`)
3. **Committez** vos changements (`git commit -m 'Add AmazingFeature'`)
4. **Push** vers la branche (`git push origin feature/AmazingFeature`)
5. **Ouvrez** une Pull Request

### Idées de Contributions

- 🎨 Améliorer l'interface graphique
- 🎵 Ajouter des sons et de la musique
- 🌐 Système de localisation (EN, ES, etc.)
- 🏆 Plus de succès et objectifs
- 🎲 Événements aléatoires (catastrophes, bonus)
- 📊 Graphiques et statistiques avancées
- 🗺️ Différents types de terrains
- 🚗 Système de transport

---

## 📄 Licence

Ce projet est sous licence **MIT** - voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

## 👤 Auteur

**PINDRA AZHAR**
- GitHub: [@SpiritGitHub](https://github.com/SpiritGitHub)
- Email: azharpindra03@gmail.com

---

## 🙏 Remerciements

- Inspiré par les jeux de gestion classiques comme SimCity et Cities: Skylines
- Merci à la communauté JavaFX pour les ressources et tutoriels
- Icônes et assets (si applicable)

---

<div align="center">

**⚡ Construisez la ville énergétique du futur ! ⚡**

Si vous aimez ce projet, n'oubliez pas de lui donner une ⭐ !

</div>
