# Étape de build
FROM gradle:8.5.0-jdk17 AS builder

WORKDIR /home/gradle/project
COPY . .

# Compile uniquement le shadowJar (plus rapide que tout le build)
RUN gradle jvmShadowJar --no-daemon

# Étape de prod
FROM openjdk:17
WORKDIR /app

# Copier le fat JAR (shadowJar avec Main-Class et dépendances)
COPY --from=builder /home/gradle/project/build/libs/*-all.jar app.jar

EXPOSE 8080

# Lancer l'application
CMD ["java", "-jar", "app.jar"]