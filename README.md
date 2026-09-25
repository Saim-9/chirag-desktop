# Chirag - Ignite Your Learning

Chirag is a desktop course marketplace application built with JavaFX, similar in concept to platforms like Udemy. Users can register, create courses with video lectures, browse and purchase courses from other users, track their progress, leave reviews, and earn completion certificates. The platform includes a full admin panel for user and content moderation.

This project was built as part of a Software Design and Architecture course (Semester 4).

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
  - [1. Database](#1-database)
  - [2. Configuration](#2-configuration)
  - [3. Build and Run](#3-build-and-run)
- [Super Admin and Admin Management](#super-admin-and-admin-management)
- [How Video Lectures Work](#how-video-lectures-work)
- [Known Limitations](#known-limitations)
- [Project Structure](#project-structure)
- [License](#license)

---

## Features

**For Users (Students and Instructors)**
- Register and log in with email and password
- Browse and search courses in the marketplace
- Purchase courses using a simulated virtual wallet
- Create and publish your own courses with video lectures
- Track enrollment progress across purchased courses
- Leave reviews and ratings on completed courses
- View transaction history
- Earn a certificate of completion after finishing all lectures
- Update profile and change password

**For Admins**
- Dedicated admin dashboard
- Manage all users (suspend/activate accounts)
- Moderate courses (activate/deactivate listings)
- View platform-wide reports and complaints
- Create additional admin accounts (Super Admin only)

---

## Tech Stack

| Component      | Technology                          |
|----------------|-------------------------------------|
| Language        | Java 21                            |
| UI Framework    | JavaFX 21 (FXML + CSS)            |
| ORM             | ORMLite 6.1                        |
| Database        | PostgreSQL (tested with Supabase)  |
| Password Hashing| BCrypt (jBCrypt 0.4)              |
| Logging         | SLF4J + Logback                   |
| Build Tool      | Maven                             |

---

## Prerequisites

Before setting up the project, make sure you have the following installed:

- **Java Development Kit (JDK) 21** or later
- **Apache Maven 3.8+**
- **A PostgreSQL database** -- you can use a cloud-hosted instance like [Supabase](https://supabase.com/) (free tier works), or a local PostgreSQL server

---

## Setup

### 1. Database

You need a running PostgreSQL database. The application will automatically create all required tables on first launch, so you do not need to run any SQL scripts manually.

**Using Supabase (recommended for quick setup):**
1. Create a free project at [supabase.com](https://supabase.com/)
2. Go to Project Settings > Database and note down the connection string, username, and password

**Using local PostgreSQL:**
1. Install PostgreSQL and create a new database
2. Note down the JDBC connection URL (e.g., `jdbc:postgresql://localhost:5432/chirag`)

### 2. Configuration

The application reads its database and admin credentials from a `config.properties` file. This file is not included in the repository for security reasons.

1. Navigate to `src/main/resources/`
2. Copy the example file to create your own config:
   ```
   cp config.properties.example config.properties
   ```
3. Open `config.properties` and fill in your values:
   ```properties
   # PostgreSQL / Supabase connection
   db.url=jdbc:postgresql://YOUR_HOST:5432/postgres
   db.username=YOUR_DB_USERNAME
   db.password=YOUR_DB_PASSWORD

   # Super Admin seed credentials (created on first run)
   admin.email=admin@example.com
   admin.password=YOUR_SECURE_PASSWORD
   admin.name=Super Admin
   ```

The Super Admin account is automatically created in the database the first time the application starts, using the credentials you provide here. See the section below for more details.

### 3. Build and Run

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/chirag-desktop.git
cd chirag-desktop

# Build the project
mvn clean install

# Run the application
mvn javafx:run
```

---

## Super Admin and Admin Management

Chirag has a role-based access system with two roles: **USER** and **ADMIN**.

- **Super Admin**: The first admin account is automatically seeded into the database on the application's first launch. The credentials for this account are defined in your `config.properties` file. If an account with the configured email already exists, the seeding step is skipped.

- **Creating Additional Admins**: Once logged in as the Super Admin (or any admin), you can create new admin accounts directly from the Admin Dashboard. You provide a name, email, and password for the new admin. The new admin can then log in and access the admin panel with the same moderation privileges.

- **Regular Users** register themselves through the registration screen and are assigned the USER role by default. Users cannot elevate their own role.

---

## How Video Lectures Work

Course lectures in Chirag are delivered through embedded video links using a WebView component. The application does not host video files directly. Instead, instructors provide a URL to a video hosted externally.

**Supported video sources:**
- YouTube (unlisted videos)


**Important: YouTube videos must be uploaded as "Unlisted"**

When adding lectures to a course, you paste a YouTube. For YouTube, the video must be uploaded with the visibility set to **Unlisted** (not Private, not Public). Unlisted means:
- The video will not appear in search results or on your channel
- Only people with the direct link can watch it
- The Chirag app uses this link to embed the video in its built-in player

If a video is set to **Private**, the embedded player will not be able to load it. If set to **Public**, it will work but the video will be discoverable by anyone on YouTube.


---

## Known Limitations

This is an academic project and has several intentional simplifications:

- **Email validation is string-based only.** The application checks whether the email matches a standard format using a regex pattern (e.g., `user@domain.com`). It does not use any email verification API, send confirmation emails, or verify that the address actually exists. Any correctly formatted string will be accepted.

- **Payments are fully simulated.** There is no real payment gateway integration. Users have a virtual wallet balance that can be topped up with arbitrary amounts. Course purchases deduct from this virtual balance. A 90/10 split is applied where the instructor receives 90% and the platform takes a 10% fee. All of this is simulated within the database.

- **Password reset is simulated.** The forgot password flow does not send a reset email or OTP. It verifies the email exists in the database and then directly allows setting a new password. In a production application, this would involve sending a secure token via email.

- **No real email or notification system.** The application does not send any emails or push notifications. All feedback is provided through in-app alerts and toast notifications.

- **Desktop-only.** This is a JavaFX desktop application. There is no web or mobile version.

---

## Project Structure

```
src/main/java/com/chirag/
|-- Main.java                  # Application entry point
|-- Launcher.java              # JavaFX launcher wrapper
|-- controllers/               # FXML controllers for each screen
|   |-- LoginController.java
|   |-- RegisterController.java
|   |-- DashboardController.java
|   |-- MarketplaceController.java
|   |-- CourseCreationController.java
|   |-- CourseDetailController.java
|   |-- CoursePlayerController.java
|   |-- EditCourseController.java
|   |-- ProfileController.java
|   |-- AdminDashboardController.java
|   |-- ForgotPasswordController.java
|   |-- ReviewPopupController.java
|   |-- WalletPopupController.java
|-- models/                    # ORM entity classes
|   |-- User.java
|   |-- Course.java
|   |-- Lecture.java
|   |-- Enrollment.java
|   |-- Transaction.java
|   |-- Review.java
|   |-- Report.java
|   |-- IReviewable.java
|-- repositories/              # Data access layer (ORMLite DAOs)
|-- services/                  # Business logic layer
|   |-- UserService.java
|   |-- CourseService.java
|   |-- PaymentServiceImpl.java
|   |-- DashboardService.java
|   |-- CertificateService.java
|   |-- ContentPlayerService.java
|   |-- CourseInteractionService.java
|   |-- AdminService.java
|   |-- AbstractService.java
|   |-- IPaymentService.java
|-- utils/                     # Shared utilities
|   |-- DatabaseConfig.java    # Singleton DB connection manager
|   |-- SceneManager.java      # FXML scene/navigation manager
|   |-- UserSession.java       # Current user session holder
|   |-- DataCache.java         # In-memory data cache
|   |-- ToastNotification.java # Toast notification utility

src/main/resources/
|-- config.properties.example  # Template for database configuration
|-- logback.xml                # Logging configuration
|-- com/chirag/views/          # FXML layouts and CSS styles
```

---

## Authors

- [Saim Tahir](https://github.com/Saim-9)
- [Affaq Hussain](https://github.com/affaqhussain)
- [Sufyan Akbar](https://github.com/akbar-sufyan)

---

## License

This project is licensed under the [MIT License](LICENSE).
