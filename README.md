# Heart Rate Sender - Pixel Watch

**Heart Rate Sender** est une application pour Wear OS (Google Pixel Watch) permettant de :

- Lire en temps réel la fréquence cardiaque de l'utilisateur.
- Faire tourner un service de santé sécurisé (`FOREGROUND_SERVICE_TYPE_HEALTH`) en arrière-plan.
- Permettre à l'utilisateur de démarrer/arrêter manuellement la mesure.
- Préparer l'envoi futur des données vers un serveur externe pour ensuite l'afficher sur mon téléphone perso.

---

## Objectifs principaux atteints

- [x] Lecture du Rythme Cardiaque via `Health Services API`.
- [x] Interface Wear OS avec boutons "Démarrer" et "Arrêter".

---

## Prochaines améliorations prévues

### 1. Notification interactive
- Ajouter un bouton "Arrêter" directement dans la notification.
- Rendre la notification cliquable pour rouvrir l'application facilement.

### 2. Transmission des données
- Envoyer les données de fréquence cardiaque en direct à une API via HTTP.
- Permettre une visualisation du Rythme Cardiaque en live via une PWA (Qui sera réalisé en Vue.js)

---

## Stack Technique
- **Langage** : Kotlin
- **Cible** : Wear OS 4 (Pixel Watch, compatibles Android 14+)
- **API de santé** : `androidx.health:health-services-client`
- **Interface** : Compose for Wear OS
- **Services** : Foreground Service Health (`ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH`)

---

## 🧡 Auteur
- On va pas se le cacher, je suis très honnête avec vous, je suis pas développeur Kotlin ni Android, donc c'est clairement du ChatGPT. Merci à lui il a fait 90% du taff. Je me permets de m'octroyer les 10% restants car pour l'instant il est pas prêt de nous remplacer 🤡

