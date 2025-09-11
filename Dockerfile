# Étape de build
FROM gradle:8.5.0-jdk17 AS builder

# Définir le répertoire de travail
WORKDIR /home/gradle/project

# Copier le code source dans le conteneur
COPY . .

# Mettre à jour le lock Kotlin/JS avant compilation
RUN gradle kotlinUpgradePackageLock --no-daemon

# Compiler le projet
RUN gradle build --no-daemon

# Étape de production
FROM openjdk:17

# Définir le répertoire de travail
WORKDIR /app

# Copier le JAR compilé depuis l'étape de build
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

# Exposer le port (à ajuster si besoin)
EXPOSE 8080

# Lancer l'application
CMD ["java", "-jar", "app.jar"]