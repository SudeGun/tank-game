import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.Iterator;
import java.util.List;

/**
 * Handles all rendering operations for the Tank Game.
 * This class encapsulates all drawing logic, including loading and displaying game assets,
 */

public class Drawing {
    private final GraphicsContext gc;
    private final int tileSize;

    // Game asset images
    private Image wallImage;
    private Image yellowTank1;
    private Image yellowTank2;
    private Image bulletImage;
    private Image smallExplosionImage;
    private Image whiteTank1;
    private Image whiteTank2;
    private Image bigExplosionImage;

    /**
     * Constructs a new Drawing instance.
     * @param gc The GraphicsContext to draw on
     * @param tileSize The size of each game tile in pixels
     */
    public Drawing(GraphicsContext gc, int tileSize) {
        this.gc = gc;
        this.tileSize = tileSize;
    }

    /**
     * Loads all game images from resources.
     */
    public void loadImages() {
        wallImage = new Image(getClass().getResourceAsStream("wall.png"));
        yellowTank1 = new Image(getClass().getResourceAsStream("yellowTank1.png"));
        yellowTank2 = new Image(getClass().getResourceAsStream("yellowTank2.png"));
        bulletImage = new Image(getClass().getResourceAsStream("bullet.png"));
        smallExplosionImage = new Image(getClass().getResourceAsStream("smallExplosion.png"));
        whiteTank1 = new Image(getClass().getResourceAsStream("whiteTank1.png"));
        whiteTank2 = new Image(getClass().getResourceAsStream("whiteTank2.png"));
        bigExplosionImage = new Image(getClass().getResourceAsStream("explosion.png"));
    }

    /**
     * Main drawing method that renders the entire game state.
     * @param map The game map grid
     * @param tankX Player tank X position
     * @param tankY Player tank Y position
     * @param direction Player tank facing direction
     * @param isMoving Whether player tank is currently moving
     * @param switchImage Player tank animation frame flag
     * @param playerAlive Whether player is alive
     * @param bullets List of player bullets
     * @param enemyBullets List of enemy bullets
     * @param explosions List of active explosions
     * @param enemyTanks List of enemy tanks
     * @param score Current game score
     * @param lives Remaining player lives
     * @param paused Whether game is paused
     * @param gameOver Whether game is over
     * @param canvasWidth Width of the game canvas
     * @param canvasHeight Height of the game canvas
     */
    public void drawGame(char[][] map,
                         int tankX, int tankY, int direction, boolean isMoving,
                         boolean switchImage, boolean playerAlive,
                         List<Bullet> bullets, List<Bullet> enemyBullets,
                         List<Explosion> explosions, List<EnemyTank> enemyTanks,
                         int score, int lives, boolean paused, boolean gameOver,
                         double canvasWidth, double canvasHeight) {
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        drawMap(map);
        if (playerAlive) {
            drawPlayerTank(tankX, tankY, direction, isMoving, switchImage);
        }
        drawBullets(bullets);
        drawEnemyBullets(enemyBullets);
        drawExplosions(explosions);
        drawEnemyTanks(enemyTanks);
        drawInfo(score, lives);
        if (paused) {
            drawPauseScreen(canvasWidth, canvasHeight);
        }

        if (gameOver) {
            drawGameOverScreen(score, canvasWidth, canvasHeight);
        }
    }

