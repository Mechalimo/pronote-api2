# Utilise une image avec Java
FROM gradle:8.5.0-jdk17 AS builder

# Copie le code dans le conteneur
COPY . /home/gradle/project
WORKDIR /home/gradle/project

# Build le projet
RUN gradle build --no-daemon

# Étape de production
FROM openjdk:17

# Copie le JAR compilé
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

# Lance l'app
CMD ["java", "-jar", "app.jar"]

# Port exposé (ajuste si besoin)
EXPOSE 8080
