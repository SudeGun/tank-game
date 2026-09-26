# 🎮 Tank 2025 (JavaFX Desktop Game)

A 2D retro arcade tank battle game built using **Java 8** and **JavaFX**.

The project features pure Java-coded GUI components, real-time 2D physics/game loop, sprite animations, collision detection, and autonomous enemy AI—implemented without external scene builders, FXML, or third-party engines.

---

## 🕹️ Gameplay & Core Features

* **Player Control:** Navigate the tank in 4 cardinal directions using arrow keys, complete with animated track (tread) motion.
* **Shooting & Bullet Physics:** Fire straight-trajectory shells with the `X` key that move at constant velocity until colliding with a wall, player, or enemy unit.
* **Enemy AI & Spawning:** Autonomous enemy tanks spawn dynamically in the upper zone of the map, randomly adjust their pathing, and continuously shoot at intervals.
* **Destruction & Explosions:** Bullets hitting tanks or indestructible obstacles trigger animated explosion effects (`Explosion.java`) before clearing[cite: 12, 13].
* **Lives & Scoring System:** The player starts with 3 lives and a score counter. Taking damage depletes a life and triggers respawning in the home base; eliminating enemies increments score.
* **Pause & Game Over States:** 
  * Press `P` during the match to toggle the pause menu.
  * Losing all lives displays the Game Over screen with your final score[cite: 13].
  * Press `R` to restart or `Escape` to quit[cite: 13].

---

## 📁 Repository Structure

```text
├── Main.java         # Main application entry point, game loop, scene management & controls
├── Drawing.java      # Canvas/Stage rendering, wall layout, and graphical asset handling
├── EnemyTank.java    # AI-controlled enemy tank logic, movement, and shooting intervals
├── Bullet.java       # Shell kinematics, boundary checks, and collision detection
├── Explosion.java    # Animation frames and lifecycle for impact/destruction effects
└── Checklist.pdf     # Compliance checklist for assignment deliverables
