# SEG 3502 — Lab 8 : Services Web GraphQL

Application pédagogique de gestion des employés, composée d'un **client Angular/TypeScript**, d'une **API GraphQL Spring Boot/Kotlin** et d'une base de données **MongoDB**. Le client permet d'ajouter un employé et d'afficher la liste des employés enregistrés.

## Structure

```text
client/   Application Angular et Apollo GraphQL
server/   API GraphQL Spring Boot / Kotlin et persistance MongoDB
```

Les fichiers complétés pour le laboratoire sont :

- `server/src/main/resources/graphql/employee.graphqls` : types, requête `employees` et mutation `newEmployee`.
- `server/src/main/kotlin/seg3x02/employeeGql/resolvers/EmployeesResolver.kt` : résolveurs de la requête et de la mutation.

## Prérequis

- JDK 21 (`java -version`)
- Node.js et npm compatibles avec Angular 18
- Docker Desktop en cours d'exécution

## 1. Démarrer MongoDB

Dans un terminal :

```powershell
docker run -d -p 27017:27017 --name mongodb mongo
```

Si le conteneur `mongodb` existe déjà, mais est arrêté :

```powershell
docker start mongodb
```

Vérifier avec `docker ps` que MongoDB écoute sur le port **27017**. La base utilisée est `employees-mongo`.

## 2. Lancer le serveur GraphQL

Depuis la racine du projet, sous Windows :

```powershell
cd server
.\gradlew.bat bootRun
```

Sous macOS ou Linux :

```bash
cd server
./gradlew bootRun
```

Garder le terminal ouvert. L'API est disponible à **http://localhost:9000/graphql**. L'interface GraphiQL est configurée à **http://localhost:9000/graphiql** (son chargement peut dépendre des ressources Web externes).

## 3. Lancer le client Angular

Dans un **autre terminal**, depuis la racine du projet :

```powershell
cd client
npm ci
npx ng serve
```

Ouvrir **http://localhost:4200**.

Le client utilise **Angular 18** et **Apollo Angular 7.0.2**, compatibles entre eux. Le fichier `package-lock.json` est synchronisé avec cette dépendance.

## 4. Tester l'application

1. Ouvrir le formulaire **Add Employee**.
2. Saisir un nom, une date de naissance, une ville, un salaire, un genre et un courriel fictifs, puis cliquer sur **Submit**.
3. Vérifier que l'employé apparaît dans **Employee List**.
4. Actualiser la page et vérifier que l'employé est toujours affiché.

Pour tester directement la requête GraphQL dans un PowerShell distinct :

```powershell
Invoke-RestMethod -Uri "http://localhost:9000/graphql" -Method Post -ContentType "application/json" -Body '{"query":"{ employees { id name city email } }"}' | ConvertTo-Json -Depth 10
```

Si la base est vide, la requête retourne `{"data":{"employees":[]}}`.

## Fonctionnalités

- **Query `employees`** : récupérer les employés enregistrés.
- **Mutation `newEmployee`** : ajouter un employé dans MongoDB et retourner l'enregistrement créé.

> La configuration de sécurité de ce laboratoire est destinée uniquement au développement local, et non à un déploiement en production.
