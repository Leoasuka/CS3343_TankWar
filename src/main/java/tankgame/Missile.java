package tankgame;
import java.awt.*;
import java.util.List;

/**
 * Missile Class - Represents a projectile fired by tanks
 * Handles missile movement, collision detection, and damage dealing
 */
public class Missile {
	// Constants for missile movement speed
	public static final int MOVEMENT_SPEED_X = 10;
	public static final int MOVEMENT_SPEED_Y = 10;

	// Constants for missile dimensions
	public static final int MISSILE_WIDTH = 10;
	public static final int MISSILE_HEIGHT = 10;

	// Missile position coordinates
	private int positionX, positionY;

	// Missile movement direction
	private Tank.Direction direction;

	// Missile properties
	private boolean isFromPlayerTank;
	private boolean isActive = true;

	// Reference to game client
	private TankClient gameClient;

	/**
	 * Basic constructor for missile
	 * @param x Initial X coordinate
	 * @param y Initial Y coordinate
	 * @param direction Movement direction
	 */
	public Missile(int x, int y, Tank.Direction direction) {
		this.positionX = x;
		this.positionY = y;
		this.direction = direction;
	}

	/**
	 * Extended constructor with additional properties
	 * @param x Initial X coordinate
	 * @param y Initial Y coordinate
	 * @param isPlayerMissile Whether missile is from player tank
	 * @param direction Movement direction
	 * @param client Game client reference
	 */
	public Missile(int x, int y, boolean isPlayerMissile, Tank.Direction direction, TankClient client) {
		this(x, y, direction);
		this.isFromPlayerTank = isPlayerMissile;
		this.gameClient = client;
	}

	/**
	 * Renders and updates missile state
	 * @param graphics Graphics context for rendering
	 */
	public void render(Graphics graphics) {
		if(!isActive) {
			gameClient.getMissiles().remove(this);
			return;
		}

		// Draw enhanced missile
		Color originalColor = graphics.getColor();
		graphics.setColor(Color.BLACK);
		// Draw missile using filled circle
		graphics.fillOval(positionX, positionY, MISSILE_WIDTH, MISSILE_HEIGHT);
		// Add missile glow effect
		graphics.setColor(new Color(255, 200, 0, 150));
		graphics.fillOval(positionX-1, positionY-1, MISSILE_WIDTH+2, MISSILE_HEIGHT+2);

		graphics.setColor(originalColor);

		updatePosition();
	}

	/**
	 * Updates missile position based on direction
	 */
	private void updatePosition() {
		switch(direction) {
			case L:
				positionX -= MOVEMENT_SPEED_X;
				break;
			case LU:
				positionX -= MOVEMENT_SPEED_X;
				positionY -= MOVEMENT_SPEED_Y;
				break;
			case U:
				positionY -= MOVEMENT_SPEED_Y;
				break;
			case RU:
				positionX += MOVEMENT_SPEED_X;
				positionY -= MOVEMENT_SPEED_Y;
				break;
			case R:
				positionX += MOVEMENT_SPEED_X;
				break;
			case RD:
				positionX += MOVEMENT_SPEED_X;
				positionY += MOVEMENT_SPEED_Y;
				break;
			case D:
				positionY += MOVEMENT_SPEED_Y;
				break;
			case LD:
				positionX -= MOVEMENT_SPEED_X;
				positionY += MOVEMENT_SPEED_Y;
				break;
			case STOP:
				break;
		}

		// Check if missile is out of bounds
		checkBoundaryCollision();
	}

	/**
	 * Checks if missile has left game boundaries
	 */
	private void checkBoundaryCollision() {
		if(positionX < 0 || positionY < 0 ||
				positionX > TankClient.GAME_WIDTH ||
				positionY > TankClient.GAME_HEIGHT) {
			isActive = false;
		}
	}

	/**
	 * Checks if missile is still active
	 * @return boolean indicating missile's active status
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * Gets missile's collision rectangle
	 * @return Rectangle representing missile's bounds
	 */
	public Rectangle getCollisionBounds() {
		return new Rectangle(positionX, positionY, MISSILE_WIDTH, MISSILE_HEIGHT);
	}

	/**
	 * Handles collision with a single tank
	 * @param tank Tank to check collision with
	 * @return boolean indicating if collision occurred
	 */
	public boolean handleTankCollision(Tank tank) {
		if(this.isActive &&
				this.getCollisionBounds().intersects(tank.getCollisionBounds()) &&
				tank.isAlive() &&
				this.isFromPlayerTank != tank.isPlayerControlled()) {

			if(tank.isPlayerControlled()) {
				// Reduce player tank health
				tank.setHealthPoints(tank.getHealthPoints() - 20);
				if(tank.getHealthPoints() <= 0) {
					tank.setAlive(false);
				}
			} else {
				// Destroy enemy tank immediately
				tank.setAlive(false);
				Explode explosion = new Explode(positionX, positionY, gameClient);
				gameClient.setScore(gameClient.getScore() + 10);
			    gameClient.getExplosions().add(explosion);
			}

			// Deactivate missile and create explosion
			this.isActive = false;
			
			return true;
		}
		return false;
	}

	/**
	 * Handles collisions with multiple tanks
	 * @param tanks List of tanks to check collisions with
	 * @return boolean indicating if any collision occurred
	 */
	public boolean handleTankCollisions(List<Tank> tanks) {
		for(Tank tank : tanks) {
			if(handleTankCollision(tank)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Handles collision with walls
	 * @param wall Wall to check collision with
	 * @return boolean indicating if collision occurred
	 */
	public boolean handleWallCollision(Wall wall) {
		if(this.isActive && this.getCollisionBounds().intersects(wall.getCollisionBounds())) {
			// Deactivate missile but don't create explosion
			this.isActive = false;
			wall.handleMissileCollision(this);
			return true;
		}
		return false;
	}

	public int getPositionX() {
		return positionX;
	}

	public int getPositionY() {
		return positionY;
	}
}