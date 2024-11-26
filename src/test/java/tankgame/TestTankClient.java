package tankgame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestTankClient {
    private TankClient tankClient;

    @BeforeEach
    void setUp() {
        tankClient = new TankClient();
        tankClient.setGameState(TankClient.GameState.RUNNING);
        tankClient.getMissiles().clear();
        tankClient.getExplosions().clear();
        tankClient.getEnemyTanks().clear();
        tankClient.setScore(0);
    }

    @AfterEach
    void tearDown() {
        tankClient = null;  // 清理引用
    }

    @Test
    void testGameInitialization() {
        // 验证游戏初始状态
        assertEquals(TankClient.GameState.RUNNING, tankClient.getGameState());
        assertNotNull(tankClient.getPlayerTank());
        assertTrue(tankClient.getMissiles().isEmpty());
        assertTrue(tankClient.getExplosions().isEmpty());
        assertTrue(tankClient.getEnemyTanks().isEmpty());
    }

    @Test
    void testPaintGameOver() {
        Graphics graphics = mock(Graphics.class); // Mock Graphics 对象
        tankClient.setGameState(TankClient.GameState.GAME_OVER);

        tankClient.paint(graphics);

        verify(graphics, times(1)).setColor(Color.RED);
        verify(graphics, times(1)).setFont(new Font("Arial", Font.BOLD, 50));
        verify(graphics, times(1)).drawString("GAME OVER", 100, 200);
    }

    @Test
    void testPaintRunning() {
        // Create a BufferedImage to get a Graphics2D object
        BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = bufferedImage.createGraphics();

        // Mock the Graphics2D object
        Graphics2D graphics = spy(graphics2D);

        // Set game state to RUNNING
        tankClient.setGameState(TankClient.GameState.RUNNING);

        // Call the paint method
        tankClient.paint(graphics);

        // Verify that game statistics and game objects are rendered
        // verify(graphics, times(1)).drawString(contains("Missiles Count:"), eq(10), eq(50));
        // verify(graphics, times(1)).drawString(contains("Explosions Count:"), eq(10), eq(70));
        verify(graphics, times(1)).drawString(contains("Enemy Tanks Count:"), eq(10), eq(50));
        verify(graphics, times(1)).drawString(contains("Player Health:"), eq(10), eq(70));
        verify(graphics, times(1)).drawString(contains("Score:"), eq(10), eq(90));
    }

    @Test
    void testPlayerTankInteraction() {
        // 创建玩家坦克
        Tank playerTank = tankClient.getPlayerTank();
        playerTank.setHealthPoints(0); // 将玩家坦克的生命值设置为 0

        // 验证游戏状态是否切换到 GAME_OVER
        assertEquals(TankClient.GameState.GAME_OVER, tankClient.getGameState());
    }

    @Test
    void testEnemyTankSpawning() {
        // 初始敌人坦克列表为空
        assertTrue(tankClient.getEnemyTanks().isEmpty());

        // 调用敌人坦克生成逻辑
        tankClient.spawnEnemyTanks();

        // 验证是否生成了敌人坦克
        assertFalse(tankClient.getEnemyTanks().isEmpty());
    }

    @Test
    void testMissileUpdateAndRender() {
        // 创建导弹并添加到游戏中
        Missile missile = new Missile(100, 100, true, Tank.Direction.LU, tankClient);
        tankClient.getMissiles().add(missile);

        // 模拟渲染逻辑
        Graphics graphics = new Canvas().getGraphics();
        tankClient.updateMissiles(graphics);

        // 验证导弹是否被渲染
        assertEquals(1, tankClient.getMissiles().size());
        assertEquals(100, tankClient.getMissiles().get(0).getPositionX());
    }

    @Test
    void testHealthPackInteraction() {
        // 创建玩家坦克
        Tank playerTank = tankClient.getPlayerTank();

        // 创建血包
        Blood healthPack = new Blood(100, 100);
        healthPack.setActive(true);

        // 模拟玩家拾取血包
        playerTank.setCurrentX(100);
        playerTank.setCurrentY(100);
        playerTank.collectHealthPowerUp(healthPack);

        // 验证血包是否被拾取并设置为非激活状态
        assertFalse(healthPack.isActive());
        assertTrue(playerTank.getHealthPoints() > 0); // 假设血包会增加玩家的血量
    }

    @Test
    void testRestartGame() {
        // 模拟游戏重启
        tankClient.restartGame();

        // 验证游戏状态是否重置
        assertEquals(TankClient.GameState.RUNNING, tankClient.getGameState());

        // 验证游戏对象是否清空
        assertTrue(tankClient.getMissiles().isEmpty());
        assertTrue(tankClient.getExplosions().isEmpty());
        assertEquals(0, tankClient.getScore());
        assertEquals(5, tankClient.getEnemyTanks().size());
    }

    @Test
    void testWindowClosing() {
        // 创建 TankClient 实例
        TankClient tankClient = new TankClient();

        // 创建一个 AWTEventListener 来捕获窗口关闭事件
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof WindowEvent) {
                WindowEvent windowEvent = (WindowEvent) event;
                if (windowEvent.getID() == WindowEvent.WINDOW_CLOSING) {
                    // 验证窗口关闭事件
                    assertEquals(WindowEvent.WINDOW_CLOSING, windowEvent.getID());
                }
            }
        }, AWTEvent.WINDOW_EVENT_MASK);

        // 模拟窗口关闭事件
        WindowEvent windowClosingEvent = new WindowEvent(tankClient, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(windowClosingEvent);
    }

    @Test
    void testKeyMonitorKeyPressed() {
        // Mock playerTank
        Tank mockPlayerTank = mock(Tank.class);
        tankClient.setPlayerTank(mockPlayerTank);

        // Create a mock KeyEvent
        KeyEvent keyEvent = mock(KeyEvent.class);

        // Get KeyMonitor instance using the public method
        TankClient.KeyMonitor keyMonitor = tankClient.getKeyMonitor();

        // Call keyPressed
        keyMonitor.keyPressed(keyEvent);

        // Verify that handleKeyPressed was called on playerTank
        verify(mockPlayerTank, times(1)).handleKeyPressed(keyEvent);
    }

    @Test
    void testKeyMonitorKeyReleased() {
        // Mock playerTank
        Tank mockPlayerTank = mock(Tank.class);
        tankClient.setPlayerTank(mockPlayerTank);

        // Create a mock KeyEvent
        KeyEvent keyEvent = mock(KeyEvent.class);

        // Get KeyMonitor instance
        TankClient.KeyMonitor keyMonitor = tankClient.new KeyMonitor();

        // Call keyReleased
        keyMonitor.keyReleased(keyEvent);

        // Verify that handleKeyReleased was called on playerTank
        verify(mockPlayerTank, times(1)).handleKeyReleased(keyEvent);
    }

    @Test
    void testUpdateExplosions() {
        // Mock Graphics 对象
        Graphics graphics = mock(Graphics.class);

        // Mock Explode 对象
        Explode explosion1 = mock(Explode.class);
        Explode explosion2 = mock(Explode.class);

        // 将 Mock 的爆炸对象添加到 explosions 列表中
        tankClient.getExplosions().add(explosion1);
        tankClient.getExplosions().add(explosion2);

        // 调用 updateExplosions 方法
        tankClient.updateExplosions(graphics);

        // 验证每个爆炸对象的 renderExplosion 方法是否被调用
        verify(explosion1, times(1)).renderExplosion(graphics);
        verify(explosion2, times(1)).renderExplosion(graphics);

        // 验证 explosions 列表中所有对象都被正确处理
        assertEquals(2, tankClient.getExplosions().size());
    }

    @Test
    void testUpdateExplosionsWithEmptyList() {
        // Mock Graphics 对象
        Graphics graphics = mock(Graphics.class);

        // 确保 explosions 列表为空
        tankClient.getExplosions().clear();

        // 调用 updateExplosions 方法
        tankClient.updateExplosions(graphics);

        // 验证 explosions 列表为空时 renderExplosion 不被调用
        assertTrue(tankClient.getExplosions().isEmpty());
        verifyNoInteractions(graphics); // 确保没有与 Graphics 对象交互
    }

    @Test
    void testLaunchGame() {
        // Call the launchGame method
        tankClient.launchGame();

        // Verify window properties
        assertEquals(TankClient.GAME_WIDTH, tankClient.getWidth());
        assertEquals(TankClient.GAME_HEIGHT, tankClient.getHeight());
        assertEquals("Tank War", tankClient.getTitle());
        assertFalse(tankClient.isResizable());
        assertEquals(new Color(189, 174, 174), tankClient.getBackground());

        // Verify enemy tanks are spawned
        assertFalse(tankClient.getEnemyTanks().isEmpty());

        // Verify the game window is visible
        assertTrue(tankClient.isVisible());
    }

    @Test
    void testRestartGameButtonAction() {
        // Simulate the game over state
        tankClient.setGameState(TankClient.GameState.GAME_OVER);
        tankClient.renderGameOver(tankClient.getGraphics(), tankClient.getScore());

        // Simulate button click
        ActionEvent event = new ActionEvent(tankClient.playAgainButton, ActionEvent.ACTION_PERFORMED, "Play Again");
        tankClient.playAgainButton.getActionListeners()[0].actionPerformed(event);

        // Verify that the game state is reset to RUNNING
        assertEquals(TankClient.GameState.RUNNING, tankClient.getGameState());
    }

}