import java.awt.*;

/**
 * Wall Class - Represents an obstacle wall in the tank game
 * Walls are rectangular barriers that block tank and missile movement
 */
public class Wall {
	// Wall position coordinates
	private final int positionX;
	private final int positionY;

	// Wall dimensions
	private final int width;
	private final int height;

    /**
	 * Constructor for creating a wall
	 * @param x Initial X coordinate of the wall
	 * @param y Initial Y coordinate of the wall
	 * @param width Width of the wall
	 * @param height Height of the wall
	 * @param client Reference to the game client
	 */
	public Wall(int x, int y, int width, int height, TankClient client) {
		this.positionX = x;
		this.positionY = y;
		this.width = width;
		this.height = height;
        // Reference to game client
    }

	/**
	 * Renders the wall on the game screen
	 * @param graphics Graphics context for rendering
	 */
	public void render(Graphics graphics) {
		Color originalColor = graphics.getColor();
		graphics.setColor(Color.GRAY);  // Set wall color to gray
		graphics.fillRect(positionX, positionY, width, height);
		graphics.setColor(originalColor);
	}

	/**
	 * Handles missile collision with wall
	 * @param missile Missile to check collision with
	 * @return true if collision occurred
	 */
	public boolean handleMissileCollision(Missile missile) {
        // Basic walls just block missiles without taking damage
        return getCollisionBounds().intersects(missile.getCollisionBounds());
    }

	/**
	 * Calculates the nearest point on the wall to a given position
	 * @param x X coordinate of position
	 * @param y Y coordinate of position
	 * @return Point representing nearest position on wall
	 */
	public Point getNearestPoint(int x, int y) {
		int nearestX = Math.max(positionX, Math.min(x, positionX + width));
		int nearestY = Math.max(positionY, Math.min(y, positionY + height));
		return new Point(nearestX, nearestY);
	}

	/**
	 * Checks if a point is behind this wall relative to a reference point
	 * @param pointX X coordinate to check
	 * @param pointY Y coordinate to check
	 * @param referenceX X coordinate of reference point
	 * @param referenceY Y coordinate of reference point
	 * @return true if point is behind wall
	 */
	public boolean isPointBehindWall(int pointX, int pointY, int referenceX, int referenceY) {
		// Vector from reference to wall center
		double wallCenterX = positionX + width/2.0;
		double wallCenterY = positionY + height/2.0;
		double toWallX = wallCenterX - referenceX;
		double toWallY = wallCenterY - referenceY;

		// Vector from reference to point
		double toPointX = pointX - referenceX;
		double toPointY = pointY - referenceY;

		// Dot product to determine if point is behind wall
		return (toWallX * toPointX + toWallY * toPointY) > 0;
	}

	/**
	 * Gets safe positions around the wall for cover
	 * @return Array of points representing safe positions
	 */
	public Point[] getCoverPositions() {
		return new Point[] {
				new Point(positionX - 40, positionY), // Left
				new Point(positionX + width + 40, positionY), // Right
				new Point(positionX, positionY - 40), // Top
				new Point(positionX, positionY + height + 40) // Bottom
		};
	}

	/**
	 * Gets the collision bounds of the wall
	 * Used for collision detection with tanks and missiles
	 * @return Rectangle representing the wall's bounds
	 */
	public Rectangle getCollisionBounds() {
		return new Rectangle(positionX, positionY, width, height);
	}

	/**
	 * Gets the X coordinate of the wall
	 * @return Current X position
	 */
	public int getPositionX() {
		return positionX;
	}

	/**
	 * Gets the Y coordinate of the wall
	 * @return Current Y position
	 */
	public int getPositionY() {
		return positionY;
	}

	/**
	 * Gets the width of the wall
	 * @return Wall width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Gets the height of the wall
	 * @return Wall height
	 */
	public int getHeight() {
		return height;
	}
}
