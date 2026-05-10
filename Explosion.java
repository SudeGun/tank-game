/**
 * Represents an explosion effect in the game.
 */
public class Explosion {
    double x,y;
    long startTime;
    public boolean isBigExplosion;

    /**
     * Creates a small explosion at the given position.
     *
     * @param x the x-coordinate in pixels
     * @param y the y-coordinate in pixels
     */
    public Explosion(double x, double y) {
        this(x, y, false);
    }
    /**
     * Creates an explosion at the given position with an option to be a big explosion.
     *
     * @param x              the x-coordinate in pixels
     * @param y              the y-coordinate in pixels
     * @param isBigExplosion true for a big explosion, false for normal
     */
    public Explosion(double x, double y, boolean isBigExplosion) {
        this.x = x;
        this.y = y;
        this.isBigExplosion = isBigExplosion;
        this.startTime = System.nanoTime();
    }

    /**
     * Checks whether the explosion duration has expired.
     * Explosions last approximately 450 milliseconds.
     *
     * @return true if the explosion should be removed; false otherwise
     */
    public boolean checkingTime(){
        return System.nanoTime() - startTime > 450_000_000L;
    }

}
