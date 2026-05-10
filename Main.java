import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.util.*;

/**
 * Main class for the JavaFX Tank Game.
 * Features:
 * - Player-controlled tank with shooting and movement
 * - Randomly spawning enemy tanks with shooting
 * - Bullet-wall and bullet-tank collisions with explosions
 * - Score tracking, lives system, pause and restart functionality
 */

public class Main extends Application {
    private static final int TILE_SIZE = 25;  // size of the one wall image

    private int lives = 3;
    private int score = 0;
    private boolean playerAlive = true;
    private long deathTime = 0;
    private static final long RESPAWN_DELAY = 2_000_000_000L; // 2 seconds
    private int maxEnemyTanks = 10; // max limit for the number of enemy tanks

    private final ArrayList<Bullet> bullets = new ArrayList<>();
    private final ArrayList<Bullet> enemyBullets = new ArrayList<>();
    private final List<Explosion> explosions = new ArrayList<>();
    private final List<EnemyTank> enemyTanks = new ArrayList<>();

    private char[][] map = {   // map for the walls
            "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW".toCharArray(),
            "W                                      W".toCharArray(),
            "W                                      W".toCharArray(),
            "W                                      W".toCharArray(),
            "W                                      W".toCharArray(),
            "W                                      W".toCharArray(),
            "W                                      W".toCharArray(),
            "W                                      W".toCharArray(),
            "W                                      W".toCharArray(),
            "W             WWWWWWWWWWWW             W".toCharArray(),
            "W   WW                            WW   W".toCharArray(),
            "W   WW                            WW   W".toCharArray(),
            "W   WW                            WW   W".toCharArray(),
            "W   WW  WW                    WW  WW   W".toCharArray(),
            "W   WW  WW                    WW  WW   W".toCharArray(),
            "W   WW  WW                    WW  WW   W".toCharArray(),
            "W   WW  WW                    WW  WW   W".toCharArray(),
            "W   WW  WW                    WW  WW   W".toCharArray(),
            "W                                      W".toCharArray(),
            "W                                      W".toCharArray(),
            "WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW".toCharArray()
    };

    private final Set<KeyCode> keysPressed = new HashSet<>();
    private Canvas canvas;
    private GraphicsContext gc;
    private final int START_X = 2;
    private final int START_Y = 19;
    private int tankX = START_X;
    private int tankY = START_Y;
    private int direction = 0;
    private boolean isMoving = false;
    private boolean gameOver = false;
    private boolean paused = false;
    private long lastImageSwitchTime = 0;
    private boolean switchImage = false;
    private long lastMoveTime = 0;
    private static final long MOVE_DELAY = 150_000_000L; // 150ms

    private long lastEnemySpawnTime = 0;
    private long enemySpawnInterval = 5000_000_000L; // 5 seconds

    private Drawing drawing;

    /**
     * Initializes the game window, loads images, sets up the canvas, and starts the game loop.
     *
     * @param stage The primary stage for this application.
     */
    @Override
    public void start(Stage stage) {
        canvas = new Canvas(map[0].length * TILE_SIZE, map.length * TILE_SIZE);
        gc = canvas.getGraphicsContext2D();
        drawing = new Drawing(gc, TILE_SIZE);
        drawing.loadImages();

        Pane root = new Pane(canvas);
        Scene scene = new Scene(root);
        // Key input handling
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.P) {
                if (!gameOver) {
                    paused = !paused;
                }
                return;
            }

            if (gameOver || paused) {
                if (e.getCode() == KeyCode.R) {
                    resetGame();
                } else if (e.getCode() == KeyCode.ESCAPE) {
                    System.exit(0);
                }
                return;
            }

