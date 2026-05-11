# XML Studio

A production-ready Android application for importing, rendering, and creating Android XML layout files.

## Features

- **Import XML** — Pick any `.xml` file from device storage and see it rendered as a real native Android UI instantly
- **Create XML** — Full-featured code editor powered by [Sora Editor](https://github.com/Rosemoe/sora-editor) with:
  - XML syntax highlighting (TextMate grammar / Darcula theme)
  - Line numbers
  - Undo / Redo
  - Auto-indentation
  - Live split-panel preview
- **XML Render Engine** — Modular runtime renderer supporting:
  - `LinearLayout`, `RelativeLayout`, `FrameLayout`
  - `ScrollView`, `HorizontalScrollView`
  - `TextView`, `Button`, `EditText`, `ImageView`
  - `CheckBox`, `RadioButton`, `Switch`, `ProgressBar`
  - All common attributes: `layout_width`, `layout_height`, `padding`, `margin`, `gravity`, `textSize`, `textColor`, `background`, etc.
- **Material Design 3** with full dark mode support
- **Smooth screen transitions** and entrance animations

## Architecture

```
app/src/main/java/com/xmlstudio/app/
├── MainActivity.kt              # Landing screen
├── models/
│   └── XmlNode.kt               # Data model + ParseResult sealed class
├── parser/
│   └── XmlParser.kt             # XmlPullParser-based recursive parser
├── renderer/
│   ├── AttributeParser.kt       # dp/sp/px/color/gravity parsing
│   ├── ComponentFactory.kt      # Maps XML tags → native Android Views
│   └── XmlRenderer.kt           # Orchestrates parse + render
├── editor/
│   ├── EditorActivity.kt        # Sora Editor integration + live preview
│   └── EditorViewModel.kt       # Editor state
└── export/
    ├── ExportActivity.kt        # File picker + render display
    └── ExportViewModel.kt       # Coroutine-based async parsing
```

## Build Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Gradle 8.7
- Android SDK 34

## Building

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```

## CI/CD

GitHub Actions workflow at `.github/workflows/android.yml` automatically:
- Builds both debug and release APKs on every push
- Caches Gradle dependencies
- Uploads APK artifacts (retained 30 days)
- Runs unit tests

## Dependencies

| Library | Purpose |
|---|---|
| Material3 | UI components & theming |
| Sora Editor | Professional code editor |
| AndroidX Lifecycle | MVVM (ViewModel + LiveData) |
| Kotlinx Coroutines | Async XML parsing |

## Minimum SDK

API 24 (Android 7.0 Nougat)
