# Anno1800 Gradle Project

Ein Gradle-Projekt mit Java 21 LTS.

## Voraussetzungen

- Java 21 LTS
- Gradle 8.5 (wird über den Wrapper bereitgestellt)

## Projekt bauen

```bash
# Windows
gradlew.bat build

# Linux/Mac
./gradlew build
```

## Anwendung ausführen

```bash
# Windows
gradlew.bat run

# Linux/Mac
./gradlew run
```

## Tests ausführen

```bash
# Windows
gradlew.bat test

# Linux/Mac
./gradlew test
```

## Projektstruktur

```
Anno1800/
├── src/
│   ├── main/
│   │   └── java/        # Java-Quellcode
│   └── test/
│       └── java/        # Java-Tests
├── build.gradle.kts     # Build-Konfiguration
├── settings.gradle.kts  # Projekt-Einstellungen
└── gradlew[.bat]        # Gradle Wrapper
```
