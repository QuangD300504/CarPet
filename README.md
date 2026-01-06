# CarPet

An Android application built with Jetpack Compose following Clean Architecture principles.

## 🚀 Features

- Modern UI built with Jetpack Compose
- Clean Architecture with separation of concerns
- MVVM pattern with ViewModel and StateFlow
- Navigation using Jetpack Navigation Compose
- Material Design 3

## 📋 Requirements

- Android Studio Hedgehog | 2023.1.1 or later
- JDK 11 or higher
- Android SDK 24 (Minimum)
- Android SDK 36 (Target)

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: Clean Architecture (Domain, Data, Presentation layers)
- **State Management**: StateFlow, ViewModel
- **Navigation**: Jetpack Navigation Compose
- **Dependency Injection**: (To be implemented)
- **Build System**: Gradle with Kotlin DSL

## 📁 Project Structure

```
app/src/main/java/com/example/carpet/
├── data/                    # Data Layer
│   ├── datasource/         # Remote & Local data sources
│   ├── models/             # Data models (DTOs, entities)
│   └── repository/         # Repository implementations
│
├── domain/                 # Domain Layer (Business Logic)
│   ├── models/             # Domain models (business entities)
│   ├── repository/         # Repository interfaces
│   └── usecases/          # Use cases (business operations)
│
├── presentation/           # Presentation Layer (UI)
│   ├── components/         # Reusable UI components
│   ├── navigation/         # Navigation setup
│   ├── screens/           # Screen composables
│   ├── theme/             # Theme configuration
│   └── viewmodels/        # ViewModels
│
├── di/                    # Dependency Injection modules
└── utils/                 # Utility functions & extensions
```

## 🏗️ Architecture

The project follows **Clean Architecture** principles:

- **Domain Layer**: Contains business logic, use cases, and domain models. This layer is independent of frameworks.
- **Data Layer**: Handles data sources (remote/local) and implements repository interfaces defined in the domain layer.
- **Presentation Layer**: Contains UI components, ViewModels, and navigation logic.

## 🚦 Getting Started

1. Clone the repository
   ```bash
   git clone <repository-url>
   cd CarPet
   ```

2. Open the project in Android Studio

3. Sync Gradle files

4. Run the app on an emulator or physical device

## 📱 Screens

- **Login Screen**: User authentication interface
- **Home Screen**: Main screen after login (to be implemented)

## 🔧 Build

To build the project:

```bash
./gradlew build
```

To build an APK:

```bash
./gradlew assembleDebug
```

To install on a connected device:

```bash
./gradlew installDebug
```

## 🧪 Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

## 📝 License

[Add your license here]

## 👥 Contributors

[Add contributors here]

---

**Note**: This project is currently in development. More features and screens will be added in future updates.

