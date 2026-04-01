# 🚀 Space Shooter — Jeu Java

<div align="center">

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/Swing-GUI-512BD4?style=for-the-badge)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Sockets](https://img.shields.io/badge/Sockets-Réseau-00b4d8?style=for-the-badge)

Jeu de tir spatial développé en **Java** avec mode **solo** et **multijoueur en réseau**, conçu dans le cadre du module POO — ENSA Tétouan 2024/2025.

</div>

---

## 🎮 Présentation

**Space Shooter** est un jeu de tir spatial où le joueur contrôle un vaisseau et affronte des vagues d'ennemis. Il propose deux modes de jeu :

- **Mode Solo** — vagues d'ennemis progressives, boss, niveaux de difficulté
- **Mode Multijoueur (PvP)** — duel en réseau en temps réel via sockets Java

---

## ✨ Fonctionnalités

- 🛸 **3 vaisseaux jouables** : Basic, Fast, Tank — chacun avec ses propres stats
- 👾 **3 types d'ennemis** avec comportements différents
- 🎯 **Système de collision** avec hitbox rectangulaires
- 📈 **Progression par niveaux** avec difficulté croissante
- 💾 **Sauvegarde des scores** en base de données MySQL
- 🌐 **Multijoueur réseau** (serveur sur port 5555, connexion par IP)
- 💬 **Chat intégré** entre joueurs (touche `T`)
- 🏆 **Tableau des meilleurs scores**

---

## 🕹️ Contrôles

| Touche | Action |
|--------|--------|
| ← → ↑ ↓ | Déplacer le vaisseau |
| `Espace` | Tirer |
| `T` | Ouvrir le chat (multijoueur) |
| `Échap` | Quitter |

---

## 🔧 Technologies

| Catégorie | Technologie |
|-----------|-------------|
| Langage | Java |
| Interface graphique | Swing (JFrame, JPanel) |
| Réseau | Sockets Java (TCP) |
| Base de données | MySQL via phpMyAdmin |
| Concurrence | Threads Java, `javax.swing.Timer` |
| Collections thread-safe | `ConcurrentHashMap`, `CopyOnWriteArraySet` |

---

## ✅ Prérequis

- [JDK 11+](https://adoptium.net/)
- [MySQL Server](https://www.mysql.com/) + phpMyAdmin
- [Visual Studio Code](https://code.visualstudio.com/) ou [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- [Git](https://git-scm.com/)

---

## 🚀 Installation

### 1. Cloner le dépôt

```bash
git clone https://github.com/votre-utilisateur/space-shooter.git
cd space-shooter
```

### 2. Configurer la base de données

Importez le script SQL dans phpMyAdmin ou via MySQL :

Puis mettez à jour les identifiants dans `GestionBaseDonnees.java` :

```java
private String BD_URL      = "";
private String BD_utilisateur = "root";
private String BD_password    = "votre_mot_de_passe";
```

### 3. Compiler et lancer

```bash
javac -cp .:lib/mysql-connector.jar src/**/*.java -d out/
java -cp out/:lib/mysql-connector.jar Main
```

---

## 🌐 Mode Multijoueur

### Lancer le serveur (joueur Administrateur)

```bash
java -cp out/ Serveur
```

Le serveur écoute sur le **port 5555**.

### Se connecter en tant que client

Au démarrage, entrer l'**adresse IP** du serveur dans l'interface multijoueur.

---

## 🏗️ Architecture

```
src/
├── Main.java                  # Point d'entrée
├── MenuPrincipale.java        # Menu principal
├── BoucleJeu.java             # Boucle de jeu principale
├── Joueur.java                # Classe joueur
├── Ennemi.java                # Classe ennemi
├── Projectile.java            # Classe projectile
├── GestionNiveau.java         # Gestion de la difficulté
├── GestionResources.java      # Chargement des images
├── GestionBaseDonnees.java    # Connexion MySQL & scores
├── Serveur.java               # Serveur multijoueur
├── ClientManager.java         # Gestion des clients connectés
└── ClientHandler.java         # Handler par client
```

---

## 👥 Équipe

| Membre | Rôle |
|--------|------|
| ESSAMIT Taha | Développement du jeu |
| HAJJOU Hajar | Développement du jeu |
| ZERBOUHI Oumaima | Développement du jeu |
| HARISS Houssam | Gestion base de données |

> 📚 Projet encadré par **Prof. ELBOUHDIDI Jaber** — ENSA Tétouan, S2 2024/2025

---

## 📄 Licence

Projet académique — ENSA Tétouan © 2024-2025
