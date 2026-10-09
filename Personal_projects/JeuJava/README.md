# Les Catacombes

Jeu d'exploration et de combat en 2D realise avec Java et LibGDX. Incarnez un
orc, explorez des catacombes de pierre et affrontez des ennemis varies.
Les personnages et les decors sont des sprites pixel art generes par le jeu :
aucun telechargement d'images supplementaires n'est necessaire.

## Installation et lancement sous Windows

Installez un JDK recent (17 ou plus recent), par exemple :

```powershell
winget install EclipseAdoptium.Temurin.21.JDK
```

Fermez puis rouvrez PowerShell apres l'installation. Depuis le dossier du projet,
lancez le wrapper Gradle inclus :

```powershell
.\gradlew.bat run
```

Le wrapper utilise Gradle 9.8.1, compatible avec Java 25, puis telecharge
automatiquement LibGDX et ses dependances au premier lancement. Une connexion
Internet est necessaire la premiere fois.

## Organisation

- `src/main/java/com/catacombes/Lwjgl3Launcher.java` : lanceur LibGDX pour PC.
- `Main.java` : gere l'application et ses ecrans.
- `Menu.java` : menu principal.
- `GameScreen.java` : exploration, combat, affichage et progression.
- `LevelFactory.java` : generation deterministe des labyrinthes.
- `Enemy.java` : types, statistiques et attaques des ennemis.
- `Weapon.java` : armes du joueur.
- `GameAssets.java` et `PixelArt.java` : textures, sprites et animations.
- `build.gradle` : dependances LibGDX et configuration Gradle.

## Progression et ennemis

La partie comporte **20 niveaux**. Chaque niveau utilise une graine differente
pour generer un labyrinthe de catacombes dont le chemin de la sortie reste
accessible. Les ennemis deviennent plus nombreux au fil de la progression :

- **Chevalier** : combat au corps a corps.
- **Archer** : attaque a distance avec un projectile.
- **Mage** : tir magique plus puissant et a plus longue portee.
- **Boss** : beaucoup de points de vie et des coups puissants. Un boss attend
  le joueur tous les cinq niveaux, y compris au dernier.

Le deplacement du heros est ralenti par rapport au premier prototype. Le sprite
de l'orc s'anime pendant la marche et possede une pose d'attaque.

## Commandes

- Deplacement : fleches ou **ZQSD**.
- Attaque : **Espace**.
- Choisir une arme debloquee : **1**, **2** ou **3**.
- Recommencer : **R**. Apres une victoire ou une defaite, **Espace** fonctionne
  aussi.
- Revenir au menu : **Echap**.