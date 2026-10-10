# Java Games

Application de jeux 2D realisee avec Java et LibGDX. Le menu permet de choisir
parmi les jeux disponibles : **Les Catacombes** et le prototype **Jeu Zombie**.
Les personnages et les decors sont des sprites pixel art generes par le jeu :
aucun telechargement d'images supplementaires n'est necessaire.
La musique d'ambiance et les bruitages retro sont egalement inclus dans le
projet et fonctionnent hors ligne.

## Menu des jeux

- **Les Catacombes** : jeu disponible, selectionnable avec les fleches puis
  **Entree** ou **Espace**, ou en cliquant sur sa carte.
- **Jeu Zombie** : simulation d'une ville avec trois cartes. Avant la partie,
  choisis la carte, de 1 a 40 zombies au depart, et leur mode d'apparition :
  regroupes a un carrefour ou disperses dans la ville. Les zombies accelerent,
  errent individuellement lorsqu'ils ne chassent pas et poursuivent les civils
  visibles. Lors d'une attaque, le civil est immobilise ; un seul zombie a la
  fois le transforme apres un bond puis une seconde passee aupres de lui. Les
  civils tentent de fuir ; les batiments bloquent les deplacements et la ligne
  de vue. Le joueur peut selectionner des civils par rectangle et leur donner
  un ordre de deplacement.
  Une ambiance musicale apocalyptique accompagne la simulation.

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
- `Menu.java` : menu de selection des jeux.
- `GameScreen.java` : exploration, combat, affichage et progression.
- `ZombieScreen.java` : configuration, cartes, ville et simulation des groupes.
- `LevelFactory.java` : generation deterministe des labyrinthes.
- `Enemy.java` : types, statistiques et attaques des ennemis.
- `Weapon.java` : armes du joueur.
- `GameAssets.java` et `PixelArt.java` : textures, sprites et animations.
- `GameAudio.java` et `src/main/resources/audio/` : musiques d'ambiance et
  bruitages des pas, attaques, impacts, degats et objets ramasses.
- `build.gradle` : dependances LibGDX et configuration Gradle.

## Progression et ennemis

La partie comporte **20 niveaux**. Chaque niveau genere un labyrinthe de
catacombes de **31 x 23 cases**, avec une sortie dont le chemin reste
accessible. La camera reste centree sur l'orc et zoome sur sa position ; les
deplacements du heros et des ennemis sont interpoles pour un defilement fluide.
Le rendu du jeu est synchronise avec le rafraichissement de l'ecran.
La vision est limitee a six cases et les murs bloquent la vue.

Les ennemis ne se deplacent vers l'orc et n'attaquent que lorsqu'ils le voient
dans leur champ de vision. Un ennemi qui le perd de vue cesse de le poursuivre.
Leur portee de vue depend du type :

- **Chevalier** : combat au corps a corps.
- **Archer** : attaque a distance avec un projectile et un champ de vision etendu.
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

## Simulation Zombie

- Avant de lancer : fleches gauche/droite pour choisir la carte, haut/bas pour
  regler le nombre de zombies, **Tab** pour changer leur formation, **Entree**
  pour lancer. La configuration peut aussi se faire a la souris.
- Dans la ville : molette ou **+/-** pour zoomer jusqu'au niveau de la rue,
  **Echap** pour revenir au menu. Le HUD reste en haut, hors de la zone de jeu.
  Clic-glisse gauche pour selectionner les civils, clic droit pour leur donner
  un ordre ; le bouton milieu de la souris deplace la camera.