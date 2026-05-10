import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents an enemy tank in the game
 */
public class EnemyTank {
    int x, y;
    int direction;
    long lastDirectionChangeTime;  //last time direction was changed
    long directionChangeInterval;  //randomized interval before the next direction change
    long lastShotTime; // last time the tank fired a bullet
    long shotInterval; //randomized interval between shots
    boolean alive = true;
    public boolean isMoving = false;
    public boolean animationFrame = false; /** Used for animation frame switching. */
    private long lastAnimationSwitchTime = System.nanoTime();
    private static final long ANIMATION_SWITCH_INTERVAL = 200_000_000L; /** Time interval between animation frame switches (200 ms). */
    private Random random = new Random();
    private char[][] map; // game map
    private List<Bullet> bulletsFired = new ArrayList<>();
    private final int tileSize;
    private long lastMoveTime = System.nanoTime();
    private static final long MOVE_INTERVAL = 200_000_000L; // 200 ms

    /**
     * Constructs a new EnemyTank at the given coordinates.
     *
     * @param x        the x position in tile units
     * @param y        the y position in tile units
     * @param map      the game map
     * @param tileSize the size of each tile
     */
    public EnemyTank(int x, int y, char[][] map, int tileSize) {
        this.x = x;
        this.y = y;
        this.direction = random.nextInt(4);
        this.lastDirectionChangeTime = System.nanoTime();
        this.directionChangeInterval = random.nextInt(2000_000_000) + 2000_000_000L; // 2-4 seconds
        this.lastShotTime = System.nanoTime();
        this.shotInterval = random.nextInt(2000_000_000) + 1000_000_000L; // 1-3 seconds
        this.map = map;
        this.tileSize = tileSize;
    }

    /**
     * Updates the enemy tank's state:
     * - Moves if it's time
     * - Changes direction if needed
     * - Fires a bullet at random intervals
     * - Switches animation frames if moving
     */
    public void update(char[][] map, int playerX, int playerY, List<EnemyTank> otherEnemies) {
        long now = System.nanoTime();
        int oldX = x;
        int oldY = y;

        // Random direction change
        if (now - lastDirectionChangeTime > directionChangeInterval) {
            direction = random.nextInt(4);
            lastDirectionChangeTime = now;
            directionChangeInterval = random.nextInt(2000_000_000) + 2000_000_000L;
        }
        // Movement
        if (now - lastMoveTime > MOVE_INTERVAL) {
            int dx = 0, dy = 0;
            switch (direction) {
                case 0:
                    dx = 1;
                    break;  // right
                case 1:
                    dy = 1;
                    break;  // down
                case 2:
                    dx = -1;
                    break; // left
                case 3:
                    dy = -1;
                    break;// up
            }

            int newX = x + dx;
            int newY = y + dy;

            if (isValidMove(newX, newY, map, playerX, playerY, otherEnemies)) {
                this.x = newX;
                this.y = newY;
            } else {
                // If can't move, change direction immediately
                direction = random.nextInt(4);
                lastDirectionChangeTime = now;
            }
            lastMoveTime = now;
        }
        isMoving = (x != oldX || y != oldY);

        // Animation frame switch
        if (isMoving && now - lastAnimationSwitchTime > ANIMATION_SWITCH_INTERVAL) {
            animationFrame = !animationFrame;
            lastAnimationSwitchTime = now;
        }
        // Shooting bullets
        if (now - lastShotTime > shotInterval) {
            double bulletX = x * tileSize + tileSize / 2.0;
            double bulletY = y * tileSize + tileSize / 2.0;
            bulletsFired.add(new Bullet(bulletX, bulletY, direction));
            lastShotTime = now;
            shotInterval = random.nextInt(2000_000_000) + 1000_000_000L;
        }

    }

    /**
     * Checks the position to prevent overlap with player or wall and helps to stay inside the map.
     *
     * @param x        the x position in tile units
     * @param y        the y position in tile units
     * @param map      the game map
     * @param playerX  the x position of player in tile units
     * @param playerY  the y position of player in tile units
     * @param otherEnemies the list of enemies
     */
    private boolean isValidMove(int x, int y, char[][] map, int playerX, int playerY, List<EnemyTank> otherEnemies) {
        // Check wall collision
        if (x < 0 || x >= map[0].length || y < 0 || y >= map.length || map[y][x] == 'W') {
            return false;
        }

        // Check player collision
        if (x == playerX && y == playerY) {
            return false;
        }

        // Check other enemies collision
        for (EnemyTank enemy : otherEnemies) {
            if (enemy != this && enemy.x == x && enemy.y == y && enemy.alive) {
                return false;
            }
        }
        return true;
    }
    /**
     * Sets whether the enemy tank is alive.
     *
     * @param alive true if the tank is alive, false if destroyed
     */
    public void setAlive(boolean alive) { this.alive = alive; }
    /**
     * Returns the list of bullets fired by this enemy tank.
     *
     * @return list of fired bullets
     */
    public List<Bullet> getBulletsFired() { return bulletsFired; }

}
