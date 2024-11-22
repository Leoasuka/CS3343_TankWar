package Resources;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;

/**
 * Tank Class - Represents a tank entity in the game
 * Handles tank movement, collision detection, shooting and health management
 */
public class Tank {
	// Constants for tank movement and dimensions
	public static final int MOVEMENT_SPEED_X = 5;
	public static final int MOVEMENT_SPEED_Y = 5;
	public static final int TANK_WIDTH = 40;
	public static final int TANK_HEIGHT = 30;

	// Tank state properties
	private boolean isAlive = true;
	private final HealthBar healthBar = new HealthBar();
	private int healthPoints = 100;

	// Game client reference
	private TankClient gameClient;

	// Tank type flag
	private final boolean isPlayerTank;

	// Position coordinates
	private int currentX, currentY;
	private int previousX, previousY;

	// Random generator for AI movement
	private static final Random randomGenerator = new Random();

	private TankAI ai;

	// Movement direction flags
	private boolean movingLeft = false;
	private boolean movingUp = false;
	private boolean movingRight = false;
	private boolean movingDown = false;

	/**
	 * Enum defining all possible movement directions
	 */
	public enum Direction {L, LU, U, RU, R, RD, D, LD, STOP}

	// Current movement and barrel direction
	private Direction moveDirection = Direction.STOP;
	private Direction barrelDirection = Direction.D;

	// AI movement counter
	private int movementStep = randomGenerator.nextInt(12) + 3;

	/**
	 * Constructor for creating a basic tank
	 */
	public Tank(int x, int y, boolean isPlayer) {
		this.currentX = x;
		this.currentY = y;
		this.previousX = x;
		this.previousY = y;
		this.isPlayerTank = isPlayer;
	}

	/**
	 * Constructor for creating a tank with specific direction and game client
	 */
	public Tank(int x, int y, boolean isPlayer, Direction dir, TankClient client) {
		this(x, y, isPlayer);
		this.moveDirection = dir;
		this.gameClient = client;
		if (!isPlayer) {
			this.ai = new TankAI(this, client);
		}
	}

	/**
	 * Renders the tank and updates its state
	 */
	public void render(Graphics graphics) {
		if(!isAlive) {
			if(!isPlayerTank) {
				gameClient.getEnemyTanks().remove(this);
			}
			return;
		}

		Color originalColor = graphics.getColor();

		// Draw tracks with increased width
		graphics.setColor(Color.DARK_GRAY);
		graphics.fillRect(currentX - 5, currentY, 5, TANK_HEIGHT); // Left track
		graphics.fillRect(currentX + TANK_WIDTH, currentY, 5, TANK_HEIGHT); // Right track

		// Draw tank body
		graphics.setColor(isPlayerTank ? Color.RED : Color.BLUE);
		graphics.fillRect(currentX, currentY, TANK_WIDTH, TANK_HEIGHT);

		// Draw armor plate textures
		graphics.setColor(isPlayerTank ? new Color(180, 0, 0) : new Color(0, 0, 180));
		graphics.drawLine(currentX, currentY + TANK_HEIGHT/3,
				currentX + TANK_WIDTH, currentY + TANK_HEIGHT/3);
		graphics.drawLine(currentX, currentY + 2*TANK_HEIGHT/3,
				currentX + TANK_WIDTH, currentY + 2*TANK_HEIGHT/3);

		// Draw turret with reduced size
		int turretWidth = 25;
		int turretHeight = 25;
		int turretX = currentX + (TANK_WIDTH - turretWidth)/2;
		int turretY = currentY + (TANK_HEIGHT - turretHeight)/2;
		graphics.setColor(Color.DARK_GRAY);
		graphics.fillOval(turretX, turretY, turretWidth, turretHeight);

		// Add armor ring around turret
		graphics.setColor(isPlayerTank ? new Color(180, 0, 0) : new Color(0, 0, 180));
		graphics.drawOval(turretX, turretY, turretWidth, turretHeight);

		graphics.setColor(originalColor);

		// Draw health bar if player tank
		if(isPlayerTank) healthBar.render(graphics);

		// Draw barrel
		renderBarrel(graphics);

		// Update position
		updatePosition();
	}

