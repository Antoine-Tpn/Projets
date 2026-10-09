# Robot Mobile ROS 2 — Raspberry Pi & STM32

[![ROS 2](https://img.shields.io/badge/ROS%202-Robotique-22314E?logo=ros)](https://www.ros.org/)
[![Raspberry Pi](https://img.shields.io/badge/Raspberry%20Pi-Embarqué-C51A4A?logo=raspberrypi)](https://www.raspberrypi.com/)
[![STM32](https://img.shields.io/badge/STM32-Nucleo%20F411-03234B?logo=stmicroelectronics)](https://www.st.com/)
[![Python](https://img.shields.io/badge/Python-Traitement%20d'image-3776AB?logo=python)](https://www.python.org/)

## 📌 Présentation

Ce projet consiste à concevoir et programmer un **robot mobile intelligent**, capable de se déplacer, de détecter des obstacles et de suivre une cible grâce à une caméra.

Le système repose sur une architecture distribuée associant un **Raspberry Pi**, une carte **STM32 Nucleo F411** et un ordinateur hôte. Les différents composants communiquent à l'aide de **ROS 2 et Micro-ROS**.

L'objectif est de mettre en œuvre un système robotique complet intégrant l'électronique embarquée, l'asservissement des moteurs, le traitement d'images, la communication entre processeurs et une interface homme-machine.

📚 **Documentation de référence :** [Robot Mobile ROS 2 — Vincent Kerhoas](https://web.enib.fr/~kerhoas/automatique-robotique/robot-mobile-rpi-ros2/)

---

## 🎯 Fonctionnalités

Le robot dispose de trois modes de fonctionnement principaux :

* **Mode manuel :** commande à distance de la vitesse et de la direction du robot, avec détection d'obstacles.
* **Mode aléatoire :** déplacement autonome avec changements réguliers de direction et prise en compte des obstacles.
* **Mode tracking :** détection et suivi d'une cible colorée à partir des images acquises par la caméra.

L'interface de commande sur PC permet de sélectionner le mode de fonctionnement, d'envoyer les consignes de déplacement, de visualiser les mesures des capteurs et d'afficher le flux vidéo de la caméra.

## 🏗️ Architecture du système

Le système est organisé autour de trois éléments principaux.

### 1. STM32 Nucleo F411 — Contrôle bas niveau

Le microcontrôleur assure les fonctions temps réel liées au déplacement du robot :

* Commande des moteurs et asservissement en boucle fermée.
* Acquisition des capteurs de distance.
* Gestion de l'afficheur LCD.
* Communication avec le Raspberry Pi.
* Exécution des tâches temps réel à l'aide d'un RTOS.

### 2. Raspberry Pi — Traitement et communication

Le Raspberry Pi assure les fonctions de traitement et de coordination :

* Acquisition des images de la caméra.
* Traitement d'images pour détecter une cible colorée.
* Exécution des composants ROS 2 nécessaires au système.
* Communication sans fil avec le PC hôte.
* Échanges de données avec le microcontrôleur via Micro-ROS.

### 3. PC hôte — Interface homme-machine

L'ordinateur permet à l'utilisateur de superviser et de commander le robot :

* Sélection du mode de fonctionnement.
* Envoi des consignes de vitesse et de direction.
* Visualisation des mesures des capteurs.
* Affichage du flux vidéo.
* Communication avec le robot via le réseau Wi-Fi.

### 🔄 Communication entre les composants

L'architecture repose sur les échanges suivants :

```text
                 ┌───────────────────────┐
                 │       PC HÔTE          │
                 │ Interface homme-machine│
                 │ Commande / Supervision │
                 └───────────┬───────────┘
                             │
                         Wi-Fi / ROS 2
                             │
                 ┌───────────▼───────────┐
                 │     RASPBERRY PI       │
                 │ Traitement d'images    │
                 │ Communication ROS 2    │
                 └───────────┬───────────┘
                             │
                        Micro-ROS
                             │
                 ┌───────────▼───────────┐
                 │    STM32 NUCLEO F411   │
                 │ Asservissement moteurs │
                 │ Capteurs / RTOS        │
                 └───────────────────────┘
```

Cette séparation permet de répartir les tâches entre le traitement d'images, la supervision et le contrôle temps réel du robot.

## 🛠️ Technologies utilisées

| Technologie             | Rôle                                                       |
| ----------------------- | ---------------------------------------------------------- |
| ROS 2                   | Middleware de communication entre les composants logiciels |
| Micro-ROS               | Communication ROS 2 avec le microcontrôleur embarqué       |
| Raspberry Pi            | Calcul embarqué et traitement d'images                     |
| STM32 Nucleo F411       | Contrôle des moteurs et acquisition des capteurs           |
| RTOS                    | Gestion des tâches temps réel sur le STM32                 |
| Caméra / Webcam         | Acquisition d'images et détection de cible                 |
| Wi-Fi                   | Communication entre le PC hôte et le Raspberry Pi          |
| Interface homme-machine | Commande et supervision du robot                           |

## 📂 Organisation du projet

Le projet est structuré autour de plusieurs domaines fonctionnels :

* **STM32 :** configuration du microcontrôleur, asservissement des moteurs, capteurs et tâches temps réel.
* **Raspberry Pi :** configuration du système, communication ROS 2 et traitement d'images.
* **ROS 2 / Micro-ROS :** échanges de messages entre le PC, le Raspberry Pi et le STM32.
* **Interface PC :** commande du robot, sélection des modes et visualisation des données.
* **Vision :** acquisition vidéo et détection d'une cible colorée.

L'organisation exacte des dossiers dépend de la structure du dépôt Git.

## 🚀 Installation et mise en route

Les étapes suivantes donnent une démarche générale. Les commandes exactes dépendent de la version de ROS 2, du système d'exploitation et des fichiers présents dans le dépôt.

### Prérequis

* Un ordinateur hôte compatible avec l'environnement ROS 2 utilisé.
* Un Raspberry Pi configuré avec un système Linux compatible.
* Une carte STM32 Nucleo F411.
* Le matériel de motorisation et les capteurs nécessaires.
* Une caméra compatible.
* Une connexion réseau entre le PC et le Raspberry Pi.
* Les outils de développement et les dépendances requis par le projet.

### Récupérer le dépôt

```bash
git clone <URL_DU_DEPOT_GIT>
cd <NOM_DU_DEPOT>
```

### Préparer l'environnement

1. Installer la distribution ROS 2 correspondant au projet.
2. Configurer la communication entre le PC hôte et le Raspberry Pi.
3. Installer les dépendances nécessaires aux nœuds ROS 2 et au traitement d'images.
4. Préparer l'environnement Micro-ROS sur le STM32.
5. Compiler les composants logiciels selon les instructions du dépôt.
6. Vérifier les communications avant de tester les moteurs.

> **Important :** consulter la [documentation complète du projet](https://web.enib.fr/~kerhoas/automatique-robotique/robot-mobile-rpi-ros2/) avant l'installation. Les versions logicielles, les dépendances et les procédures de compilation doivent correspondre au matériel réellement utilisé.

## 📖 Documentation

La documentation de référence détaille les différents sous-systèmes :

* [Présentation du projet](https://web.enib.fr/~kerhoas/automatique-robotique/robot-mobile-rpi-ros2/presentation-du-projet/)
* [Architecture du système](https://web.enib.fr/~kerhoas/automatique-robotique/robot-mobile-rpi-ros2/architecture-du-systeme/)
* [Carte STM32 Nucleo F411](https://web.enib.fr/~kerhoas/automatique-robotique/robot-mobile-rpi-ros2/)
* [ROS 2 et Micro-ROS](https://web.enib.fr/~kerhoas/automatique-robotique/robot-mobile-rpi-ros2/ros2-et-micro-ros/)

Ces ressources couvrent notamment le contrôle des moteurs, les communications entre processeurs, le traitement vidéo et l'interface homme-machine.

## 🎓 Objectifs pédagogiques

Ce projet permet de mettre en pratique plusieurs compétences en ingénierie :

* Développement de systèmes embarqués.
* Programmation de microcontrôleurs STM32.
* Asservissement et commande de moteurs électriques.
* Programmation concurrente et temps réel.
* Communication distribuée avec ROS 2 et Micro-ROS.
* Traitement d'images et vision par ordinateur.
* Conception d'interfaces homme-machine.
* Intégration de systèmes matériels et logiciels.

## 📌 État du projet

Ce dépôt a vocation à centraliser le code source, les configurations, les outils et la documentation nécessaires à la réalisation du robot mobile.

Les fonctionnalités effectivement disponibles, les procédures de compilation et les résultats expérimentaux sont à compléter en fonction de l'état réel de l'implémentation.

## 📄 Référence

Projet pédagogique : **Robot Mobile ROS 2**, documentation de Vincent Kerhoas.

Site de référence : https://web.enib.fr/~kerhoas/automatique-robotique/robot-mobile-rpi-ros2/
