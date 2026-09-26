# SmartPantryManager  
Smart Pantry Manager is an Android application built in Java using VS Code, with SQLite as the database for persistent storage. The app helps users reduce food waste by tracking pantry items and suggesting recipes that can be cooked strictly with the ingredients already available at home.

Key Features

Pantry management: add, edit, delete items with quantity, unit, and expiry date.

Recipe collection: 15–20 preloaded recipes stored in SQLite.

Suggested Recipes: strict‑matching algorithm ensures only recipes with all required ingredients appear.

Recipe detail screen with full instructions.

Settings/profile options (e.g., expiry alerts, unit preferences).

Full CRUD functionality with persistent storage.

Tech Stack

Language: Java

IDE: Android Studio

Database: SQLite

UI: RecyclerView/ListView, custom Adapters, ConstraintLayout

Setup Instructions

Clone the repository:

bash
git clone https://github.com/yourusername/SmartPantryManager.git
Open in IDE.

Build and run on emulator or physical device.

SQLite setup: database initializes automatically with preloaded recipes on first run.
