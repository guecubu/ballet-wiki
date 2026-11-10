# En Pointe

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![MVVM](https://img.shields.io/badge/Architecture-MVVM-blue?style=flat-square)](https://developer.android.com/topic/architecture)
[![Room](https://img.shields.io/badge/Database-Room-00599C?style=flat-square&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Retrofit](https://img.shields.io/badge/Networking-Retrofit-20beff?style=flat-square)](https://square.github.io/retrofit/)

> *Stories Behind the Steps*

**En Pointe** (also known as Ballet Reference) is a comprehensive Android application designed as a
reference catalog for classical ballet masterworks. It provides ballet enthusiasts, students, and
performing arts professionals with detailed insights into classical repertoires, historical context,
composers, choreographers, multimedia galleries, and personalized notes.

---

## 📱 Key Features

- **Repertoire Catalog**: Explore an extensive catalog of classical ballet works with advanced
  search and filtering capabilities (by tradition, era, genre, school, and premiere date).
- **Comprehensive Detail View**: Access rich metadata for each ballet work, including synopsis,
  historical background, key composers, choreographers, and media galleries.
- **Favorites Management**: Bookmark your favorite ballet pieces for quick access.
- **Personal Notes**: Create, edit, and organize personal notes and ideas related to specific ballet
  works, securely stored locally.
- **Bilingual & Original Text Support**: View original source texts alongside localized
  representations without altering user-created content.
- **Persistent Preferences**: Remembers user preferences such as last viewed items and display
  settings using `SharedPreferences`.

---

## 🛠️ Architecture & Tech Stack

The application follows clean architectural principles and industry best practices for Android
development:

- **Language**: [Kotlin](https://kotlinlang.org)
- **UI Framework**: Android Views, XML Layouts,
  and [ViewBinding](https://developer.android.com/topic/libraries/view-binding) (No Jetpack
  Compose).
- **Architecture Pattern**: Model-View-ViewModel (MVVM) with clean separation of concerns.
- **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room)
  (SQLite) for storing works, favorites, notes, and metadata.
- **Local Preferences**: `SharedPreferences` for user session settings and last viewed work
  tracking.
- **Networking**: [Retrofit](https://square.github.io/retrofit/) for REST API communication.
- **Asynchronous Operations**: Kotlin Coroutines for non-blocking background operations.
- **Design System**: Material Design components customized with bespoke typography (Bodoni Moda &
  Plus Jakarta Sans) and color palettes.

---

## 📂 Project Structure

```text
com.gf.ballet_wiki
├── activities/       # Core Activities (MainActivity, DetailActivity, NotesActivity)
├── adapters/         # RecyclerView Adapters with lambda callbacks
├── data/
│   ├── local/        # Room Database, DAOs, and Entities
│   ├── remote/       # Retrofit API services and DTOs
│   └── repository/   # Repository pattern implementation
├── fragments/        # Fragment views (Catalog, Notes)
└── utils/            # Image loaders, session managers, extensions, and translation helpers
```

---

## 📋 Academic & Technical Requirements

- **Activities**: Multi-activity architecture with intent extra passing and retrieval.
- **Interactive Dialogs**: Implements `AlertDialog` for user confirmation and actions.
- **Persistence**: Hybrid local storage using Room SQLite and `SharedPreferences`.
- **Networking**: RESTful API integration via Retrofit.
- **UI Components**: `RecyclerView` powered by lambda/callback click listeners, Material components,
  and custom AppBar menus.
- **Localization**: Spanish localization as the default language with extensible string resources.

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (Arctic Fox or newer recommended)
- Android SDK (minSdk 24+, targetSdk 34+)
- JDK 17 or higher

### Installation & Execution

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/en-pointe.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and build the project.
4. Run the app on an Android Emulator or physical device.

---

## 📄 License

This project is developed as an academic and reference MVP project. See [LICENSE](LICENSE) for
details.
