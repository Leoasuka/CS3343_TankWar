package tankgame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

class TestTank {

    private TankGenerator tankGenerator;
    private Tank tank;
    private TankClient gameClient;
    private Graphics graphics;

    private static class StubTankAI extends TankAI {
        private boolean updateCalled = false;

        public StubTankAI(Tank tank, TankClient client) {
            super(tank, client);
        }

        @Override
        public void update() {
            updateCalled = true;
        }

        public boolean wasUpdateCalled() {
            return updateCalled;
        }
    }

    @BeforeEach
    void setUp() {
        gameClient = mock(TankClient.class);
        graphics = mock(Graphics.class);
        tank = new Tank(100, 100, true, Tank.Direction.STOP, gameClient);
        tankGenerator = new TankGenerator(gameClient);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, 200, 200);
    }

    private KeyEvent createKeyEvent(final int keyCode) {
        return new KeyEvent(new javax.swing.JComponent() {},
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                0,
                keyCode,
                (char) keyCode);
    }

    @Test
    void testConstructor() {
        assertEquals(100, tank.getPositionX());
        assertEquals(100, tank.getPositionY());
        assertTrue(tank.isPlayerControlled());
    }

    @Test
    void testRenderAlivePlayerTank() {
        Tank tank = new Tank(100, 100, true);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        tank.render(graphics);
        assertEquals(Color.RED.getRGB(), image.getRGB(101, 101));
    }

    @Test
    void testRenderDeadPlayerTank() {
        TankClient mockGameClient = mock(TankClient.class);
        Tank tank = new Tank(100, 100, true, Tank.Direction.STOP, mockGameClient);
        tank.setAlive(false);

        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        tank.render(graphics);
    }

    @Test
    void testRenderAliveNonPlayerTank() {
        Tank tank = new Tank(100, 100, false);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        tank.render(graphics);
        assertEquals(Color.BLUE.getRGB(), image.getRGB(101, 101));
    }

    @Test
    void testRenderDeadNonPlayerTankWithRemoval() {
        TankClient mockGameClient = mock(TankClient.class);
        Tank tank = new Tank(100, 100, false, Tank.Direction.STOP, mockGameClient);
        tank.setAlive(false);

        List<Tank> enemyTanks = new ArrayList<>();
        enemyTanks.add(tank);
        when(mockGameClient.getEnemyTanks()).thenReturn(enemyTanks);

        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        tank.render(graphics);
    }

    @Test
    void testRenderHealthBar() {
        Tank tank = new Tank(100, 100, true);
        tank.setHealthPoints(50);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        tank.render(graphics);
    }

    @Test
    void testRenderBarrel() {
        Tank tank = new Tank(100, 100, true);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        tank.setBarrelDirection(Tank.Direction.R);
        tank.renderBarrel(graphics);
    }

    @Test
    void testTankCollision() {
        Tank tank1 = new Tank(100, 100, true, Tank.Direction.R, gameClient);
        Tank tank2 = new Tank(140, 100, false, Tank.Direction.L, gameClient);

        ArrayList<Tank> enemyTanks = new ArrayList<>();
        enemyTanks.add(tank2);
        when(gameClient.getEnemyTanks()).thenReturn(enemyTanks);

        tank1.updatePosition();
        int originalX = tank1.getPositionX();
        assertTrue(tank1.willCollideWithOtherTanks(tank1.getPositionX() + Tank.MOVEMENT_SPEED_X, tank1.getPositionY()));
        assertEquals(originalX, tank1.getPositionX());
    }

    @Test
    void testWallCollision() {
        Wall wall = new Wall(150, 100, 20, 40, gameClient);
        when(gameClient.getWalls()).thenReturn(new Wall[]{wall});

        tank = new Tank(100, 100, true, Tank.Direction.R, gameClient);
        int originalX = tank.getPositionX();

        assertTrue(tank.willCollideWithWalls(150, 100));
        tank.updatePosition();
        assertEquals(originalX, tank.getPositionX());
    }

    @Test
    void testStopMovement() {
        int originalX = tank.getPositionX();
        int originalY = tank.getPositionY();

        tank.setMovementDirection(Tank.Direction.STOP);
        tank.updatePosition();

        assertEquals(originalX, tank.getPositionX());
        assertEquals(originalY, tank.getPositionY());
    }

    @Test
    void testAIMovement() {
        Tank aiTank = new Tank(100, 100, false, Tank.Direction.STOP, gameClient);
        int originalX = aiTank.getPositionX();
        int originalY = aiTank.getPositionY();

        aiTank.handleAIMovement();
        aiTank.updatePosition();

        assertTrue(aiTank.getPositionX() != originalX || aiTank.getPositionY() != originalY ||
                aiTank.getMoveDirection() != Tank.Direction.STOP);
    }

    @Test
    void testUpdatePositionCollisionWithOtherTanks() {
        Tank tank = new Tank(100, 100, true);
        tank.setMovementDirection(Tank.Direction.L);
        tank.updatePosition();
        assertTrue(tank.getPositionX() < 100);
    }

    @Test
    void testUpdatePositionCollisionWithWalls() {
        Tank tank = new Tank(0, 100, true);
        tank.setMovementDirection(Tank.Direction.L);
        tank.updatePosition();
        assertEquals(0, tank.getPositionX());
    }

    @Test
    void testUpdatePositionOutOfBounds() {
        Tank tank = new Tank(199, 100, true);
        tank.setMovementDirection(Tank.Direction.R);
        tank.updatePosition();
        assertTrue(tank.getPositionX() < 200);
    }

    @Test
    void testUpdatePositionAIMovement() {
        Tank tank = new Tank(100, 100, false);
        tank.setMovementDirection(Tank.Direction.STOP);
        tank.updatePosition();
        assertNotEquals(Tank.Direction.STOP, tank.getMoveDirection());
    }

    @Test
    void testUpdatePosition() {
        tank.setMovementDirection(Tank.Direction.R);
        tank.updatePosition();
        assertEquals(105, tank.getPositionX());
        assertEquals(100, tank.getPositionY());
    }

    @Test
    void testRenderBarrelL() {
        tank = new Tank(100, 100, true, Tank.Direction.D, null);
        tank.setBarrelDirection(Tank.Direction.L);
        tank.renderBarrel(graphics);
        graphics.dispose();
    }

    @Test
    void testRenderBarrelLU() {
        tank = new Tank(100, 100, true, Tank.Direction.D, null);
        tank.setBarrelDirection(Tank.Direction.LU);
        tank.renderBarrel(graphics);
        graphics.dispose();
    }

    @Test
    void testRenderBarrelU() {
        tank = new Tank(100, 100, true, Tank.Direction.D, null);
        tank.setBarrelDirection(Tank.Direction.U);
        tank.renderBarrel(graphics);
        graphics.dispose();
    }

    @Test
    void testRenderBarrelRU() {
        tank = new Tank(100, 100, true, Tank.Direction.D, null);
        tank.setBarrelDirection(Tank.Direction.RU);
        tank.renderBarrel(graphics);
        graphics.dispose();
    }

    @Test
    void testRenderBarrelR() {
        tank = new Tank(100, 100, true, Tank.Direction.D, null);
        tank.setBarrelDirection(Tank.Direction.R);
        tank.renderBarrel(graphics);
        graphics.dispose();
    }

    @Test
    void testRenderBarrelRD() {
        tank = new Tank(100, 100, true, Tank.Direction.D, null);
        tank.setBarrelDirection(Tank.Direction.RD);
        tank.renderBarrel(graphics);
        graphics.dispose();
    }

    @Test
    void testRenderBarrelD() {
        tank = new Tank(100, 100, true, Tank.Direction.D, null);
        tank.setBarrelDirection(Tank.Direction.D);
        tank.renderBarrel(graphics);
        graphics.dispose();
    }

    @Test
    void testRenderBarrelLD() {
        tank = new Tank(100, 100, true, Tank.Direction.D, null);
        tank.setBarrelDirection(Tank.Direction.LD);
        tank.renderBarrel(graphics);
        graphics.dispose();
    }

    @Test
    void testWillCollideWithOtherTanks() {
        Tank otherTank = new Tank(120, 100, false, Tank.Direction.STOP, gameClient);
        when(gameClient.getEnemyTanks()).thenReturn(List.of(otherTank));
        assertTrue(tank.willCollideWithOtherTanks(120, 100));
    }

    @Test
    void testWillCollideWithWalls() {
        Wall wall = new Wall(100, 100, 40, 30, gameClient);
        when(gameClient.getWalls()).thenReturn(new Wall[]{wall});
        assertTrue(tank.willCollideWithWalls(100, 100));
    }

    @Test
    void testCollectHealthPowerUp_TankNotAlive() {
        Tank tank = new Tank(100, 100, true);
        tank.setAlive(false);
        Blood mockBlood = mock(Blood.class);
        when(mockBlood.isActive()).thenReturn(true);

        boolean result = tank.collectHealthPowerUp(mockBlood);

        assertFalse(result);
        verify(mockBlood, never()).setActive(false);
    }

    @Test
    void testCollectHealthPowerUp_PowerUpNotActive() {
        Tank tank = new Tank(100, 100, true);
        Blood mockBlood = mock(Blood.class);
        when(mockBlood.isActive()).thenReturn(false);

        boolean result = tank.collectHealthPowerUp(mockBlood);

        assertFalse(result);
        verify(mockBlood, never()).setActive(false);
    }

    @Test
    void testCollectHealthPowerUp_NoCollision() {
        Tank tank = new Tank(100, 100, true);
        Blood mockBlood = mock(Blood.class);
        when(mockBlood.isActive()).thenReturn(true);
        when(mockBlood.getCollisionBounds()).thenReturn(new Rectangle(150, 150, 10, 10));

        boolean result = tank.collectHealthPowerUp(mockBlood);

        assertFalse(result);
        verify(mockBlood, never()).setActive(false);
    }

    @Test
    void testCollectHealthPowerUp_Success() {
        Tank tank = new Tank(100, 100, true);
        Blood mockBlood = mock(Blood.class);
        when(mockBlood.isActive()).thenReturn(true);
        when(mockBlood.getCollisionBounds()).thenReturn(new Rectangle(105, 105, 10, 10));

        boolean result = tank.collectHealthPowerUp(mockBlood);

        assertTrue(result);
        assertEquals(100, tank.getHealthPoints());
        verify(mockBlood).setActive(false);
    }

    @Test
    void testHandleKeyPressedF2() {
        tank.setAlive(false);
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_F2));
        assertTrue(tank.isAlive());
    }

    @Test
    void testAliveTankHandleKeyPressedF2() {
        tank.setAlive(true);
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_F2));
        assertTrue(tank.isAlive());
    }

    @Test
    void testLeftMovement() {
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_LEFT));
        assertEquals(Tank.Direction.L, tank.getMoveDirection());
    }

    @Test
    void testRightMovement() {
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_RIGHT));
        assertEquals(Tank.Direction.R, tank.getMoveDirection());
    }

    @Test
    void testUpMovement() {
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_UP));
        assertEquals(Tank.Direction.U, tank.getMoveDirection());
    }

    @Test
    void testDownMovement() {
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_DOWN));
        assertEquals(Tank.Direction.D, tank.getMoveDirection());
    }

    @Test
    void testDiagonalMovement() {
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_RIGHT));
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_UP));
        assertEquals(Tank.Direction.RU, tank.getMoveDirection());
    }

    @Test
    void testRightKeyRelease() {
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_RIGHT));
        assertEquals(Tank.Direction.R, tank.getMoveDirection());

        tank.handleKeyReleased(createKeyEvent(KeyEvent.VK_RIGHT));
        assertEquals(Tank.Direction.STOP, tank.getMoveDirection());
    }

    @Test
    void testUpKeyRelease() {
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_UP));
        assertEquals(Tank.Direction.U, tank.getMoveDirection());

        tank.handleKeyReleased(createKeyEvent(KeyEvent.VK_UP));
        assertEquals(Tank.Direction.STOP, tank.getMoveDirection());
    }

    @Test
    void testDownKeyRelease() {
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_DOWN));
        assertEquals(Tank.Direction.D, tank.getMoveDirection());

        tank.handleKeyReleased(createKeyEvent(KeyEvent.VK_DOWN));
        assertEquals(Tank.Direction.STOP, tank.getMoveDirection());
    }

    @Test
    void testSpaceToFire() {
        KeyEvent spaceEvent = createKeyEvent(KeyEvent.VK_SPACE);
        tank.handleKeyReleased(spaceEvent);
    }

    @Test
    void testMultipleKeysPress() {
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_LEFT));
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_UP));
        assertEquals(Tank.Direction.LU, tank.getMoveDirection());

        tank.handleKeyReleased(createKeyEvent(KeyEvent.VK_LEFT));
        assertEquals(Tank.Direction.U, tank.getMoveDirection());
    }

    @Test
    void testLDRDKeysPress() {
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_LEFT));
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_DOWN));
        assertEquals(Tank.Direction.LD, tank.getMoveDirection());

        tank.handleKeyReleased(createKeyEvent(KeyEvent.VK_LEFT));

        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_RIGHT));
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_DOWN));
        assertEquals(Tank.Direction.RD, tank.getMoveDirection());
    }

    @Test
    void testAllDirectionsFiring() {
        KeyEvent aKeyEvent = createKeyEvent(KeyEvent.VK_A);
        tank.handleKeyReleased(aKeyEvent);
    }

    @Test
    void testUpdateMoveDirectionAI() {
        StubTankAI stubAI = new StubTankAI(tank, gameClient);
        Tank tank = new Tank(100, 100, false, Tank.Direction.STOP, gameClient);
        tank.setAI(stubAI);
        tank.handleKeyPressed(createKeyEvent(KeyEvent.VK_LEFT));
        assertTrue(stubAI.wasUpdateCalled());
    }

    @Test
    void testConstrainToGameBounds() {
        tank.setCurrentX(-10);
        tank.setCurrentY(-10);
        tank.constrainToGameBounds();
        assertEquals(0, tank.getPositionX());
        assertEquals(30, tank.getPositionY());

        tank.setCurrentX(TankClient.GAME_WIDTH + 10);
        tank.setCurrentY(TankClient.GAME_HEIGHT + 10);
        tank.constrainToGameBounds();
        assertEquals(TankClient.GAME_WIDTH - Tank.TANK_WIDTH, tank.getPositionX());
        assertEquals(TankClient.GAME_HEIGHT - Tank.TANK_HEIGHT, tank.getPositionY());

        tank.setCurrentX(TankClient.GAME_WIDTH / 2);
        tank.setCurrentY(TankClient.GAME_HEIGHT / 2);
        tank.constrainToGameBounds();
        assertEquals(TankClient.GAME_WIDTH / 2, tank.getPositionX());
        assertEquals(TankClient.GAME_HEIGHT / 2, tank.getPositionY());
    }

    @Test
    void testInvalidSpawnPosition() {
        Wall wall = new Wall(90, 90, 40, 30, gameClient);
        when(gameClient.getWalls()).thenReturn(new Wall[]{wall});
        when(gameClient.getEnemyTanks()).thenReturn(new ArrayList<>());

        assertFalse(tankGenerator.isValidSpawnPosition(100, 100));
    }

    @Test
    void testSpawnEnemyTanksNoValidPosition() {
        when(gameClient.getWalls()).thenReturn(new Wall[]{new Wall(0, 0, 800, 600, gameClient)});
        when(gameClient.getEnemyTanks()).thenReturn(new ArrayList<>());

        int count = 5;
        int successfulSpawns = tankGenerator.spawnEnemyTanks(count);
        assertEquals(0, successfulSpawns);
    }
}