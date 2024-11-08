import java.awt.*;
import java.awt.event.*;
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
	enum Direction {L, LU, U, RU, R, RD, D, LD, STOP}

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

		// Draw tank body
		Color originalColor = graphics.getColor();
		graphics.setColor(isPlayerTank ? Color.RED : Color.BLUE);
		// Draw tank chassis
		graphics.fillRect(currentX, currentY, TANK_WIDTH, TANK_HEIGHT);

		// Add track details
		graphics.setColor(Color.DARK_GRAY);
		graphics.fillRect(currentX - 2, currentY, 2, TANK_HEIGHT); // Left track
		graphics.fillRect(currentX + TANK_WIDTH, currentY, 2, TANK_HEIGHT); // Right track

		// Add turret
		int turretWidth = 20;
		int turretHeight = 20;
		int turretX = currentX + (TANK_WIDTH - turretWidth) / 2;
		int turretY = currentY + (TANK_HEIGHT - turretHeight) / 2;
		graphics.setColor(Color.DARK_GRAY);
		graphics.fillOval(turretX, turretY, turretWidth, turretHeight);

		graphics.setColor(originalColor);

		// Draw health bar for player tank
		if(isPlayerTank) healthBar.render(graphics);

		// Draw tank barrel
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

		// Draw longer barrel based on direction
		int barrelLength = 25;
		switch(barrelDirection) {
			case L:
				graphics.drawLine(centerX, centerY, centerX - barrelLength, centerY);
				break;
			case LU:
				graphics.drawLine(centerX, centerY, centerX - barrelLength, centerY - barrelLength);
				break;
			case U:
				graphics.drawLine(centerX, centerY, centerX, centerY - barrelLength);
				break;
			case RU:
				graphics.drawLine(centerX, centerY, centerX + barrelLength, centerY - barrelLength);
				break;
			case R:
				graphics.drawLine(centerX, centerY, centerX + barrelLength, centerY);
				break;
			case RD:
				graphics.drawLine(centerX, centerY, centerX + barrelLength, centerY + barrelLength);
				break;
			case D:
				graphics.drawLine(centerX, centerY, centerX, centerY + barrelLength);
				break;
			case LD:
				graphics.drawLine(centerX, centerY, centerX - barrelLength, centerY + barrelLength);
				break;
		}
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
			case KeyEvent.VK_CONTROL:
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
	}

	/**
	 * Inner class representing tank's health bar
	 */
	private class HealthBar {
		public void render(Graphics graphics) {
			Color originalColor = graphics.getColor();
			graphics.setColor(Color.RED);
			graphics.drawRect(currentX, currentY-10, TANK_WIDTH, 10);
			int healthBarWidth = TANK_WIDTH * healthPoints/100;
			graphics.fillRect(currentX, currentY-10, healthBarWidth, 10);
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