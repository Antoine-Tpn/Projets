# Projet Réseau de Capteurs sur Bus CAN

> Présentation du projet « Réseau de capteurs » (ENIB, cours de Vincent Kerhoas).
> Source : https://web.enib.fr/~kerhoas/iot/reseau-de-capteurs/

## 1. Objectif

Récupérer sur un **PC Host** les informations de plusieurs capteurs. Toutes les données transitent sur un **bus de terrain, le bus CAN**.

Les capteurs ne peuvent pas émettre directement sur le bus. Des microcontrôleurs **ST Nucleo** lisent la valeur de chaque capteur et la placent dans des **trames CAN**.

## 2. Le bus CAN en bref

Le **CAN (Controller Area Network)** est un bus série conçu dans les années 1980 pour l'automobile. Il est aujourd'hui très répandu dans l'industrie et l'embarqué.

- **Multi-maître** : chaque nœud peut émettre, et tous les nœuds reçoivent tous les messages.
- **Priorité par identifiant** : en cas d'émission simultanée, le message le plus prioritaire passe (arbitrage).
- **Deux fils** (paire différentielle) avec résistances de terminaison aux extrémités du bus.
- **Robuste** : détection et gestion d'erreurs intégrées, ce qui facilite le dépannage.
- **Extensible** : on ajoute un nœud sans modifier le reste du réseau, avec un câblage réduit.

### Principe physique

L'information est une **tension différentielle entre CAN_H et CAN_L**. Une perturbation électromagnétique touche les deux fils de la même façon, donc la différence de potentiel ne change pas : le bus résiste bien aux parasites.

### Format d'une trame de données

| Champ | Rôle |
|-------|------|
| **Bit de start** | Passage de 1 (repos) à 0 |
| **Identifiant** | Identifie le message et fixe sa priorité |
| **Champ de commande** | Nombre d'octets dans le champ de données |
| **Champ de données** | Les mesures transportées |
| **Champ de CRC** | Calcul sur les données pour détecter les erreurs de transmission |
| **Champ d'ACK** | Tout message doit être acquitté : le destinataire force le bus à 0 |

### Arbitrage

Quand le bus est libre, plusieurs nœuds peuvent vouloir émettre. Tous placent leur identifiant sur le bus. Le niveau 0 est dominant : un nœud qui tente de forcer le 1 mais entend un 0 sait qu'il a **perdu l'arbitrage** et se retire. Le message qui a l'identifiant le plus prioritaire passe donc sans être perdu.

### Filtrage et synchronisation

- **Filtrage** : tous les messages n'intéressent pas tous les nœuds. On configure dans le périphérique CAN les identifiants acceptables pour ne pas interrompre le microcontrôleur inutilement.
- **Bit stuffing** : la liaison est asynchrone, il faut donc des fronts réguliers pour resynchroniser les récepteurs. L'émetteur insère un bit de polarité inverse après 5 bits identiques ; le récepteur l'ignore.

## 3. Architecture du système

```
 Capteurs                Microcontrôleurs               Bus de terrain          Supervision
 ────────                ────────────────               ──────────────          ───────────
 Dynamixel + anémomètre ─> Carte Nucleo F103 n°1 ─┐
 Lumière/distance,                                ├──>  BUS CAN  ──> sonde ──>  PC Host
 pression, humidité     ─> Carte Nucleo F103 n°2 ─┤                  USB-CAN
 Accéléro + gyroscope   ─> Carte Nucleo F103 n°3 ─┘
```

| Carte | Rôle | Capteurs / actionneurs |
|-------|------|------------------------|
| **n°1** | Moteur et vent | Servomoteur **Robotis Dynamixel** (contrôle) et **anémomètre Somfy** (vitesse du vent) |
| **n°2** | Environnement | **ST VL6180X** (luminosité ou distance, bus I2C), **ST LPS22HB** (pression) et **ST HTS221** (humidité) |
| **n°3** | Mouvement | **Invensense MPU9250** (accéléromètre et gyroscope, bus I2C) |

## 4. Côté matériel et logiciel

- **Microcontrôleur** : carte Nucleo basée sur un STM32F103.
- **Transceiver CAN** : SN65HVD251, qui adapte les niveaux logiques du microcontrôleur à ceux du bus.
- **Driver CAN** : bibliothèque fournie (`drv_can.h`) pour initialiser le périphérique CAN du STM32 et gérer les interruptions, dont le Bus-off.
- **Capteurs I2C** : lus par les microcontrôleurs, puis les valeurs sont encapsulées dans les trames CAN.
- **Outils de développement** : STM32CubeIDE, et une sonde **PEAK PCAN-USB** pour observer et envoyer des trames depuis le PC.

## 5. Découpage du travail (parties du cours)

1. **Architecture du système** : schéma d'ensemble et répartition des cartes.
2. **Carte Nucleo F103** : prise en main, communication série par USB, fonctionnement du bus CAN et driver.
3. **Carte moteur Dynamixel / anémomètre** : commande du servomoteur, mesure de la vitesse du vent.
4. **Capteurs humidité, pression, luminosité, distance** : lecture via I2C.
5. **Centrale inertielle MPU9250** : lecture de l'accéléromètre et du gyroscope.
6. **Déroulement du projet** : étapes et objectifs.

## 6. Compétences mobilisées

- Programmation de microcontrôleurs STM32 en C
- Bus de terrain CAN : trames, identifiants, débit, terminaisons
- Bus I2C et UART
- Interfaçage de capteurs et d'actionneurs
- Acquisition, mise en forme et supervision de données de capteurs

## 7. Liens utiles

- Page du projet : https://web.enib.fr/~kerhoas/iot/reseau-de-capteurs/
- Architecture : https://web.enib.fr/~kerhoas/iot/reseau-de-capteurs/architecture/
- Carte Nucleo F103 et driver CAN : https://web.enib.fr/~kerhoas/iot/reseau-de-capteurs/carte-nucleo/
- Cours sur le bus CAN : https://web.enib.fr/~kerhoas/systemes-a-processeurs/peripheriques/bus-can/
