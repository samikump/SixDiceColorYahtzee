## 🎲 Six Dice Color Yahtzee: Las Vegas Edition
**A Java Swing implementation of the classic dice game with a colorful twist.**

### 📖 Project Overview
Experience Yahtzee like never before! This **Java-based desktop application** brings a "Las Vegas" atmosphere to the game.  
Unlike the traditional game, this version combines **Six-Dice mechanics** with **Color Yahtzee** rules, creating a deeper strategic experience.  
Designed with a clean **MVC-inspired architecture**, it uses **Java Swing** for a vibrant UI and **MySQL** for robust data persistence.

### ✨ Key Features
*   **Six Dice Gameplay**: A rare variation that adds complexity and higher scoring potential than the standard 5-dice game.
*   **Color Yahtzee Integration**: Scoring is based on both numbers and dice colors, offering a fresh layer of strategy.
*   **Las Vegas Theme**: Custom-designed dice assets and "Hold" buttons in Vegas style!
*   **Live Leaderboard**: A dynamic **MySQL High Score** system that tracks top performers across sessions.
*   **Secure & Modular**: Professional-grade credential management using externalized `.properties` files to keep your database safe.

### 🛠️ Tech Stack & Architecture
*   **Language**: Java
*   **GUI Framework**: Java Swing
*   **Database**: MySQL 8.0+
*   **Build System**: Maven
*   **Database Connectivity**: JDBC

### 🚀 How to run it

To run this project locally, you will need to set up your local environment:

1.  **Database Setup**: Ensure you have a MySQL server running and create a database for the project.
    *  The MySQL script for the database is relatively simple:
       ```sql
       CREATE DATABASE Yahtzee;
       USE Yahtzee;
       CREATE TABLE hiscore (playerName VARCHAR(20), score INT, date DATETIME);
       ```
2.  **Configuration**:
    *   Locate the `dbconfig.properties.example` file in the project root.
    *   Create a copy of it and rename the copy to `dbconfig.properties`.
    *   Open `dbconfig.properties` and fill in your local MySQL **root password**.
    *   *(Note: The application currently uses 'root' as the default database user).*
3.  **Build & Run**:
    *   Open the project in NetBeans (or your preferred IDE).
    *   Clean and Build the project using Maven.
    *   Run the `SixDiceColorYahtzee` class to start the game.

TODO: Styling for the hiscore table