	/**
	 * Updates tank's position based on current direction
	 */
	private void updatePosition() {
		if (!isPlayerTank && ai != null) {
			ai.update();
		}

		// Store current position for collision recovery
		previousX = currentX;
		previousY = currentY;

		// Calculate new position based on direction
		int newX = currentX;
		int newY = currentY;

		switch(moveDirection) {
			case L:
				newX = currentX - MOVEMENT_SPEED_X;
				break;
			case LU:
				newX = currentX - MOVEMENT_SPEED_X;
				newY = currentY - MOVEMENT_SPEED_Y;
				break;
			case U:
				newY = currentY - MOVEMENT_SPEED_Y;
				break;
			case RU:
				newX = currentX + MOVEMENT_SPEED_X;
				newY = currentY - MOVEMENT_SPEED_Y;
				break;
			case R:
				newX = currentX + MOVEMENT_SPEED_X;
				break;
			case RD:
				newX = currentX + MOVEMENT_SPEED_X;
				newY = currentY + MOVEMENT_SPEED_Y;
				break;
			case D:
				newY = currentY + MOVEMENT_SPEED_Y;
				break;
			case LD:
				newX = currentX - MOVEMENT_SPEED_X;
				newY = currentY + MOVEMENT_SPEED_Y;
				break;
			case STOP:
				return;
		}

		// Check if new position would cause collision
		if(!willCollideWithOtherTanks(newX, newY) && !willCollideWithWalls(newX, newY)) {
			currentX = newX;
			currentY = newY;
		}

		// Update barrel direction if moving
		if(moveDirection != Direction.STOP) {
			barrelDirection = moveDirection;
		}

		// Keep tank within game bounds
		constrainToGameBounds();

		// Handle AI movement if not player tank
		handleAIMovement();
	}

