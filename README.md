<div align="center">

# 🏥 Clinic Manager

Desktop suite for running a modern medical practice. Clinic Manager bundles patient onboarding, doctor scheduling, visit management, and notification automation into a single JavaFX experience backed by SQLite.

</div>

---

## Contents
- [Highlights](#highlights)
- [Feature tour](#feature-tour)
- [Architecture](#architecture)
- [Data model at a glance](#data-model-at-a-glance)
- [Quick start](#quick-start)
- [Daily workflows](#daily-workflows)
- [Development guide](#development-guide)
- [Troubleshooting](#troubleshooting)

## Highlights
- **Two-role access** – patients and doctors receive dedicated dashboards tailored to their needs.
- **Virtual time engine** – simulate busy clinic days in minutes; appointments, slots, and reminders react instantly.
- **Automation-first design** – background jobs clean expired slots, auto-generate availability, and nudge patients before visits.
- **Offline friendly** – bootstrap a fresh SQLite database locally with one Gradle task; Flyway migrations and demo seed data live in version control for reproducible demos.

## Feature tour
| Area | What you get |
| --- | --- |
| **Authentication & onboarding** | Email/password login, guided registration for patients and licensed doctors, automatic provisioning of medical cards and initial time slots. |
| **Patient cockpit** | Search doctors by specialty, maintain favourites, book or reschedule visits, review medical history, and read notifications in one window. |
| **Doctor cockpit** | Monitor the live schedule, manage availability slots, review upcoming appointments, and record visit outcomes without leaving the panel. |
| **Virtual time controls** | Shared play/pause/step controls for doctors and patients with granular listeners that trigger automation on every tick. |
| **Notifications** | Real-time reminders for upcoming visits, status changes, and administrative actions delivered straight into the patient UI. |

> 💡 **Tip:** Launch the virtual clock before a demo to showcase automated slot clean-up and visit completion.

## Architecture
```
src/main/java/com/clinicmanager
├── app/            ─► Application bootstrap & login wiring
├── gui/            ─► JavaFX controllers and UI helpers
├── controller/     ─► Shared presentation logic between dashboards
├── model/          ─► Domain entities (accounts, doctors, patients, visits)
├── repository/     ─► SQLite data access objects managed by a central context
├── service/        ─► Business rules (registration, scheduling, notifications)
├── time/           ─► Virtual clock, tick handler, automation hooks
└── security/       ─► Password hashing and token utilities
```
- **AppContext** initialises the SQLite connection, repository manager, and reusable services for dependency injection across controllers.
- **RepositoryManager** exposes typed repositories (accounts, doctors, patients, schedules, slots, appointments, notifications) that share one lifecycle-managed connection.
- **TimeManager** and **TimeTickHandler** coordinate the virtual clock, dispatching events to update schedules, finalise overdue visits, and push reminders.

## Data model at a glance
| Table | Purpose |
| --- | --- |
| `accounts` | Authentication records with hashed passwords and role metadata. |
| `doctors`, `patients` | Profile details, contact info, and role-specific flags. |
| `schedules` & `slots` | Availability templates and generated visit slots. |
| `appointments` | Booked visits with status tracking and timestamps. |
| `medical_cards` & `medical_records` | Longitudinal patient history authored by doctors. |
| `notifications` | User-facing alerts triggered by automation or manual actions. |
| `favorite_doctors` | Patient shortcuts to frequently booked practitioners. |

Schema migrations live in `src/main/resources/db/migration/` and are executed with Flyway. No binary database is tracked in Git—each developer generates a local copy on demand.

## Quick start
1. **Install dependencies**
   - JDK 17+
   - (Optional) A local Gradle install – the project bundles the Gradle Wrapper.
   - No manual JAR download is required: Gradle pulls JavaFX (via the OpenJFX plugin), SQLite, SLF4J, and all supporting native
     artifacts automatically during the build.
2. **Initialise the database** (creates `clinic.db` next to the project root)
   ```bash
   ./gradlew flywayMigrate
   ```
   Supply a custom location with `./gradlew flywayMigrate -PdbPath=path/to/your.db`, or simply start the app—migrations run automatically on launch.
3. **Run the desktop app**
   ```bash
   ./gradlew run
   ```
4. **Sign in or register** using the start menu. Sample credentials from the demo seed migration include:
   - Doctor: `doctor.stone@example.com` / `doctor123`
   - Patient: `alice.johnson@example.com` / `patient123`
   Feel free to create fresh ones through the registration flow.

### Database refresh
Need a clean slate? Remove the old file and re-run the migrations:
```bash
rm -f clinic.db
./gradlew flywayMigrate
```
Flyway picks up any migration files under `src/main/resources/db/migration/`, so new scripts run automatically for every developer.

## Daily workflows
- **Front desk demo:** Register a patient and doctor, then jump into the patient dashboard to book a visit. Switch to the doctor panel to confirm the appointment and add a medical record.
- **Scheduling blitz:** From the doctor panel, open the schedule manager, generate new slots, and let the virtual clock run to showcase automatic clean-up.
- **Notification spotlight:** Trigger reminders by scheduling an appointment that starts in the next few virtual minutes, then fast-forward using the time controls.

## Development guide
- **Build & test**
  ```bash
  ./gradlew build       # compile & run unit tests
  ./gradlew run         # launch JavaFX client
  ./gradlew jlinkZip    # produce a self-contained runtime image
  ```
- Gradle resolves runtime dependencies (JavaFX, SQLite JDBC, SLF4J, etc.) on demand, so keep the `lib/` directory empty and out
  of version control.
- **Coding conventions**
  - Keep controller logic thin; push domain decisions into services.
  - Register new repositories with the central manager to reuse the shared connection.
  - Extend the virtual time automation through `TimeTickHandler` to keep behaviour consistent.
- **Database access**
  - `RepositoryManager` owns a single JDBC connection and passes it to every repository.
    Use try-with-resources (`new RepositoryManager(url)`) or call `closeAll()` during
    shutdown to dispose of the shared handle.
  - For multi-step workflows, temporarily disable auto-commit on the connection retrieved
    from the manager, execute repository calls, then `commit()`/`rollback()` to wrap the
    operations in one transaction.
- **UI tweaks**
  - FXML layouts live under `src/main/resources/gui`.
  - CSS themes are bundled alongside FXML for quick styling iterations.

## Troubleshooting
| Symptom | Fix |
| --- | --- |
| JavaFX runtime error on startup | Ensure `--module-path` isn’t overridden in your IDE; prefer running via the Gradle `run` task. |
| SQLite database locked | Close lingering app instances or IDE database explorers, then relaunch the app. |
| No notifications appear | Confirm the virtual clock is running and that appointments fall within the reminder window. |
| Time controls disabled | Log in as a doctor or patient; admins have read-only access to the virtual clock. |

Enjoy building a clinic experience that feels alive! 🚀
