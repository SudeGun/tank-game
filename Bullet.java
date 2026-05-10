/**
 * Represents a bullet fired by the player or an enemy in the tank game.
 */
public class Bullet {
    double x,y;
    int direction;  // 0 = right, 1 = down, 2 = left, 3 = up
    int speed = 5;

    /**
     * Constructs a new bullet with the given starting position and direction.
     *
     * @param x         the initial x-coordinate in pixels
     * @param y         the initial y-coordinate in pixels
     * @param direction the direction of the bullet (0=right, 1=down, 2=left, 3=up)
     */
    public Bullet(double x, double y, int direction){
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    /**
     * Updates the bullet's position based on its direction and speed.
     */
    public void updateBullet(){
        switch (direction) {
            case 0: x += speed; break; // right
            case 1: y += speed; break; // down
            case 2: x -= speed; break; // left
            case 3: y -= speed; break; // up
        }
    }

}
