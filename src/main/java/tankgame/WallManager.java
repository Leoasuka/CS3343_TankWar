package tankgame;
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
    public static final int MIN_WALL_LENGTH = 40;
    public static final int MAX_WALL_LENGTH = 200;
    public static final int MIN_WALL_THICKNESS = 20;
    public static final int MAX_FORTIFIED_HEALTH = 500;
    public static final int TEMPORARY_WALL_DURATION = 15000; // 15 seconds

    private final WallGenerator generator;

    private final List<Wall> permanentWalls = new ArrayList<>();
    private final List<BreakableWall> breakableWalls = new ArrayList<>();
    private final List<TemporaryWall> temporaryWalls = new ArrayList<>();
    private final TankClient gameClient;

    /**
     * Constructor for wall manager
     * @param client Reference to game client
     */
    public WallManager(TankClient client) {
        this.gameClient = client;
        this.generator = new WallGenerator(this);
        generator.generateDefaultLayout();
    }

    public void generateRandomWalls(int count) {
        if (count <= 0) return;
        generator.generateRandomWalls(count);
    }

    /**
     * Initializes default wall layout
     */
    /*private void initializeDefaultWalls() {
        // Add permanent walls
        addPermanentWall(100, 200, 20, 150);  // Vertical wall
        addPermanentWall(300, 100, 300, 20);  // Horizontal wall

        // Add some breakable walls
        addBreakableWall(500, 300, 100, 20);
        addBreakableWall(200, 400, 20, 100);

        // Add fortified walls
        addFortifiedWall(400, 200, 20, 100);
    }*/

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
        if (width <= 0 || height <= 0) return;
        permanentWalls.add(new Wall(x, y, width, height, gameClient));
    }

    /**
     * Adds a breakable wall
     */
    public void addBreakableWall(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        breakableWalls.add(new BreakableWall(x, y, width, height, gameClient));
    }

    /**
     * Adds a fortified wall
     */
    public void addFortifiedWall(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        breakableWalls.add(new BreakableWall(x, y, width, height, gameClient, MAX_FORTIFIED_HEALTH));
    }

    /**
     * Adds a temporary wall
     */
    public void addTemporaryWall(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        temporaryWalls.add(new TemporaryWall(x, y, width, height, gameClient,
                System.currentTimeMillis() + TEMPORARY_WALL_DURATION));
    }

    /**
     * Generates random walls in the game area
     * @param count Number of walls to generate
     */

    /**
     * Gets all walls for collision detection
     * @return Array of all walls
     */
    public Wall[] getAllWalls() {
        List<Wall> allWalls = new ArrayList<>();
        if (!permanentWalls.isEmpty()) allWalls.addAll(permanentWalls);
        if (!breakableWalls.isEmpty()) allWalls.addAll(breakableWalls);
        if (!temporaryWalls.isEmpty()) allWalls.addAll(temporaryWalls);
        return allWalls.toArray(new Wall[0]);
    }

    /**
     * Generic collision handler for game objects
     * @param gameObject Object to check collision with walls
     * @param <T> Type of game object (Missile or Tank)
     * @return true if collision occurred
     */
    private <T> boolean handleCollisionWithWalls(T gameObject) {
        if (gameObject == null) return false;

        for (Wall wall : getAllWalls()) {
            if (wall == null) continue;

            if (gameObject instanceof Missile) {
                if (wall.handleMissileCollision((Missile)gameObject)) {
                    return true;
                }
            } else if (gameObject instanceof Tank) {
                if (((Tank)gameObject).handleWallCollision(wall)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Handle missile collision with walls
     */
    public boolean handleMissileCollision(Missile missile) {
        return handleCollisionWithWalls(missile);
    }

    /**
     * Handle tank collision with walls
     */
    public void handleTankCollision(Tank tank) {
        handleCollisionWithWalls(tank);
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

/**
 * WallGenerator Class - Handles wall generation logic and positioning
 * Manages random wall generation and positioning strategies
 */
class WallGenerator {
    private final WallManager wallManager;
    private final Random random = new Random();

    // Constants for generation settings
    private static final int PADDING = 20;
    private static final int EDGE_PADDING = 50;
    private static final int MAX_POSITION_ATTEMPTS = 50;

    // Probability constants for wall types
    private static final int PERMANENT_WALL_CHANCE = 40;  // 40%
    private static final int BREAKABLE_WALL_CHANCE = 30;  // 30%
    private static final int FORTIFIED_WALL_CHANCE = 20;  // 20%
    // Remaining 10% is for temporary walls

    /**
     * Constructor for wall generator
     * @param wallManager Reference to the wall manager
     */
    public WallGenerator(WallManager wallManager) {
        this.wallManager = wallManager;
    }

    /**
     * Checks if a new wall would overlap with existing walls
     */
    private boolean wouldOverlap(int x, int y, int width, int height) {
        Rectangle newWallBounds = new Rectangle(x, y, width, height);

        // Add padding around walls
        Rectangle paddedBounds = new Rectangle(
                x - PADDING,
                y - PADDING,
                width + PADDING * 2,
                height + PADDING * 2
        );

        // Check overlap with existing walls
        for (Wall wall : wallManager.getAllWalls()) {
            if (paddedBounds.intersects(wall.getCollisionBounds())) {
                return true;
            }
        }

        // Check screen edge proximity
        return x < EDGE_PADDING ||
                y < EDGE_PADDING ||
                x + width > TankClient.GAME_WIDTH - EDGE_PADDING ||
                y + height > TankClient.GAME_HEIGHT - EDGE_PADDING;
    }

    /**
     * Finds a valid position for a new wall
     */
    private Point findValidWallPosition(int width, int height) {
        for (int i = 0; i < MAX_POSITION_ATTEMPTS; i++) {
            int x = random.nextInt(TankClient.GAME_WIDTH - WallManager.MAX_WALL_LENGTH);
            int y = random.nextInt(TankClient.GAME_HEIGHT - WallManager.MAX_WALL_LENGTH);

            if (!wouldOverlap(x, y, width, height)) {
                return new Point(x, y);
            }
        }
        return null;
    }

    /**
     * Generates random walls
     */
    public void generateRandomWalls(int count) {
        int successfulPlacements = 0;
        int maxAttempts = count * 2;
        int attempts = 0;

        while (successfulPlacements < count && attempts < maxAttempts) {
            attempts++;

            // Generate wall dimensions
            boolean isVertical = random.nextBoolean();
            int width = isVertical ? WallManager.MIN_WALL_THICKNESS :
                    random.nextInt(WallManager.MAX_WALL_LENGTH - WallManager.MIN_WALL_LENGTH) + WallManager.MIN_WALL_LENGTH;
            int height = isVertical ?
                    random.nextInt(WallManager.MAX_WALL_LENGTH - WallManager.MIN_WALL_LENGTH) + WallManager.MIN_WALL_LENGTH :
                    WallManager.MIN_WALL_THICKNESS;

            Point position = findValidWallPosition(width, height);
            if (position == null) continue;

            createRandomWall(position.x, position.y, width, height);
            successfulPlacements++;
        }
    }

    /**
     * Creates a random wall based on probability distribution
     */
    private void createRandomWall(int x, int y, int width, int height) {
        int rand = random.nextInt(100);
        if (rand < PERMANENT_WALL_CHANCE) {
            wallManager.addPermanentWall(x, y, width, height);
        } else if (rand < PERMANENT_WALL_CHANCE + BREAKABLE_WALL_CHANCE) {
            wallManager.addBreakableWall(x, y, width, height);
        } else if (rand < PERMANENT_WALL_CHANCE + BREAKABLE_WALL_CHANCE + FORTIFIED_WALL_CHANCE) {
            wallManager.addFortifiedWall(x, y, width, height);
        } else {
            wallManager.addTemporaryWall(x, y, width, height);
        }
    }

    /**
     * Generates the default wall layout
     */
    public void generateDefaultLayout() {
        // Central cross formation
        wallManager.addPermanentWall(TankClient.GAME_WIDTH/2 - 150, TankClient.GAME_HEIGHT/2, 300, 20);
        wallManager.addPermanentWall(TankClient.GAME_WIDTH/2, TankClient.GAME_HEIGHT/2 - 150, 20, 300);

        // Corner fortifications
        wallManager.addFortifiedWall(100, 100, 100, 20);
        wallManager.addFortifiedWall(TankClient.GAME_WIDTH - 200, 100, 100, 20);
        wallManager.addFortifiedWall(100, TankClient.GAME_HEIGHT - 120, 100, 20);
        wallManager.addFortifiedWall(TankClient.GAME_WIDTH - 200, TankClient.GAME_HEIGHT - 120, 100, 20);

        // Breakable walls
        wallManager.addBreakableWall(250, 200, 20, 100);
        wallManager.addBreakableWall(TankClient.GAME_WIDTH - 270, 200, 20, 100);

        // Temporary walls
        wallManager.addTemporaryWall(TankClient.GAME_WIDTH/2 - 100, 150, 200, 20);
    }
}