            keysPressed.add(e.getCode());
            if (e.getCode() == KeyCode.X) {
                double bulletX = tankX * TILE_SIZE + TILE_SIZE / 2.0;
                double bulletY = tankY * TILE_SIZE + TILE_SIZE / 2.0;
                bullets.add(new Bullet(bulletX, bulletY, direction));
            }
        });

        scene.setOnKeyReleased(e -> {
            keysPressed.remove(e.getCode());
        });

        new AnimationTimer() {
            public void handle(long now) {
                if (gameOver) {
                    drawing.drawGame(map, tankX, tankY, direction, isMoving, switchImage, playerAlive,
                            bullets, enemyBullets, explosions, enemyTanks,
                            score, lives, paused, gameOver,
                            canvas.getWidth(), canvas.getHeight());
                    return;
                }
                if (paused) {
                    drawing.drawGame(map, tankX, tankY, direction, isMoving, switchImage, playerAlive,
                            bullets, enemyBullets, explosions, enemyTanks,
                            score, lives, paused, gameOver,
                            canvas.getWidth(), canvas.getHeight());
                    return;
                }
                if (!playerAlive && now - deathTime > RESPAWN_DELAY && lives > 0) { // returning to the initial position after being hit
                    playerAlive = true;
                    tankX = START_X;
                    tankY = START_Y;
                    direction=0;
                }
                if (playerAlive) {
                    updatePlayer();
                    if (isMoving) {
                        if (now - lastImageSwitchTime > 200_000_000L) { // every 200ms
                            switchImage = !switchImage;
                            lastImageSwitchTime = now;
                        }
                    } else {
                        switchImage = false; // reset to default frame when not moving
                    }
                }
                updateBullets();
                updateEnemyTanks();
                updateEnemyBullets();


                if (now - lastEnemySpawnTime > enemySpawnInterval && enemyTanks.size() < maxEnemyTanks) {
                    spawnEnemyTank();
                    lastEnemySpawnTime = now;
                }

                drawing.drawGame(map, tankX, tankY, direction, isMoving, switchImage, playerAlive,
                        bullets, enemyBullets, explosions, enemyTanks,
                        score, lives, paused, gameOver,
                        canvas.getWidth(), canvas.getHeight());
            }
        }.start();

        stage.setTitle("Tank Game");
        stage.setScene(scene);
        stage.show();

    }

    /**
     * Updates the player tank's movement based on pressed keys, while enforcing movement delay
     * and checking for wall collisions.
     */
    public void updatePlayer() {
        isMoving = false;
        long now = System.nanoTime();
        if (now - lastMoveTime < MOVE_DELAY) {
            return; // enforce movement delay
        }
        int dx = 0;
        int dy = 0;
        boolean keyPressed = false;

        if (keysPressed.contains(KeyCode.UP)) {
            dy = -1;
            direction = 3;
            keyPressed = true;
        } else if (keysPressed.contains(KeyCode.RIGHT)) {
            dx = 1;
            direction = 0;
            keyPressed = true;
        } else if (keysPressed.contains(KeyCode.DOWN)) {
            dy = 1;
            direction = 1;
            keyPressed = true;
        } else if (keysPressed.contains(KeyCode.LEFT)) {
            dx = -1;
            direction = 2;
            keyPressed = true;
        }
        if (keyPressed) {
            int newX = tankX + dx;
            int newY = tankY + dy;

            if (newX >= 0 && newX < map[0].length && newY >= 0 && newY < map.length && map[newY][newX] != 'W' && !isThereTank(newX, newY)) { //moving the player
                tankX = newX;
                tankY = newY;
                isMoving = true;
                lastMoveTime = now;
            }
        }
    }

    /**
     * Spawns a new enemy tank at a random location near the top of the map,
     */
    public void spawnEnemyTank() {
        Random random = new Random();
        while (true) {
            int x = random.nextInt(map[0].length);
            int y = random.nextInt(3)+1; // upper part of the map

            if (map[y][x] != 'W') {
                enemyTanks.add(new EnemyTank(x, y, map, TILE_SIZE));
                break;
            }
        }
    }

    /**
     * Updates the state of all bullets fired by the player, checks for collisions with
     * walls and removes bullets that collide.
     */
    public void updateBullets(){
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.updateBullet();

            int bx = (int)(bullet.x / TILE_SIZE);
            int by = (int)(bullet.y / TILE_SIZE);

            // Remove bullet if it hits wall or goes out of bounds
            if (bullet.x < 0 || bullet.y < 0 || bullet.x > map[0].length * TILE_SIZE || bullet.y > map.length * TILE_SIZE) {
                iterator.remove();
            }else if (map[by][bx] == 'W') {
                explosions.add(new Explosion(bullet.x, bullet.y));
                iterator.remove();
            }
        }
    }

    /**
     * Updates all enemy tanks, checks for collisions with player bullets,
     * and handles explosion effects and updates score
     */
    public void updateEnemyTanks(){
        // Update enemy tanks
        Iterator<EnemyTank> enemyIterator = enemyTanks.iterator();
        while (enemyIterator.hasNext()) {
            EnemyTank enemy = enemyIterator.next();
            enemy.update(map, tankX, tankY, enemyTanks);

            // Check if enemy is hit by player's bullet
            Iterator<Bullet> bulletIterator = bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                int bx = (int)(bullet.x / TILE_SIZE);
                int by = (int)(bullet.y / TILE_SIZE);

                if (enemy.x == bx && enemy.y == by && enemy.alive) {
                    explosions.add(new Explosion(enemy.x * TILE_SIZE + TILE_SIZE / 2.0, enemy.y * TILE_SIZE + TILE_SIZE / 2.0, true));
                    bulletIterator.remove();
                    enemy.setAlive(false);
                    score += 100;
                }
            }

            // Remove dead enemies
            if (!enemy.alive) {
                enemyIterator.remove();
            } else {
                enemyBullets.addAll(enemy.getBulletsFired());
                enemy.getBulletsFired().clear();
            }
        }

    }

    /**
     * Updates all bullets fired by enemies, checks for wall and player collisions,
     * updates explosions and reduces lives if the player is hit.
     * Ends the game if the player runs out of lives.
     */
    private void updateEnemyBullets() {
        Iterator<Bullet> it = enemyBullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.updateBullet();

            int bx = (int)(b.x / TILE_SIZE), by = (int)(b.y / TILE_SIZE);
            // remove off‐screen
            if (b.x < 0 || b.y < 0 || b.x > map[0].length * TILE_SIZE || b.y > map.length * TILE_SIZE) {
                it.remove();
            }
            // explode on wall hit
            else if (map[by][bx] == 'W') {
                explosions.add(new Explosion(b.x, b.y, false));
                it.remove();
            }else if (playerAlive && tankX==bx && tankY==by) {
                // player hit
                explosions.add(new Explosion(tankX*TILE_SIZE+TILE_SIZE/2.0,
                        tankY*TILE_SIZE+TILE_SIZE/2.0,
                        true));
                it.remove();
                lives--;
                playerAlive = false;
                deathTime = System.nanoTime();

                if (lives == 0) {
                    gameOver = true;
                }
            }
        }
    }

    /**
     * Checks if any enemy tank is at the specified position
     */
    private boolean isThereTank(int x, int y) {
        for (EnemyTank enemy : enemyTanks) {
            if (enemy.x == x && enemy.y == y && enemy.alive) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resets all game variables and collections to restart the game,
     * restoring the player to the initial state.
     */
    private void resetGame() {
        lives = 3;
        score = 0;
        tankX = START_X;
        tankY = START_Y;
        bullets.clear();
        enemyBullets.clear();
        explosions.clear();
        enemyTanks.clear();
        keysPressed.clear();
        direction = 0;
        playerAlive = true;
        gameOver = false;
        paused = false;
        isMoving = false;
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch();
    }
}