# Utilise une image Node officielle, version 18 stable
FROM node:18

# Définit le dossier de travail dans le conteneur
WORKDIR /app

# Copie les fichiers package.json et package-lock.json (ou yarn.lock)
COPY package*.json ./

# Installe les dépendances
RUN npm install

# Copie le reste des fichiers du projet dans le conteneur
COPY . .

# Expose le port que ton app utilise (modifie si besoin)
EXPOSE 3000

# Commande pour démarrer ton app
CMD ["node", "index.js"]
