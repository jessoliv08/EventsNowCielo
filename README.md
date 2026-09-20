# EventsNow Cielo 🎫

An Android application for event listing, searching, and ticket purchasing, integrated with the **Cielo Smart (LIO)** POS terminal.

---

## Execution Instructions

### 1. Cielo LIO SDK Configuration

Before building the project, place the Cielo LIO SDK artifacts in the `app/libs/` directory:

- Download the SDK binaries from the official sample repository: [LIO SDK Sample Integração Local](https://github.com/DeveloperCielo/LIO-SDK-Sample-Integracao-Local)
- Required files copied from the sample `app/libs/` directory:
    - `order-manager-*.aar`
    - `orders-domain-*.aar`
    - `event-tracker-*.aar`
    - Any required companion `.jar` files
- Update `CREDENTIALS_CLIENT_ID` and `CREDENTIALS_ACCESS_TOKEN` in `app/build.gradle.kts` using your **Portal do Desenvolvedor** credentials.
- Install the **Cielo LIO Emulator** on your test device or Android Emulator to exercise checkout flows: [Cielo LIO Manual & Emulator Download](https://developercielo.github.io/manual/lio-local).

### 2. API Data Source (Important Notice)

> ⚠️ **Attention (Mock API - MyJSON):**
> The application's remote database is temporarily hosted on [myjson.online](https://myjson.online/). Because the platform expires endpoints after **8 days**, if the current link is inactive, generate a new public JSON using the following schema:

```json
{
  "events": [
    {
      "id": "evt_101",
      "title": "Rock Festival 2026",
      "imageUrl": "[https://picsum.photos/seed/evt_101/400/250](https://picsum.photos/seed/evt_101/400/250)",
      "date": "2026-09-17",
      "time": "20:00",
      "priceInCents": 15000,
      "description": "High-energy rock concert featuring top national and international bands.",
      "location": "Allianz Parque, São Paulo",
      "category": {
        "id": "cat_1",
        "name": "Concerts"
      }
    }
  ]
}
```

After generating the new URL, update the BASE_URL constant in:
com.example.eventsnowcielo.di.NetworkModule

## Architeture

The project follows Clean Architecture principles aligned with modern Android development guidelines:

- Layered Architecture:
    - UI (Presentation): Built with Jetpack Compose using Unidirectional Data Flow (UDF) and StateFlow for state management and reactivity.
    - Domain: Pure Kotlin business rules (Use Cases and Repository interfaces). Completely isolated from frameworks and the data layer.
    - Data: Manages remote and local data sources, maps DTOs to domain models, and implements domain repositories.
- Package by Feature: Code is organized into feature packages (features/events, features/printer, etc.), ensuring high cohesion and maintainability.

## Libraries

### 1. Koin Annotations / Koin:
- Lightweight, pragmatic Dependency Injection framework for Kotlin. Using Koin Annotations automates dependency generation (`@Single`, `@Factory`), eliminating boilerplate code and lowering build overhead compared to Dagger/Hilt.
### 2. Retrofit 2 & OkHttp:
- Industry standard for REST API consumption in Android. Provides native integration with Kotlin Coroutines, clean JSON serialization, and effortless logging/interceptor configuration.
### 3. Room Database:
- Official Google abstraction over SQLite. Used for local persistence (saved/bookmarked events), providing compile-time SQL validation and reactive stream support with Kotlin Flow.
### 4. MockK & KotlinX Coroutines Test:
- Enables idiomatic Kotlin unit testing, simplify mock creation (such as capturing listener callbacks and suspend functions) for UseCases and Repositories.
### 5. Cielo LIO SDK Libraries
Place the Cielo LIO Order Manager SDK artifacts in this directory before building.
Download them from the official sample project:
[https://github.com/DeveloperCielo/LIO-SDK-Sample-Integracao-Local](https://github.com/DeveloperCielo/LIO-SDK-Sample-Integracao-Local)

Typical files copied from the sample `app/libs` folder:

- `order-manager-*.aar`
- `orders-domain-*.aar`
- `event-tracker-*.aar`
- Any required companion `.jar` files

After copying the files, update `CREDENTIALS_CLIENT_ID` and `CREDENTIALS_ACCESS_TOKEN`
in `app/build.gradle.kts` with your Portal do Desenvolvedor credentials.

Install the Cielo LIO Emulator on the test device to exercise checkout flows:
[https://developercielo.github.io/manual/lio-local](https://developercielo.github.io/manual/lio-local)

## Integration with the Cielo Smart POS terminal is handled via the Cielo LIO local SDK:

* Lifecycle Management: Cielo's OrderManager is initialized using application context and dynamically bound/unbound during transaction flows.

* Receipt / Ticket Printing: The data layer abstracts Cielo's PrinterManager through the TicketPrinterRepository interface, emitting reactive states (InProgress, Success, OutOfPaper, Error) to the presentation layer.

* Asynchronous Handling: SDK listener callbacks are wrapped into predictable Kotlin Flow and Coroutine states.

## Trade-offs
* Mocking vs. Real Backend: Due to scope and delivery constraints, a temporary JSON provider (MyJSON) was selected instead of a full custom backend service, prioritizing application architecture polish and terminal integration.
* Single-Module Gradle Structure: While Clean Architecture separation is enforced logically, keeping code within a single Gradle module simplified the build pipeline and dependency management.
* Context Injection in Printer Repository: To decouple the hardware layer while using LIO hardware APIs, PrinterManager is encapsulated using a lazy delegate, allowing MockK unit testing on the JVM without requiring Android framework runtime stubs.

## Future Improvements (With More Time)
* UI & Instrumented Tests: Implement UI testing using Compose Testing API and end-to-end integration tests for purchasing workflows.
* Offline-First Capabilities with Room: Implement full offline caching strategy where local Room data serves as the single source of truth, synchronizing when network availability changes.
* API & Pagination: Add support for server-side pagination and date filtering on the events endpoint. Integrate Android’s Paging 3 library into the Events screen to handle infinite scrolling smoothly.
* Filter & Search UX: Implement ticket filtering options and display dates prominently at the top of the event listing.
* CI/CD Pipeline: Set up GitHub Actions for automated unit test execution and static code analysis on every Pull Request.
