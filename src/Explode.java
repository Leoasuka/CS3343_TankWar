import java.awt.*;

/**
 * Explode Class - Represents an explosion animation in the tank game
 * Creates a dynamic explosion effect using circles of varying sizes
 */
public class Explode {
	// Position coordinates of the explosion
	private final int positionX;
    private final int positionY;

	// Status flag indicating if the explosion animation is active
	private boolean isActive = true;

	// Reference to the main game client
	private final TankClient gameClient;

	/**
	 * Array defining the explosion animation sequence
	 * Contains diameters for each frame of the explosion animation
	 */
	private final int[] explosionSizes = {4, 7, 12, 18, 26, 32, 49, 30, 14, 6};

	// Current frame index in the explosion animation
	private int animationFrame = 0;

	/**
	 * Constructor - Initializes a new explosion
	 * @param posX Initial X coordinate of the explosion
	 * @param posY Initial Y coordinate of the explosion
	 * @param client Reference to the main game client
	 */
	public Explode(int posX, int posY, TankClient client) {
		this.positionX = posX;
		this.positionY = posY;
		this.gameClient = client;
	}

	/**
	 * Check explosion state
	 * @return whether to continue rendering
	 */
	private boolean checkExplosionState() {
		if(!isActive) {
			gameClient.getExplosions().remove(this);
			return false;
		}

		if(animationFrame == explosionSizes.length) {
			isActive = false;
			animationFrame = 0;
			return false;
		}
		return true;
	}

	/**
	 * Draw current explosion frame
	 * @param graphics Graphics context
	 */
	private void drawExplosionFrame(Graphics graphics) {
		Color originalColor = graphics.getColor();
		graphics.setColor(Color.ORANGE);
		int currentSize = explosionSizes[animationFrame];
		graphics.fillOval(positionX, positionY, currentSize, currentSize);
		graphics.setColor(originalColor);
	}

	/**
	 * Update animation state
	 */
	private void updateAnimation() {
		animationFrame++;
	}

	/**
	 * Render and update explosion animation
	 * @param graphics Graphics context
	 */
	public void renderExplosion(Graphics graphics) {
		if(!checkExplosionState()) {
			return;
		}
		drawExplosionFrame(graphics);
		updateAnimation();
	}
}