import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

/**
 * TankClient Class - Main game window and controller
 * Manages game state, rendering, and user input
 */
public class TankClient extends Frame {
	/**
	 * Game window dimensions
	 */
	public static final int GAME_WIDTH = 800;
	public static final int GAME_HEIGHT = 600;

	// Player tank instance
	private final Tank playerTank = new Tank(50, 50, true, Tank.Direction.STOP, this);

	// Add WallManager instance
	private final WallManager wallManager;

	private final TankGenerator tankGenerator;

	// Game object collections
	private final List<Explode> explosions = new ArrayList<>();
	private final List<Missile> missiles = new ArrayList<>();
	private final List<Tank> enemyTanks = new ArrayList<>();

	// Double buffering image
	private Image offScreenImage = null;

	// Health power-up
	private final Blood healthPack = new Blood();

	public TankClient() {
		// First create wall manager and generate walls
		wallManager = new WallManager(this);
		wallManager.generateRandomWalls(5);

		// Then create tank generator and initialize tanks
		tankGenerator = new TankGenerator(this);
	}



	/**
	 * Renders the game state
	 * @param graphics Graphics context for rendering
	 */
	public void paint(Graphics graphics) {
		// Draw game statistics
		drawGameStats(graphics);

		// Spawn enemy tanks if needed
		spawnEnemyTanks();

		// Update and render missiles
		updateMissiles(graphics);

		// Update and render explosions
		updateExplosions(graphics);

		// Update and render enemy tanks
		updateEnemyTanks(graphics);

		// Update and render player tank
		playerTank.render(graphics);
		playerTank.collectHealthPowerUp(healthPack);

		healthPack.renderHealthPack(graphics);

		// Update and render walls
		wallManager.update(System.currentTimeMillis());
		wallManager.render(graphics);
	}

	/**
	 * Draws game statistics on screen
	 */
	private void drawGameStats(Graphics graphics) {
		graphics.drawString("Missiles Count: " + missiles.size(), 10, 50);
		graphics.drawString("Explosions Count: " + explosions.size(), 10, 70);
		graphics.drawString("Enemy Tanks Count: " + enemyTanks.size(), 10, 90);
		graphics.drawString("Player Health: " + playerTank.getHealthPoints(), 10, 110);
	}


	/**
	 * Spawns enemy tanks when needed
	 */
	private void spawnEnemyTanks() {
		if (enemyTanks.isEmpty()) {
			tankGenerator.spawnEnemyTanks(5);
		}
	}

	/**
	 * Updates and renders missiles
	 */
	private void updateMissiles(Graphics graphics) {
		for(int i = 0; i < missiles.size(); i++) {
			Missile missile = missiles.get(i);
			// Check wall collisions first
			if (wallManager.handleMissileCollision(missile)) {
				continue; // Skip other collision checks if missile hit a wall
			}
			// Only check tank collisions if missile didn't hit a wall
			missile.handleTankCollisions(enemyTanks);
			missile.handleTankCollision(playerTank);
			missile.render(graphics);
		}
	}

	/**
	 * Updates and renders explosions
	 */
	private void updateExplosions(Graphics graphics) {
		for(int i = 0; i < explosions.size(); i++) {
			Explode explosion = explosions.get(i);
			explosion.renderExplosion(graphics);
		}
	}

	/**
	 * Updates and renders enemy tanks
	 */
	private void updateEnemyTanks(Graphics graphics) {
		for(int i = 0; i < enemyTanks.size(); i++) {
			Tank tank = enemyTanks.get(i);
			wallManager.handleTankCollision(tank);
			tank.handleTankCollisions(enemyTanks);
			tank.render(graphics);
		}
	}

	/**
	 * Implements double buffering for smooth rendering
	 */
	public void update(Graphics graphics) {
		if(offScreenImage == null) {
			offScreenImage = createImage(GAME_WIDTH, GAME_HEIGHT);
		}
		Graphics offGraphics = offScreenImage.getGraphics();
		Color originalColor = offGraphics.getColor();
		// Change background color to light gray
		offGraphics.setColor(new Color(232, 232, 232)); // Light gray
		// Alternative colors:
		// offGraphics.setColor(new Color(245, 245, 245)); // Lighter gray
		// offGraphics.setColor(new Color(230, 230, 240)); // Light blue-gray
		offGraphics.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
		offGraphics.setColor(originalColor);
		paint(offGraphics);
		graphics.drawImage(offScreenImage, 0, 0, null);
	}

	/**
	 * Initializes and launches the game window
	 */
	public void launchGame() {
		// Window setup first
		setSize(GAME_WIDTH, GAME_HEIGHT);
		setTitle("Tank War");
		setResizable(false);
		setBackground(new Color(189, 174, 174));

		// Then initialize game objects
		spawnEnemyTanks();

		// Window event handlers
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		addKeyListener(new KeyMonitor());

		// Finally make window visible and start game loop
		setVisible(true);
		new Thread(new PaintThread()).start();
	}

	/**
	 * Gets the player tank instance
	 * @return Player tank
	 */
	public Tank getPlayerTank() {
		return playerTank;
	}

	public Wall[] getWalls() {
		return wallManager.getAllWalls();
	}

	/**
	 * Game rendering thread
	 */
	private class PaintThread implements Runnable {
		public void run() {
			while(true) {
				repaint();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Keyboard input handler
	 */
	private class KeyMonitor extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			playerTank.handleKeyReleased(e);
		}

		public void keyPressed(KeyEvent e) {
			playerTank.handleKeyPressed(e);
		}
	}

	// Getter methods for collections
	public List<Missile> getMissiles() {
		return missiles;
	}

	public List<Explode> getExplosions() {
		return explosions;
	}

	public List<Tank> getEnemyTanks() {
		return enemyTanks;
	}
}