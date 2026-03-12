# SATH FireChat
### A Professional Real-time Android Messaging System

**SATH FireChat** is a robust, high-performance Android application built to demonstrate modern mobile development practices. It provides a seamless real-time communication experience, leveraging **Firebase** for backend services and a meticulously decoupled architecture to ensure scalability and maintainability.

---

## Core Features

- **Real-time Messaging**: Instant data synchronization across devices using Firebase Realtime Database.
- **Secure Authentication**: Integrated dual-auth system supporting standard **Email/Password** credentials and **Google Sign-In**.
- **Clean UI/UX**: Professional Material Design 3 interface with full **Edge-to-Edge** support and immersive system bar integration.
- **Dynamic Theming**: Full support for **Dark Mode**, Light Mode, and System Default synchronization.
- **Global Localization**: Instant runtime language switching between **English** and **Greek** without application restart.
- **Account Management**: Comprehensive profile customization including avatar selection and secure account deletion protocols.

---

## Architecture Deep Dive

The project follows **Clean Architecture** principles, prioritizing the **Single Responsibility Principle (SOLID)**. The logic is divided into distinct layers to avoid "God Activities" and ensure high maintainability:

### 1. UI Layer (View)
Activities (e.g., `ChatActivity`, `ProfileActivity`) are strictly responsible for rendering data and capturing user input. They extend a `BaseActivity` which handles global configuration like locale and theme application.

### 2. Logic Layer (Managers)
Singleton Managers (e.g., `AuthManager`, `ChatManager`, `ThemeManager`) act as the "brain" of the app. They handle business logic, data validation, and state management, communicating results back to the UI via dedicated **Interfaces**.

### 3. Data Layer (Firebase)
Firebase serves as the single source of truth. The app utilizes:
- **Authentication**: To manage user sessions securely.
- **Realtime Database**: For a reactive, NoSQL approach to storing messages and user metadata.

---

## Key Code Explanations

### Decoupled Logic Pattern
Instead of calling Firebase directly from the Activity, we use a Manager pattern with asynchronous callbacks. This makes the code unit-testable and the UI highly responsive.

```java
// Example of the clean communication pattern in ChatManager.java
public void sendMessage(String senderId, String receiverId, String messageText, MessageActionResultListener listener) {
    DatabaseReference newMsgRef = mDatabase.child("Chats").push();
    // Payload creation...
    newMsgRef.setValue(hashMap).addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            updateChatList(senderId, receiverId); // Atomic update logic
            if (listener != null) listener.onSuccess();
        } else {
            if (listener != null) listener.onFailure(task.getException().getMessage());
        }
    });
}
```

### Global Configuration via BaseActivity
To ensure that theme and language changes persist and apply immediately to every screen, we utilize a `BaseActivity` context wrapper.

```java
// BaseActivity handles the locale and theme for every inheriting screen
@Override
protected void attachBaseContext(Context newBase) {
    languageManager = new LanguageManager(newBase);
    super.attachBaseContext(languageManager.setLocale(newBase));
}
```

---

## Tech Stack

- **Language**: Java
- **Backend**: Firebase (Auth, Realtime Database)
- **Identity**: Google Sign-In SDK
- **UI Framework**: Material Design 3 (Components), ConstraintLayout, Edge-to-Edge
- **Jetpack Libraries**: Activity, Core
- **Persistence**: SharedPreferences (Theme & Locale preference)

---

## Setup & Run

1. **Clone the repository**:
   ```sh
   git clone https://github.com/your-username/FireChat.git
   ```
2. **Firebase Configuration**:
   - Create a project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android App with the package name `com.example.p22005unipifirechat`.
   - Download `google-services.json` and place it in the `app/` directory.
   - Enable **Email/Password** and **Google** sign-in methods in the Auth tab.
   - Create a **Realtime Database** instance.
3. **Build**:
   - Open the project in Android Studio.
   - Sync Gradle files and run the application on an emulator or physical device.

---

## Author
**Efstathios Panagiotis Athanasakos**  
*Computer Science Student at University of Piraeus*
