import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * WallManager Class - Handles wall generation and management
 * Manages different types of walls, dynamic generation, and wall damage
 */
public class WallManager {
    // Wall type definitions
    public enum WallType {
        NORMAL,     // Standard wall
        BREAKABLE,  // Wall that can be destroyed
        FORTIFIED,  // Stronger wall with more health
        TEMPORARY   // Wall that disappears after time
    }

    // Constants for wall properties
    private static final int MIN_WALL_LENGTH = 40;
    private static final int MAX_WALL_LENGTH = 200;
    private static final int MIN_WALL_THICKNESS = 20;
    private static final int MAX_FORTIFIED_HEALTH = 500;
    private static final int TEMPORARY_WALL_DURATION = 15000; // 15 seconds

    // Lists to store different types of walls
    private final List<Wall> permanentWalls = new ArrayList<>();
    private final List<BreakableWall> breakableWalls = new ArrayList<>();
    private final List<TemporaryWall> temporaryWalls = new ArrayList<>();

    // Reference to game client
    private final TankClient gameClient;
    private final Random random = new Random();

    /**
     * Constructor for wall manager
     * @param client Reference to game client
     */
    public WallManager(TankClient client) {
        this.gameClient = client;
        initializeDefaultWalls();
    }

    /**
     * Initializes default wall layout
     */
    private void initializeDefaultWalls() {
        // Add permanent walls
        addPermanentWall(100, 200, 20, 150);  // Vertical wall
        addPermanentWall(300, 100, 300, 20);  // Horizontal wall

        // Add some breakable walls
        addBreakableWall(500, 300, 100, 20);
        addBreakableWall(200, 400, 20, 100);

        // Add fortified walls
        addFortifiedWall(400, 200, 20, 100);
    }

    /**
     * Updates all walls' state
     * @param currentTime Current game time in milliseconds
     */
    public void update(long currentTime) {
        // Update temporary walls
        Iterator<TemporaryWall> iterator = temporaryWalls.iterator();
        while (iterator.hasNext()) {
            TemporaryWall wall = iterator.next();
            if (wall.hasExpired(currentTime)) {
                iterator.remove();
            }
        }

        // Update breakable walls
        breakableWalls.removeIf(wall -> !wall.isAlive());
    }

    /**
     * Renders all walls
     * @param graphics Graphics context for rendering
     */
    public void render(Graphics graphics) {
        // Render all types of walls
        for (Wall wall : permanentWalls) {
            wall.render(graphics);
        }
        for (BreakableWall wall : breakableWalls) {
            wall.render(graphics);
        }
        for (TemporaryWall wall : temporaryWalls) {
            wall.render(graphics);
        }
    }

    /**
     * Adds a permanent wall
     */
    public void addPermanentWall(int x, int y, int width, int height) {
        permanentWalls.add(new Wall(x, y, width, height, gameClient));
    }

    /**
     * Adds a breakable wall
     */
    public void addBreakableWall(int x, int y, int width, int height) {
        breakableWalls.add(new BreakableWall(x, y, width, height, gameClient));
    }

    /**
     * Adds a fortified wall
     */
    public void addFortifiedWall(int x, int y, int width, int height) {
        breakableWalls.add(new BreakableWall(x, y, width, height, gameClient, MAX_FORTIFIED_HEALTH));
    }

    /**
     * Adds a temporary wall
     */
    public void addTemporaryWall(int x, int y, int width, int height) {
        temporaryWalls.add(new TemporaryWall(x, y, width, height, gameClient,
                System.currentTimeMillis() + TEMPORARY_WALL_DURATION));
    }

    /**
     * Generates random walls in the game area
     * @param count Number of walls to generate
     */
    public void generateRandomWalls(int count) {
        for (int i = 0; i < count; i++) {
            int x = random.nextInt(TankClient.GAME_WIDTH - MAX_WALL_LENGTH);
            int y = random.nextInt(TankClient.GAME_HEIGHT - MAX_WALL_LENGTH);
            int width = random.nextInt(MAX_WALL_LENGTH - MIN_WALL_LENGTH) + MIN_WALL_LENGTH;
            int height = MIN_WALL_THICKNESS;

            // Randomly choose wall type
            switch (random.nextInt(4)) {
                case 0:
                    addPermanentWall(x, y, width, height);
                    break;
                case 1:
                    addBreakableWall(x, y, width, height);
                    break;
                case 2:
                    addFortifiedWall(x, y, width, height);
                    break;
                case 3:
                    addTemporaryWall(x, y, width, height);
                    break;
            }
        }
    }

    /**
     * Gets all walls for collision detection
     * @return Array of all walls
     */
    public Wall[] getAllWalls() {
        List<Wall> allWalls = new ArrayList<>();
        allWalls.addAll(permanentWalls);
        allWalls.addAll(breakableWalls);
        allWalls.addAll(temporaryWalls);
        return allWalls.toArray(new Wall[0]);
    }

