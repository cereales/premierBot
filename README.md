# PremierBot
Ce projet utilise l'API JDA.

## Installation
Après avoir cloné le git, lancer la compilation du projet à l'aide de l'outil gradel.
```
./gradlew build
```

Insérer le token du bot dans le fichier `src/main/java/fr/PrivateTokenised.java`.

## Création du fichier .jar
Utiliser IntelliJ pour compiler les sources.
Ouvrir un nouveau projet. Cocher la case `Use auto-import`.

Dans `File > Project Structure (Crtl + Alt + Maj + S) > Artifacts`, créer un nouvel Artifact `JAR > From modules with dependencies...`.
Ajouter la Main Class et modifier le path du META-INF de `src/main/java` à `src/main/ressource`.

Ensuite, `build > build Artefacts`.

Pour lancer le programme, `java -jar JDA.jar`.