    /**
     * Draws the game map including all walls.
     * @param map The 2D char array representing the game map
     */
    private void drawMap(char[][] map) {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                if (map[y][x] == 'W') {
                    gc.drawImage(wallImage, x * tileSize, y * tileSize, tileSize, tileSize);
                }
            }
        }
    }

    /**
     * Draws the player tank with proper orientation and animation.
     * @param x Tank's X position
     * @param y Tank's Y position
     * @param direction Facing direction (0=right, 1=down, 2=left, 3=up)
     * @param isMoving Whether tank is currently moving
     * @param switchImage Animation frame flag
     */
    private void drawPlayerTank(int x, int y, int direction, boolean isMoving, boolean switchImage){
        gc.save();
        // Calculate center position of tank
        double px = x * tileSize + tileSize / 2.0;
        double py = y * tileSize + tileSize / 2.0;
        gc.translate(px, py);
        switch (direction) {
            case 0: // right
                break; // no transformation
            case 1: // down
                gc.rotate(90);
                break;
            case 2: // left
                gc.scale(-1, 1);
                break;
            case 3: // up
                gc.rotate(-90);
                break;
        }
        Image image ;
        if (isMoving){
            if(switchImage){
                image= yellowTank2;
            }else {
                image= yellowTank1;
            }
        }else {
            image= yellowTank1;
        }
        gc.drawImage(image, -tileSize / 2.0, -tileSize / 2.0, tileSize, tileSize);
        gc.restore();
    }

    /**
     * Draws all active player bullets.
     * @param bullets List of player bullets to draw
     */
    private void drawBullets(List<Bullet> bullets){
        for (Bullet bullet : bullets) {
            gc.drawImage(bulletImage, bullet.x - tileSize / 4.0, bullet.y - tileSize / 4.0, tileSize / 2.0, tileSize / 2.0);
        }
    }

    /**
     * Draws all active enemy bullets.
     * @param enemyBullets List of enemy bullets to draw
     */
    private void drawEnemyBullets(List<Bullet> enemyBullets) {
        for (Bullet bullet : enemyBullets) {
            gc.drawImage(bulletImage, bullet.x - tileSize / 4.0, bullet.y - tileSize / 4.0, tileSize / 2.0, tileSize / 2.0);
        }
    }

    /**
     * Draws and manages all active explosions.
     * Removes explosions that have completed their animation.
     * @param explosions List of active explosions
     */
    private void drawExplosions(List<Explosion> explosions){
        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            if (explosion.checkingTime()) {
                explosionIterator.remove();
            } else {
                Image image;
                double size;
                double drawX,drawY;
                if(explosion.isBigExplosion){
                    image = bigExplosionImage;
                    size = tileSize * 2; //make it bigger
                }else {
                    image = smallExplosionImage;
                    size = tileSize;   //normal size
                }
                drawX = explosion.x - size / 2.0;
                drawY = explosion.y - size / 2.0;
                gc.drawImage(image, drawX, drawY, size, size);
            }
        }
    }

    /**
     * Draws all enemy tanks with proper orientation and animation.
     * @param enemyTanks List of enemy tanks to draw
     */
    private void drawEnemyTanks(List<EnemyTank> enemyTanks){
        for (EnemyTank enemy : enemyTanks) {
            gc.save();
            double ex = enemy.x * tileSize + tileSize / 2.0;
            double ey = enemy.y * tileSize + tileSize / 2.0;
            gc.translate(ex, ey);
            switch (enemy.direction) {
                case 0: break; // right
                case 1: gc.rotate(90); break; // down
                case 2: gc.scale(-1, 1); break; // left
                case 3: gc.rotate(-90); break; // up
            }
            Image image;
            if(enemy.animationFrame){
                image=whiteTank1;
            }else {
                image=whiteTank2;
            }
            gc.drawImage(image, -tileSize / 2.0, -tileSize / 2.0, tileSize, tileSize);
            gc.restore();
        }

    }

    /**
     * Draws score and lives.
     * @param score Current game score
     * @param lives Remaining player lives
     */
    private void drawInfo(int score, int lives) {
        gc.setFill(Color.DARKBLUE);
        gc.setFont(javafx.scene.text.Font.font("Arial", 18));
        gc.fillText("Lives: " + lives, 10, 20);
        gc.fillText("Score: " + score, 10, 40);
    }

    /**
     * Draws the pause screen.
     * @param width Canvas width
     * @param height Canvas height
     */
    private void drawPauseScreen(double width, double height) {
        gc.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, width, height);
        gc.setFill(javafx.scene.paint.Color.YELLOW);
        gc.setFont(javafx.scene.text.Font.font("Arial", 36));
        gc.fillText("PAUSED", width / 2 - 70, height / 2 - 20);
        gc.setFont(javafx.scene.text.Font.font("Arial", 18));
        gc.fillText("Press R to Restart, ESC to Quit", width / 2 - 110, height / 2 + 20);
    }

    /**
     * Draws the game over screen.
     * @param score Final game score
     * @param width Canvas width
     * @param height Canvas height
     */
    private void drawGameOverScreen(int score, double width, double height) {
        gc.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, width, height);
        gc.setFill(javafx.scene.paint.Color.RED);
        gc.setFont(javafx.scene.text.Font.font("Arial", 36));
        gc.fillText("GAME OVER", width / 2 - 100, height / 2 - 40);
        gc.setFont(javafx.scene.text.Font.font("Arial", 24));
        gc.fillText("Your Score: " + score, width / 2 - 80, height / 2);
        gc.setFont(javafx.scene.text.Font.font("Arial", 18));
        gc.fillText("Press R to Restart, ESC to Quit", width / 2 - 110, height / 2 + 40);
    }

}
