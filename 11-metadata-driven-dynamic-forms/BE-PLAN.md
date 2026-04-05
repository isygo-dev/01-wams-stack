# Backend MetaData-Driven Dynamic Form System

**Plan de Réalisation – Partie Backend**  
**Java 17 + Spring Boot 3.x**

**Version** : 1.0  
**Date** : Avril 2026  
**Auteur** : Senior Backend Architect  
**Objectif** : Fournir un plan clair, moderne, lisible et maintenable pour développer un système de génération de
métadonnées de formulaires entièrement piloté par annotations.

---

## 1. Overall Goals

Construire un backend **annotation-driven** puissant qui permet de :

- Scanner automatiquement des classes Java (DTOs ou classes dédiées View) via reflection.
- Générer un JSON metadata hiérarchique, riche et récursif.
- Permettre au frontend (Angular ou autre) de rendre dynamiquement des formulaires complexes sans hardcoding.
- Intégrer nativement Jakarta Validation pour une validation cohérente client/serveur.
- Assurer une excellente **lisibilité du code**, une **maintenabilité élevée** et un **design moderne**.

**Principes directeurs** :

- Annotation-first & reflection-based
- Séparation claire des préoccupations
- Extensibilité sans modification du core
- Performance et caching intelligents
- Code propre, testable et documenté

---

## 2. Principes de Design Moderne, Lisibilité & Maintenabilité

- Utilisation intensive de **records** Java 17 pour les modèles de métadonnées internes (immutables, concis).
- Architecture en **hexagonal / clean architecture** (ports & adapters).
- Séparation stricte entre :
    - Annotations (API publique pour les développeurs métier)
    - Modèles internes de métadonnées
    - JSON exposé au frontend
- Utilisation de **Spring Boot 3.x** avec configuration moderne (constructor injection, @ConfigurationProperties).
- **Strong typing** partout où possible, avec fallback JSON flexible.
- Tests unitaires et d’intégration prioritaires (TDD recommandé pour le metadata engine).
- Documentation vivante via Swagger + AsciiDoc / Markdown.
- Pas de reflection "chaude" en production → pré-scanning au démarrage + caching.

---

## 3. Phases du Projet

### Phase 1 : Préparation & Conception (1-2 semaines)

**Objectifs** : Définir des bases solides, lisibles et extensibles.

**Activités principales** :

- Conception finale des annotations (`@FormView`, `@FormField`, `@FormList`, `@FormTable`, `@FormOption`, `@DependsOn`,
  `@Conditional`, etc.).
- Définition précise du **JSON Schema** hiérarchique et récursif.
- Conception des modèles internes (records) de métadonnées.
- Stratégie de mapping Jakarta Validation → MetaData.
- Définition de la stratégie d’i18n (labels, messages, options).
- Choix des outils : Caffeine/Redis pour caching, Jackson pour sérialisation.
- Rédaction des trois exemples JSON demandés (simple, selections, complexe avec nested + table).

**Livraisons** :

- Document de design des annotations et du JSON Schema.
- Diagrammes (C4 Model ou UML léger).
- Stratégie de caching et versioning.

**Focus modernité** : Utiliser des records et sealed interfaces pour les types de champs.

---

### Phase 2 : Core MetaData Engine (3-4 semaines)

**Objectifs** : Construire le cœur du système de manière propre et performante.

**Composants clés** :

- `FormMetaDataRegistry` – Enregistrement et pré-chargement des vues au démarrage.
- `FormMetaDataService` – Service principal de génération.
- `AnnotationProcessor` – Lecture et interprétation des annotations via reflection (une seule fois).
- `ValidationMapper` – Conversion automatique des contraintes Jakarta Validation.
- `ConditionalEvaluator` – Gestion des règles conditionnelles (visibility, enabled, required).
- `OptionsResolver` – Support static, enum, API, dependent.
- `DefaultValueMerger` – Fusion des valeurs par défaut et des données existantes (mode edit).

**Améliorations modernité & maintenabilité** :

- Utilisation de **records** pour tous les DTOs internes de métadonnées.
- Pattern Visitor ou Strategy pour le traitement des différents types de champs.
- Caching agressif avec `@Cacheable` et clés composites (`viewName:mode:version:locale`).
- Pré-scanning au démarrage via `ApplicationReadyEvent`.

**Livraisons** :

- MetaData engine complet et testable.
- Cache fonctionnel.
- Tests unitaires couvrant > 90% du core.

---

### Phase 3 : API Layer (2 semaines)

**Objectifs** : Exposer des endpoints REST clairs et robustes.

**Endpoints principaux** :

- `GET /api/forms/{viewName}/metadata?mode=create|edit&entityId=...`
- `POST /api/forms/{viewName}/submit`

**Composants** :

