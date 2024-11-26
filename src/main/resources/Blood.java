package resources;
import java.awt.*;

/**
 * Blood Class - Represents a health pack in the tank game
 * The health pack moves in a predefined path continuously
 * When collected by the player tank, it restores tank's health to maximum
 */
public class Blood {
    // Position coordinates and dimensions of the health pack
    private int positionX, positionY;
    private final int width;
    private final int height;

    // Reference to the main game client
    private TankClient gameClient;

    // Current index in the movement path
    private int pathIndex = 0;

    // Status flag indicating if the health pack is available
    private boolean isActive = true;

    /**
     * Predefined movement path coordinates
     * Each array element contains [x, y] coordinates for health pack movement
     */
    private final int[][] movementPath = {
            {350, 300}, {360, 300}, {375, 275}, {400, 200},
            {360, 270}, {365, 290}, {340, 280}
    };

    /**
     * Constructor - Initializes the health pack
     * Sets initial position to first point in movement path
     * Sets default size to 15x15 pixels
     */
    public Blood() {
        positionX = movementPath[0][0];
        positionY = movementPath[0][1];
        width = height = 15;
    }

    /**
     * Renders the health pack on the game screen
     * @param graphics Graphics context for rendering
     */
    public void renderHealthPack(Graphics graphics) {
        if(!isActive) return;

        Color originalColor = graphics.getColor();
        graphics.setColor(Color.MAGENTA);
        graphics.fillRect(positionX, positionY, width, height);
        graphics.setColor(originalColor);

        updatePosition();
    }

    /**
     * Updates the health pack's position along its predefined path
     * Cycles through movement path coordinates
     */
    private void updatePosition() {
        pathIndex++;
        if(pathIndex == movementPath.length) {
            pathIndex = 0;
        }
        positionX = movementPath[pathIndex][0];
        positionY = movementPath[pathIndex][1];
    }

    /**
     * Gets the collision rectangle for the health pack
     * Used for collision detection with tanks
     * @return Rectangle representing the health pack's bounds
     */
    public Rectangle getCollisionBounds() {
        return new Rectangle(positionX, positionY, width, height);
    }

    /**
     * Checks if the health pack is currently active in the game
     * @return boolean indicating if health pack can be collected
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the active status of the health pack
     * @param status New active status to set
     */
    public void setActive(boolean status) {
        this.isActive = status;
    }

    // // if health <=0
    // public void checkHealth(Tank playerTank) {
    //     if (playerTank.getHealthPoints() <= 0) {
    //         gameClient.setGameState(TankClient.GameState.GAME_OVER);
    //     }
    // }
}
