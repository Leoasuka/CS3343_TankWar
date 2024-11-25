package Resources;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JPanel;

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
	private Tank playerTank = new Tank(50, 50, true, Tank.Direction.STOP, this);

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

	//gameover state button
	private JButton playAgainButton;
    private JButton quitButton;


	//gameState gameover/running
	enum GameState { RUNNING, GAME_OVER }
    private GameState gameState = GameState.RUNNING;
    private int score = explosions.size()*10;


    public void setGameState(GameState state) {
        this.gameState = state;
    }

    public GameState getGameState() {
        return gameState;
    }
    
	public void setScore(int number){
		this.score = number*10;
	}
    

    public int getScore() {
        return score;
    }

	public TankClient() {
		// First create wall manager and generate walls
		wallManager = new WallManager(this);
		wallManager.generateRandomWalls(5);

		// Then create tank generator and initialize tanks
		tankGenerator = new TankGenerator(this);
		//initGameOverButtons();

		 // 设置布局和其他窗口属性
		 setLayout(new BorderLayout());
		 setSize(GAME_WIDTH, GAME_HEIGHT);
		 setTitle("Tank War");
		 setResizable(false);
		 setBackground(new Color(189, 174, 174));
	 
		 // 窗口事件处理
		 addWindowListener(new WindowAdapter() {
			 public void windowClosing(WindowEvent e) {
				 System.exit(0);
			 }
		 });
		 addKeyListener(new KeyMonitor());
	 
		 setVisible(true);
		 new Thread(new PaintThread()).start();
	}



	/**
	 * Renders the game state
	 * @param graphics Graphics context for rendering
	 */
	public void paint(Graphics graphics) {
		if (gameState == GameState.GAME_OVER) {
            renderGameOver(graphics);
        } else {
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
	 * load game over
	 */
	private void renderGameOver(Graphics g) {
		// Set color to red for the "GAME OVER" text
		g.setColor(Color.RED);
		g.setFont(new Font("Arial", Font.BOLD, 50));
		g.drawString("GAME OVER", 100, 200);
	
		// Set font and draw the score
		g.setFont(new Font("Arial", Font.PLAIN, 30));
		g.drawString("Score: " + score, 100, 250);
	
        playAgainButton = new JButton("Play Again");
        playAgainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        });

        quitButton = new JButton("Quit");
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // 将按钮添加到窗口的南边（底部）
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(playAgainButton);
        buttonPanel.add(quitButton);
        add(buttonPanel, BorderLayout.SOUTH);
		validate(); // 重新验证布局

	}
	
	private void restartGame() {
	    // Reset player tank
	    playerTank = new Tank(50, 50, true, Tank.Direction.STOP, this);
	    
	    // Clear enemy tanks, missiles, and explosions
	    enemyTanks.clear();
	    missiles.clear();
	    explosions.clear();
	    
	    // Spawn new enemy tanks
	    tankGenerator.spawnEnemyTanks(5);
	    
	    // Reset score
	    score = 0; 

	    // Reset game state to RUNNING
	    setGameState(GameState.RUNNING);
	    
	    // Remove any existing key listeners
	    for (KeyListener kl : getKeyListeners()) {
	        removeKeyListener(kl);
	    }
	    
	    // Add a new key listener
	    addKeyListener(new KeyMonitor());
	    
	    // Make sure the frame is focused and ready to receive key events
	    requestFocusInWindow();

	    // Repaint and validate the window
	    validate(); 
	    repaint();
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