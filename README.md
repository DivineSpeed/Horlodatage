# Algorithmiques Réparties | Horlodatage 

Ce dépôt contient des implémentations en Java et C de trois horloges logiques fondamentales utilisées dans les systèmes distribués :

1. **Horloge Scalaire (Horloge de Lamport)**
2. **Horloge Vectorielle**
3. **Horloge Matricielle**

## Description du Projet

Ce projet démontre comment les horloges logiques peuvent être utilisées pour établir des relations causales entre les événements dans les systèmes distribués. Chaque implémentation simule plusieurs processus communiquant via l'échange de messages, chaque processus maintenant sa propre horloge logique.

## Réalisation des Exigences du Projet

Conformément aux exigences demandées, ce projet implémente :

- **3 systèmes d'horlodatage** complets (scalaire, vectoriel et matriciel) en C et en Java
- **4 processus** simulés communiquant entre eux
- **Au moins 5 instructions locales** dans chaque processus
- **Au moins 4 émissions de messages** utilisant les sockets TCP
- **Affichage des mises à jour** à chaque modification de l'horloge
- **Respect des règles** concernant les mises à jour d'horloges (non comptabilisées comme instructions locales)

**Bonus** :
- Une interface graphique JavaFX pour faciliter la visualisation des échanges entre processus et des mises à jour d'horloges
- Une simulation de pannes de processus dans l'implémentation Java pour tester la robustesse du système
- Un mode interactif via ligne de commande dans l'implémentation C permettant à l'utilisateur de déclencher des événements manuellement

## Implémentations

### Implémentation Java

L'implémentation Java fournit un framework de simulation simple avec :

- Une interface abstraite `Horloge` avec des implémentations concrètes pour chaque type d'horloge
- Une classe `Processus` qui gère la communication et les événements
- Une interface graphique pour faciliter la simulation
- Simulation de pannes pour tester la résilience du système

#### Fonctionnalités :
- Supporte 4 processus par défaut
- Délais d'événements et de messages configurables
- Gestion propre de l'arrêt
  
#### Comment exécuter :

1. Ouvrez le projet dans IntelliJ IDEA
2. Ajoutez la bibliothèque JavaFX à la structure du projet
3. Exécutez le fichier `src/gui/LanceurSimulateurHorloge.java`

C'est tout ! L'interface graphique de simulation se lancera automatiquement.

### Implémentation C

L'implémentation C comprend :

- Des exécutables séparés pour chaque type d'horloge
- Une implémentation multi-thread utilisant pthreads
- Communication entre processus basée sur les sockets
- Mode interactif via ligne de commande après l'exécution automatique initiale

**Note importante** : L'implémentation C est conçue pour fonctionner sous Linux/Unix, car elle utilise des fonctionnalités spécifiques à ces systèmes comme les sockets POSIX, et pthreads.

#### Fonctionnalités :
- Gestion des erreurs avec tentatives répétées pour les opérations réseau
- Gestion des signaux pour une terminaison propre
- Opérations d'horloge thread-safe
- Mode interactif permettant à l'utilisateur de déclencher des événements locaux et d'envoyer des messages manuellement

#### Comment compiler :

1. Naviguez vers le répertoire C :
```
cd C/Horlodatage_C
```

2. Compilez avec Make :
```
make
```

3. Exécutez une implémentation d'horloge spécifique :
```
./horloge_scalaire     # Pour l'Horloge Scalaire
./horloge_vectorielle  # Pour l'Horloge Vectorielle
./horloge_matricielle  # Pour l'Horloge Matricielle
```

Le programme commence par exécuter automatiquement 5 événements locaux et envoie des messages, puis passe en mode interactif où vous pouvez déclencher manuellement des événements supplémentaires.

## Explication des Horloges Logiques

### Horloge Scalaire (Horloge de Lamport)
- Simple compteur entier
- Incrémenté à chaque événement local
- À la réception d'un message : horloge = max(horloge_locale, horloge_reçue) + 1
- Fournit la relation "s'est produit avant" mais ne détecte pas la concurrence

### Horloge Vectorielle
- Tableau d'entiers (un par processus)
- Le processus local incrémente sa propre position lors d'événements locaux
- À la réception d'un message : maximum composant par composant avec le vecteur reçu
- Peut détecter des événements concurrents lorsqu'aucun vecteur ne "domine" l'autre

### Horloge Matricielle
- Matrice où chaque ligne est une horloge vectorielle (une ligne par processus)
- Fournit des informations de causalité transitive
- Plus complexe mais offre une connaissance complète des dépendances entre processus