	/**
	 * Checks if moving to a new position would cause collision with other tanks
	 * @param newX Potential new X position
	 * @param newY Potential new Y position
	 * @return true if collision would occur, false otherwise
	 */
	private boolean willCollideWithOtherTanks(int newX, int newY) {
		Rectangle predictedBounds = new Rectangle(newX, newY, TANK_WIDTH, TANK_HEIGHT);

		List<Tank> allTanks = new ArrayList<>(gameClient.getEnemyTanks());
		if(!isPlayerTank) {
			allTanks.add(gameClient.getPlayerTank());
		}

		for(Tank otherTank : allTanks) {
			if(this != otherTank && otherTank.isAlive()) {
				Rectangle otherBounds = otherTank.getCollisionBounds();
				if(predictedBounds.intersects(otherBounds)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if moving to a new position would cause collision with walls
	 * @param newX Potential new X position
	 * @param newY Potential new Y position
	 * @return true if collision would occur, false otherwise
	 */
	private boolean willCollideWithWalls(int newX, int newY) {
		Rectangle predictedBounds = new Rectangle(newX, newY, TANK_WIDTH, TANK_HEIGHT);

		// Check collision with all walls from WallManager
		for (Wall wall : gameClient.getWalls()) {
			if (predictedBounds.intersects(wall.getCollisionBounds())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Constrains tank position to game boundaries
	 */
	private void constrainToGameBounds() {
		if(currentX < 0) currentX = 0;
		if(currentY < 30) currentY = 30;
		if(currentX + TANK_WIDTH > TankClient.GAME_WIDTH) {
			currentX = TankClient.GAME_WIDTH - TANK_WIDTH;
		}
		if(currentY + TANK_HEIGHT > TankClient.GAME_HEIGHT) {
			currentY = TankClient.GAME_HEIGHT - TANK_HEIGHT;
		}
	}

	/**
	 * Handles AI movement for enemy tanks
	 */
	private void handleAIMovement() {
		if(!isPlayerTank) {
			Direction[] directions = Direction.values();
			if(movementStep == 0) {
				movementStep = randomGenerator.nextInt(12) + 3;
				int randomDirection = randomGenerator.nextInt(directions.length);
				moveDirection = directions[randomDirection];
			}
			movementStep--;

			if(randomGenerator.nextInt(40) > 38) {
				fireMissile();
			}
		}
	}

	private void renderBarrel(Graphics graphics) {
		int centerX = currentX + TANK_WIDTH/2;
		int centerY = currentY + TANK_HEIGHT/2;
		graphics.setColor(Color.DARK_GRAY);

		// Set barrel dimensions
		int barrelLength = 30;
		int barrelWidth = 8;

		Graphics2D g2d = (Graphics2D)graphics;
		// Store the original transform
		AffineTransform oldTransform = g2d.getTransform();

		switch(barrelDirection) {
			case L:
				g2d.translate(centerX, centerY);
				g2d.rotate(Math.PI);
				g2d.fillRect(0, -barrelWidth/2, barrelLength, barrelWidth);
				break;
			case LU:
				g2d.translate(centerX, centerY);
				g2d.rotate(-3 * Math.PI/4);  // -135 degrees
				g2d.fillRect(0, -barrelWidth/2, barrelLength, barrelWidth);
				break;
			case U:
				g2d.translate(centerX, centerY);
				g2d.rotate(-Math.PI/2);  // -90 degrees
				g2d.fillRect(0, -barrelWidth/2, barrelLength, barrelWidth);
				break;
			case RU:
				g2d.translate(centerX, centerY);
				g2d.rotate(-Math.PI/4);  // -45 degrees
				g2d.fillRect(0, -barrelWidth/2, barrelLength, barrelWidth);
				break;
			case R:
				g2d.translate(centerX, centerY);
				g2d.fillRect(0, -barrelWidth/2, barrelLength, barrelWidth);
				break;
			case RD:
				g2d.translate(centerX, centerY);
				g2d.rotate(Math.PI/4);  // 45 degrees
				g2d.fillRect(0, -barrelWidth/2, barrelLength, barrelWidth);
				break;
			case D:
				g2d.translate(centerX, centerY);
				g2d.rotate(Math.PI/2);  // 90 degrees
				g2d.fillRect(0, -barrelWidth/2, barrelLength, barrelWidth);
				break;
			case LD:
				g2d.translate(centerX, centerY);
				g2d.rotate(3 * Math.PI/4);  // 135 degrees
				g2d.fillRect(0, -barrelWidth/2, barrelLength, barrelWidth);
				break;
		}

		// Restore the original transform
		g2d.setTransform(oldTransform);
	}

	/**
	 * Reverts tank to previous position (used for collision handling)
	 */
	private void revertToPreviousPosition() {
		currentX = previousX;
		currentY = previousY;
	}

	/**
	 * Handles key press events for tank control
	 */
	public void handleKeyPressed(KeyEvent event) {
		int keyCode = event.getKeyCode();
		switch(keyCode) {
			case KeyEvent.VK_F2:
				if(!isAlive) {
					isAlive = true;
					healthPoints = 100;
				}
				break;
			case KeyEvent.VK_LEFT:
				movingLeft = true;
				break;
			case KeyEvent.VK_UP:
				movingUp = true;
				break;
			case KeyEvent.VK_RIGHT:
				movingRight = true;
				break;
			case KeyEvent.VK_DOWN:
				movingDown = true;
				break;
		}
		updateMoveDirection();
	}

	/**
	 * Updates movement direction based on current key states
	 */
	private void updateMoveDirection() {
		if (!isPlayerTank && ai != null) {
			ai.update();
		}
		if(movingLeft && !movingUp && !movingRight && !movingDown)
			moveDirection = Direction.L;
		else if(movingLeft && movingUp && !movingRight && !movingDown)
			moveDirection = Direction.LU;
		else if(!movingLeft && movingUp && !movingRight && !movingDown)
			moveDirection = Direction.U;
		else if(!movingLeft && movingUp && movingRight && !movingDown)
			moveDirection = Direction.RU;
		else if(!movingLeft && !movingUp && movingRight && !movingDown)
			moveDirection = Direction.R;
		else if(!movingLeft && !movingUp && movingRight && movingDown)
			moveDirection = Direction.RD;
		else if(!movingLeft && !movingUp && !movingRight && movingDown)
			moveDirection = Direction.D;
		else if(movingLeft && !movingUp && !movingRight && movingDown)
			moveDirection = Direction.LD;
		else if(!movingLeft && !movingUp && !movingRight && !movingDown)
			moveDirection = Direction.STOP;
	}

	/**
	 * Handles key release events for tank control
	 */
	public void handleKeyReleased(KeyEvent event) {
		int keyCode = event.getKeyCode();
		switch(keyCode) {
			case KeyEvent.VK_SPACE:
				fireMissile();
				break;
			case KeyEvent.VK_LEFT:
				movingLeft = false;
				break;
			case KeyEvent.VK_UP:
				movingUp = false;
				break;
			case KeyEvent.VK_RIGHT:
				movingRight = false;
				break;
			case KeyEvent.VK_DOWN:
				movingDown = false;
				break;
			case KeyEvent.VK_A:
				fireInAllDirections();
				break;
		}
		updateMoveDirection();
	}

	/**
	 * Fires a missile in current barrel direction
	 */
	public Missile fireMissile() {
		if(!isAlive) return null;
		int missileX = currentX + TANK_WIDTH/2 - Missile.MISSILE_WIDTH/2;
		int missileY = currentY + TANK_HEIGHT/2 - Missile.MISSILE_HEIGHT/2;
		Missile missile = new Missile(missileX, missileY, isPlayerTank, barrelDirection, gameClient);
		gameClient.getMissiles().add(missile);
		return missile;
	}

	/**
	 * Fires a missile in specified direction
	 */
	public Missile fireMissile(Direction direction) {
		if(!isAlive) return null;
		int missileX = currentX + TANK_WIDTH/2 - Missile.MISSILE_WIDTH/2;
		int missileY = currentY + TANK_HEIGHT/2 - Missile.MISSILE_HEIGHT/2;
		Missile missile = new Missile(missileX, missileY, isPlayerTank, direction, gameClient);
		gameClient.getMissiles().add(missile);
		return missile;
	}

	/**
	 * Gets tank's collision rectangle
	 */
	public Rectangle getCollisionBounds() {
		return new Rectangle(currentX, currentY, TANK_WIDTH, TANK_HEIGHT);
	}

	/**
	 * Checks if tank is alive
	 */
	public boolean isAlive() {
		return isAlive;
	}

	/**
	 * Sets tank's alive status
	 */
	public void setAlive(boolean status) {
		this.isAlive = status;
	}

	/**
	 * Checks if tank is player controlled
	 */
	public boolean isPlayerControlled() {
		return isPlayerTank;
	}

	/**
	 * Handles collision with walls
	 */
	public boolean handleWallCollision(Wall wall) {
		if(this.isAlive && this.getCollisionBounds().intersects(wall.getCollisionBounds())) {
			this.revertToPreviousPosition();
			return true;
		}
		return false;
	}

	/**
	 * Handles collision with other tanks
	 */
	public boolean handleTankCollisions(java.util.List<Tank> tanks) {
		for(Tank otherTank : tanks) {
			if(this != otherTank) {
				if(this.isAlive && otherTank.isAlive() &&
						this.getCollisionBounds().intersects(otherTank.getCollisionBounds())) {
					this.revertToPreviousPosition();
					otherTank.revertToPreviousPosition();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Fires missiles in all directions
	 */
	private void fireInAllDirections() {
		Direction[] directions = Direction.values();
		for(int i = 0; i < 8; i++) {
			fireMissile(directions[i]);
		}
	}

	/**
	 * Gets tank's current health points
	 */
	public int getHealthPoints() {
		return healthPoints;
	}

	/**
	 * Sets tank's health points
	 */
	public void setHealthPoints(int points) {
		this.healthPoints = points;
		if (this.healthPoints <= 0 && this.isPlayerTank) {
            gameClient.setGameState(TankClient.GameState.GAME_OVER);
			gameClient.setScore(gameClient.getExplosions().size());
        }
	}

	/**
	 * Inner class representing tank's health bar
	 */
	private class HealthBar {
		// Constants for health bar appearance
		private static final int BAR_HEIGHT = 6;  // Reduced height for less intrusive look
		private static final int BAR_VERTICAL_OFFSET = 8;  // Distance above tank

		public void render(Graphics graphics) {
			Color originalColor = graphics.getColor();

			// Calculate center-aligned position
			int barWidth = TANK_WIDTH - 10;  // Slightly narrower than tank
			int barX = currentX + (TANK_WIDTH - barWidth) / 2;  // Center horizontally
			int barY = currentY - BAR_HEIGHT - BAR_VERTICAL_OFFSET;  // Position above tank

			// Draw background (empty bar)
			graphics.setColor(Color.GRAY);
			graphics.fillRect(barX, barY, barWidth, BAR_HEIGHT);

			// Draw health bar
			if (healthPoints > 30) {
				graphics.setColor(Color.GREEN);
			} else {
				graphics.setColor(Color.RED);  // Red when health is low
			}
			int currentBarWidth = (int)((barWidth * healthPoints) / 100.0);
			graphics.fillRect(barX, barY, currentBarWidth, BAR_HEIGHT);

			// Draw border
			graphics.setColor(Color.DARK_GRAY);
			graphics.drawRect(barX, barY, barWidth, BAR_HEIGHT);

			graphics.setColor(originalColor);
		}
	}

	/**
	 * Handles collection of health power-up
	 */
	public boolean collectHealthPowerUp(Blood healthPowerUp) {
		if(this.isAlive && healthPowerUp.isActive() &&
				this.getCollisionBounds().intersects(healthPowerUp.getCollisionBounds())) {
			this.healthPoints = 100;
			healthPowerUp.setActive(false);
			return true;
		}
		return false;
	}

	// Add necessary getter/setter methods
	public int getPositionX() { return currentX; }
	public int getPositionY() { return currentY; }
	public void setBarrelDirection(Direction dir) { this.barrelDirection = dir; }
	public void setMovementDirection(Direction dir) { this.moveDirection = dir; }
}

/**
 * TankGenerator Class - Handles tank spawning logic and positioning
 * Ensures tanks spawn in valid positions without overlapping walls or other tanks
 */
class TankGenerator {
	private final TankClient gameClient;
	private final Random random = new Random();

	// Constants for spawn settings
	private static final int SPAWN_PADDING = 50;  // Minimum distance from edges
	private static final int MAX_SPAWN_ATTEMPTS = 50;  // Maximum attempts to find valid position

	public TankGenerator(TankClient gameClient) {
		this.gameClient = gameClient;
	}

	/**
	 * Checks if position is valid for tank spawn
	 */
	private boolean isValidSpawnPosition(int x, int y) {
		// Create bounds for potential tank position
		Rectangle tankBounds = new Rectangle(x, y, Tank.TANK_WIDTH, Tank.TANK_HEIGHT);

		// Add padding around tank
		Rectangle paddedBounds = new Rectangle(
				x - SPAWN_PADDING/2,
				y - SPAWN_PADDING/2,
				Tank.TANK_WIDTH + SPAWN_PADDING,
				Tank.TANK_HEIGHT + SPAWN_PADDING
		);

		// Check collision with walls
		for (Wall wall : gameClient.getWalls()) {
			if (paddedBounds.intersects(wall.getCollisionBounds())) {
				return false;
			}
		}

		// Check collision with existing tanks
		for (Tank tank : gameClient.getEnemyTanks()) {
			if (paddedBounds.intersects(tank.getCollisionBounds())) {
				return false;
			}
		}

		// Check collision with player tank
		if (paddedBounds.intersects(gameClient.getPlayerTank().getCollisionBounds())) {
			return false;
		}

		// Check if too close to screen edges
		return !(x < SPAWN_PADDING ||
				y < SPAWN_PADDING ||
				x + Tank.TANK_WIDTH > TankClient.GAME_WIDTH - SPAWN_PADDING ||
				y + Tank.TANK_HEIGHT > TankClient.GAME_HEIGHT - SPAWN_PADDING);
	}

	/**
	 * Finds a valid spawn position for a tank
	 * @return Point representing valid spawn position, or null if none found
	 */
	private Point findValidSpawnPosition() {
		for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++) {
			int x = random.nextInt(TankClient.GAME_WIDTH - Tank.TANK_WIDTH - SPAWN_PADDING * 2) + SPAWN_PADDING;
			int y = random.nextInt(TankClient.GAME_HEIGHT - Tank.TANK_HEIGHT - SPAWN_PADDING * 2) + SPAWN_PADDING;

			if (isValidSpawnPosition(x, y)) {
				return new Point(x, y);
			}
		}
		return null;
	}

	/**
	 * Spawns enemy tanks in valid positions
	 * @param count Number of tanks to spawn
	 * @return Number of successfully spawned tanks
	 */
	public int spawnEnemyTanks(int count) {
		int successfulSpawns = 0;
		List<Tank> enemyTanks = gameClient.getEnemyTanks();

		// Clear existing tanks
		enemyTanks.clear();

		// Try to spawn new tanks
		for (int i = 0; i < count; i++) {
			Point spawnPoint = findValidSpawnPosition();
			if (spawnPoint != null) {
				Tank.Direction randomDirection = Tank.Direction.values()[
						random.nextInt(Tank.Direction.values().length - 1)  // Exclude STOP
						];
				enemyTanks.add(new Tank(spawnPoint.x, spawnPoint.y, false, randomDirection, gameClient));
				successfulSpawns++;
			}
		}

		return successfulSpawns;
	}
}