    /**
     * Handles missile collision with walls
     * @param missile Missile to check collision with
     * @return true if collision occurred
     */
    public boolean handleMissileCollision(Missile missile) {
        for (Wall wall : getAllWalls()) {
            if (wall.handleMissileCollision(missile)) {
                // Missile is deactivated inside handleMissileCollision
                return true;
            }
        }
        return false;
    }

    public boolean handleTankCollision(Tank tank) {
        for (Wall wall : getAllWalls()) {
            if (tank.handleWallCollision(wall)) {
                return true;
            }
        }
        return false;
    }
}

/**
 * BreakableWall Class - Represents a destructible wall in the game
 * Extends the basic Wall class with health and damage mechanics
 * Changes color based on remaining health and can be destroyed
 */
class BreakableWall extends Wall {
    // Health points of the wall
    private int health;
    // Status flag indicating if wall is still functional
    private boolean isAlive = true;

    /**
     * Constants for health management
     * MAX_HEALTH: Maximum health points for a standard breakable wall
     * MIN_HEALTH: Minimum health points (wall is destroyed below this)
     */
    private static final int MAX_HEALTH = 100;
    private static final int MIN_HEALTH = 0;
    private static final int FORTIFIED_HEALTH = 500;  // Fortified wall max health

    /**
     * Constructor for creating a standard breakable wall
     * Initializes the wall with maximum health
     *
     * @param x Initial X coordinate of the wall
     * @param y Initial Y coordinate of the wall
     * @param width Width of the wall
     * @param height Height of the wall
     * @param client Reference to the game client
     */
    public BreakableWall(int x, int y, int width, int height, TankClient client) {
        super(x, y, width, height, client);
        this.health = MAX_HEALTH;
    }

    /**
     * Constructor for creating a breakable wall with custom health
     * Useful for creating walls with different strength levels
     * Health value is clamped between MIN_HEALTH and MAX_FORTIFIED_HEALTH
     *
     * @param x Initial X coordinate of the wall
     * @param y Initial Y coordinate of the wall
     * @param width Width of the wall
     * @param height Height of the wall
     * @param client Reference to the game client
     * @param health Initial health points for the wall
     */
    public BreakableWall(int x, int y, int width, int height, TankClient client, int health) {
        super(x, y, width, height, client);
        // Ensure health is within valid range
        this.health = Math.min(Math.max(MIN_HEALTH, health), FORTIFIED_HEALTH);
    }

    /**
     * Applies damage to the wall
     * Reduces health by specified amount and checks for destruction
     * Health cannot go below MIN_HEALTH
     *
     * @param amount Amount of damage to apply
     */
    public void damage(int amount) {
        // Ensure health doesn't go below minimum
        health = Math.max(MIN_HEALTH, health - amount);
        // Check if wall should be destroyed
        if (health <= MIN_HEALTH) {
            isAlive = false;
        }
    }

    /**
     * Handles collision with missiles
     * Applies damage when hit and returns collision status
     * Overrides the parent class's method to add damage mechanics
     *
     * @param missile The missile to check collision with
     * @return true if collision occurred and was handled, false otherwise
     */
    @Override
    public boolean handleMissileCollision(Missile missile) {
        if (!isAlive) return false;

        if (getCollisionBounds().intersects(missile.getCollisionBounds())) {
            damage(20); // Apply damage but don't create explosion
            return true;
        }
        return false;
    }

    /**
     * Gets the current alive status of the wall
     * @return boolean indicating if wall is still alive
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Gets the current health of the wall
     * @return current health points
     */
    public int getHealth() {
        return health;
    }

    /**
     * Renders the wall with health-based color
     * Color changes from green to red as health decreases
     * Override parent's render method to add health visualization
     *
     * @param graphics Graphics context for rendering
     */
    @Override
    public void render(Graphics graphics) {
        if (!isAlive) return;

        Color originalColor = graphics.getColor();
        // Calculate green component based on health percentage
        int greenValue = Math.min(255, Math.max(0, (int)(255 * (health / 100.0))));
        // Create color with fixed red component and variable green
        graphics.setColor(new Color(128, greenValue, 0));
        // Draw the wall
        graphics.fillRect(getPositionX(), getPositionY(), getWidth(), getHeight());
        // Restore original color
        graphics.setColor(originalColor);
    }
}

/**
 * TemporaryWall Class - Wall that exists for a limited time
 */
class TemporaryWall extends Wall {
    private final long expirationTime;

    public TemporaryWall(int x, int y, int width, int height, TankClient client, long expirationTime) {
        super(x, y, width, height, client);
        this.expirationTime = expirationTime;
    }

    @Override
    public void render(Graphics graphics) {
        Color originalColor = graphics.getColor();
        graphics.setColor(new Color(0, 191, 255, 180)); // Semi-transparent light blue
        graphics.fillRect(getPositionX(), getPositionY(), getWidth(), getHeight());
        graphics.setColor(originalColor);
    }

    public boolean hasExpired(long currentTime) {
        return currentTime > expirationTime;
    }
}