- `FormController` (@RestController)
- `FormSubmissionService` – Validation serveur + exécution des actions.
- `FormDataBinder` – Binding et merge des données.
- `GlobalExceptionHandler` (@ControllerAdvice) pour erreurs field-mapped.

**Design moderne** :

- Utilisation de **ResponseEntity** et records pour les réponses.
- Validation avec `@Valid` + `BindingResult` enrichi.
- OpenAPI 3 avec annotations SpringDoc pour documentation interactive.
- Support CORS et security moderne (Spring Security 6).

**Livraisons** :

- Endpoints fonctionnels avec tests d’intégration.
- Documentation Swagger complète.

---

### Phase 4 : Features Avancées & Polish (2-3 semaines)

**Activités** :

- Support complet i18n (MessageSource + LocaleResolver).
- Gestion avancée des listes/tables (actions per-item, bulk actions, pagination metadata).
- Mécanisme d’extensibilité (`CustomFieldHandler` registry).
- Support pour file uploads (futur-proofing).
- Versioning des vues (`@FormView(version = "1.2")`).
- Optimisations de performance pour vues très imbriquées.

**Focus maintenabilité** :

- Interfaces claires et abstractions pour chaque responsabilité.
- Configuration externe via `@ConfigurationProperties`.
- Logging structuré (MDC + structured JSON logging).

---

### Phase 5 : Tests, Optimisation & Documentation (1-2 semaines)

**Activités** :

- Tests unitaires (JUnit 5 + AssertJ + Mockito).
- Tests d’intégration (@SpringBootTest).
- Tests de performance (JMeter ou Gatling sur génération metadata).
- Audit de sécurité.
- Rédaction du **Developer Guide** (comment créer une nouvelle vue, best practices annotations).
- Configuration CI/CD friendly (GitHub Actions / GitLab CI).

**Livraisons** :

- Code avec couverture > 85%.
- Guide de développement complet.
- Rapport de performance et recommandations.

---

## 4. Stack Technique Recommandée

| Couche        | Technologie                          | Justification                                |
|---------------|--------------------------------------|----------------------------------------------|
| Langage       | Java 17                              | Records, sealed interfaces, pattern matching |
| Framework     | Spring Boot 3.3+                     | Modernité, AOT, GraalVM ready                |
| Validation    | Jakarta Bean Validation 3.0+         | Intégration native                           |
| JSON          | Jackson 2.17+                        | Performance & custom serializers             |
| Caching       | Caffeine (local) / Redis (distribué) | Haute performance                            |
| Documentation | SpringDoc OpenAPI                    | Documentation interactive                    |
| Testing       | JUnit 5 + Testcontainers             | Tests réalistes                              |
| Logging       | SLF4J + Logback avec JSON layout     | Observabilité moderne                        |

---

## 5. Stratégie de Caching & Performance

- Pré-chargement de toutes les métadonnées au démarrage.
- Cache par `viewName + mode + locale + version`.
- Invalidations sélectives sur redéploiement ou via actuator endpoint.
- Lazy loading des options lourdes (API calls).

---

## 6. Meilleures Pratiques Recommandées

- Annotations légères, composables et bien nommées.
- Modèles internes en **records** (immutables).
- Éviter la reflection en runtime → tout pré-calculé.
- Code lisible : méthodes courtes, noms explicites, peu de commentaires (code self-documenting).
- Utiliser des builders ou records canoniques pour construire le metadata JSON.
- Séparer les préoccupations métier des préoccupations UI.

---

## 7. Risques & Mitigations

| Risque                         | Mitigation                                 |
|--------------------------------|--------------------------------------------|
| Performance reflection         | Pré-scanning + caching agressif            |
| Complexité nested/conditionals | Tests exhaustifs + schéma JSON strict      |
| Évolution des annotations      | Design composable + versioning             |
| i18n des options dynamiques    | Services dédiés injectables                |
| Maintenance à long terme       | Clean architecture + records + tests forts |

---

## 8. Planning Estimé

- **Phase 1** : 1-2 semaines
- **Phase 2** : 3-4 semaines
- **Phase 3** : 2 semaines
- **Phase 4** : 2-3 semaines
- **Phase 5** : 1-2 semaines

**Durée totale estimée** : **9 à 13 semaines** pour une version production-ready (MVP complet + features avancées).

---

**Conclusion**

Ce plan privilégie un **design moderne**, une **lisibilité exceptionnelle** et une **maintenabilité durable**.  
Il transforme un système potentiellement complexe en une solution élégante, extensible et agréable à maintenir pour les
équipes de développement.

---

**Prochaines étapes recommandées** :

1. Valider ce plan avec l’équipe.
2. Démarrer par la Phase 1 (design des annotations et JSON Schema).
3. Mettre en place le squelette du projet Spring Boot avec les bonnes pratiques modernes.

Besoin d’un template de projet Spring Boot initial ou d’un approfondissement sur une phase spécifique ?