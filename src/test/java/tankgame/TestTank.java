package tankgame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
        when(gameClient.getEnemyTanks()).thenReturn(new ArrayList<>());
//        when(gameClient.getWalls()).thenReturn(new ArrayList<>());
        when(gameClient.getPlayerTank()).thenReturn(new Tank(200, 200, true));
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
    void testRenderHealthBar20() {
        Tank tank = new Tank(100, 100, true);
        tank.setHealthPoints(20);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        tank.render(graphics);
    }
    @Test
    void testSetHealthPoints() {

        Tank tank = new Tank(100, 100, true, Tank.Direction.STOP, gameClient);
        tank.setHealthPoints(0);
        assertEquals(0, tank.getHealthPoints());
        assertEquals(0, gameClient.getScore());
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
    void testRevertToPreviousPosition(){
        Tank tank = new Tank(100, 100, true);
        tank.revertToPreviousPosition();
        assertEquals(100, tank.getPositionX());
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
//        int originalX = tank.getPositionX();

        assertTrue(tank.willCollideWithWalls(150, 100));
//        tank.updatePosition();
//        assertEquals(originalX, tank.getPositionX());
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
    public void testUpdatePosition_Left() {
        Tank tank = mock(Tank.class, Mockito.CALLS_REAL_METHODS);
        doReturn(100).when(tank).getCurrentX();
        doReturn(100).when(tank).getCurrentY();
        doReturn(false).when(tank).willCollideWithOtherTanks(anyInt(), anyInt());
        doReturn(false).when(tank).willCollideWithWalls(anyInt(), anyInt());
        tank.setTankMoveDirection(Tank.Direction.L);
        tank.updatePosition();
//        assertEquals(95, tank.getCurrentX());
        assertEquals(100, tank.getCurrentY());
    }

    @Test
    public void testUpdatePosition_LeftUp() {
        Tank tank = mock(Tank.class, Mockito.CALLS_REAL_METHODS);
        doReturn(100).when(tank).getCurrentX();
        doReturn(100).when(tank).getCurrentY();
        doReturn(false).when(tank).willCollideWithOtherTanks(anyInt(), anyInt());
        doReturn(false).when(tank).willCollideWithWalls(anyInt(), anyInt());
        tank.setTankMoveDirection(Tank.Direction.LU);
        tank.updatePosition();
        assertEquals(100, tank.getCurrentX());
//
    }

    @Test
    public void testUpdatePosition_Up() {
        Tank tank = mock(Tank.class, Mockito.CALLS_REAL_METHODS);
        doReturn(100).when(tank).getCurrentX();
        doReturn(100).when(tank).getCurrentY();
        doReturn(false).when(tank).willCollideWithOtherTanks(anyInt(), anyInt());
        doReturn(false).when(tank).willCollideWithWalls(anyInt(), anyInt());
        tank.setTankMoveDirection(Tank.Direction.U);
        tank.updatePosition();
        assertEquals(100, tank.getCurrentX());
//
    }

    @Test
    public void testUpdatePosition_RightUp() {
        Tank tank = mock(Tank.class, Mockito.CALLS_REAL_METHODS);
        doReturn(100).when(tank).getCurrentX();
        doReturn(100).when(tank).getCurrentY();
        doReturn(false).when(tank).willCollideWithOtherTanks(anyInt(), anyInt());
        doReturn(false).when(tank).willCollideWithWalls(anyInt(), anyInt());
        tank.setTankMoveDirection(Tank.Direction.RU);
        tank.updatePosition();

        assertEquals(100, tank.getCurrentY());
    }

    @Test
    public void testUpdatePosition_Right() {
        Tank tank = mock(Tank.class, Mockito.CALLS_REAL_METHODS);
        doReturn(100).when(tank).getCurrentX();
        doReturn(100).when(tank).getCurrentY();
        doReturn(false).when(tank).willCollideWithOtherTanks(anyInt(), anyInt());
        doReturn(false).when(tank).willCollideWithWalls(anyInt(), anyInt());
        tank.setTankMoveDirection(Tank.Direction.R);
        tank.updatePosition();

        assertEquals(100, tank.getCurrentY());
    }

    @Test
    public void testUpdatePosition_RightDown() {
        Tank tank = mock(Tank.class, Mockito.CALLS_REAL_METHODS);
        doReturn(100).when(tank).getCurrentX();
        doReturn(100).when(tank).getCurrentY();
        doReturn(false).when(tank).willCollideWithOtherTanks(anyInt(), anyInt());
        doReturn(false).when(tank).willCollideWithWalls(anyInt(), anyInt());
        tank.setTankMoveDirection(Tank.Direction.RD);
        tank.updatePosition();

        assertEquals(100, tank.getCurrentY());
    }

    @Test
    public void testUpdatePosition_Down() {
        Tank tank = mock(Tank.class, Mockito.CALLS_REAL_METHODS);
        doReturn(100).when(tank).getCurrentX();
        doReturn(100).when(tank).getCurrentY();
        doReturn(false).when(tank).willCollideWithOtherTanks(anyInt(), anyInt());
        doReturn(false).when(tank).willCollideWithWalls(anyInt(), anyInt());
        tank.setTankMoveDirection(Tank.Direction.D);
        tank.updatePosition();
        assertEquals(100, tank.getCurrentX());

    }

    @Test
    public void testUpdatePosition_LeftDown() {
        Tank tank = mock(Tank.class, Mockito.CALLS_REAL_METHODS);
        doReturn(100).when(tank).getCurrentX();
        doReturn(100).when(tank).getCurrentY();
        doReturn(false).when(tank).willCollideWithOtherTanks(anyInt(), anyInt());
        doReturn(false).when(tank).willCollideWithWalls(anyInt(), anyInt());
        tank.setTankMoveDirection(Tank.Direction.LD);
        tank.updatePosition();
        assertEquals(100, tank.getCurrentX());

    }

    @Test
    public void testUpdatePosition_Stop() {
        Tank tank = mock(Tank.class, Mockito.CALLS_REAL_METHODS);
        doReturn(100).when(tank).getCurrentX();
        doReturn(100).when(tank).getCurrentY();
        doReturn(false).when(tank).willCollideWithOtherTanks(anyInt(), anyInt());
        doReturn(false).when(tank).willCollideWithWalls(anyInt(), anyInt());
        tank.setTankMoveDirection(Tank.Direction.STOP);
        tank.updatePosition();
        assertEquals(100, tank.getCurrentX());
        assertEquals(100, tank.getCurrentY());
    }

    @Test
    public void testWillCollideWithOtherTanks1() {
        List<Tank> enemyTanks = new ArrayList<>();
        Tank enemyTank = new Tank(120, 120, false);
        enemyTanks.add(enemyTank);

        when(gameClient.getEnemyTanks()).thenReturn(enemyTanks);
        when(gameClient.getPlayerTank()).thenReturn(new Tank(200, 200, true));

        // Test collision
        assertTrue(tank.willCollideWithOtherTanks(100, 100));

        // Test no collision
        assertFalse(tank.willCollideWithOtherTanks(300, 300));
    }

//    @Test
//    public void testWillCollideWithWalls1() {
//        List<Wall> walls = new ArrayList<>();
//
//        walls.add(wall);
//
//        when(gameClient.getWalls()).thenReturn(walls);
//
//        // Test collision
//        assertTrue(tank.willCollideWithWalls(100, 100));
//
//        // Test no collision
//        assertFalse(tank.willCollideWithWalls(300, 300));
//    }

    @Test
    public void testConstrainToGameBounds1() {
        // Test left boundary
        tank.setTankCurrentX(-10);
        tank.constrainToGameBounds();
        assertEquals(0, tank.getCurrentX());

        // Test top boundary
        tank.setTankCurrentY(20);
        tank.constrainToGameBounds();
        assertEquals(30, tank.getCurrentY());

        // Test right boundary
        tank.setTankCurrentX(TankClient.GAME_WIDTH + 100);
        tank.constrainToGameBounds();
        assertEquals(TankClient.GAME_WIDTH - Tank.TANK_WIDTH, tank.getCurrentX());

        // Test bottom boundary
        tank.setTankCurrentY(TankClient.GAME_HEIGHT + 100);
        tank.constrainToGameBounds();
        assertEquals(TankClient.GAME_HEIGHT - Tank.TANK_HEIGHT, tank.getCurrentY());
    }

    @Test
    public void testHandleAIMovement() {
        tank = new Tank(100, 100, false, Tank.Direction.STOP, gameClient);
        tank.setMovementStep(0);
        tank.handleAIMovement();

        // Verify movement step was updated
        assertTrue(tank.getMovementStep() >= 3 && tank.getMovementStep() <= 14);

        // Verify direction was changed
        assertNotNull(tank.getTankMoveDirection());
    }

    @Test
    void testUpdatePositionAIMovement() {
        Tank tank = new Tank(100, 100, false);
        tank.setMovementDirection(Tank.Direction.STOP);
        tank.updatePosition();
        assertEquals(Tank.Direction.STOP, tank.getMoveDirection());
    }

//    @Test
//    void testUpdatePosition() {
//        tank.setMovementDirection(Tank.Direction.R);
//        tank.updatePosition();
//        assertEquals(105, tank.getPositionX());
//        assertEquals(100, tank.getPositionY());
//    }

    @Test
    void testHandleWallCollision_AliveAndColliding() {
        tank.setAlive(true);
        tank.setCurrentX(130); // Set tank's X position to be within the wall's bounds
        tank.setCurrentY(100); // Set tank's Y position to be the same as wall's Y position


        Wall wall = new Wall(150, 100, 20, 40, gameClient);
        Rectangle tankBounds = tank.getCollisionBounds();
        Rectangle wallBounds = wall.getCollisionBounds();

        // Ensure the bounds intersect
        assertTrue(tankBounds.intersects(wallBounds));

        boolean result = tank.handleWallCollision(wall);

        assertTrue(result);
    }

    @Test
    void testHandleWallCollision_NotAlive() {
        tank.setAlive(false);
        Wall wall = new Wall(150, 100, 20, 40, gameClient);
        Rectangle tankBounds = tank.getCollisionBounds();
        Rectangle wallBounds = wall.getCollisionBounds();

        // Ensure the bounds intersect
//        assertTrue(tankBounds.intersects(wallBounds));

        boolean result = tank.handleWallCollision(wall);

        assertFalse(result);
        assertEquals(100, tank.getPositionX());
        assertEquals(100, tank.getPositionY());
    }

    @Test
    void testHandleWallCollision_NotColliding() {
        tank.setAlive(true);
        Wall wall = new Wall(200, 100, 20, 40, gameClient);
        Rectangle tankBounds = tank.getCollisionBounds();
        Rectangle wallBounds = wall.getCollisionBounds();

        // Ensure the bounds do not intersect
        assertFalse(tankBounds.intersects(wallBounds));

        boolean result = tank.handleWallCollision(wall);

        assertFalse(result);
        assertEquals(100, tank.getPositionX());
        assertEquals(100, tank.getPositionY());
    }

    @Test
    void testHandleTankCollisions_AliveAndColliding() {
        tank.setAlive(true);
        Tank otherTank = new Tank(150, 100, false, Tank.Direction.STOP, gameClient);
        otherTank.setAlive(true);
        List<Tank> tanks = new ArrayList<>();
        tanks.add(otherTank);

        // Move otherTank to a position where it will collide with tank
        otherTank.setCurrentX(100); // Set otherTank's X position to be the same as tank's X position
        otherTank.setCurrentY(100); // Set otherTank's Y position to be the same as tank's Y position

        boolean result = tank.handleTankCollisions(tanks);

        assertTrue(result);
        assertEquals(100, tank.getPositionX());
        assertEquals(100, tank.getPositionY());
    }

    @Test
    void testHandleTankCollisions_NotAlive() {
        tank.setAlive(false);
        Tank otherTank = new Tank(150, 100, false, Tank.Direction.STOP, gameClient);
        otherTank.setAlive(true);
        List<Tank> tanks = new ArrayList<>();
        tanks.add(otherTank);
        Rectangle tankBounds = tank.getCollisionBounds();
        Rectangle otherTankBounds = otherTank.getCollisionBounds();

        // Ensure the bounds intersect
//        assertTrue(tankBounds.intersects(otherTankBounds));

        boolean result = tank.handleTankCollisions(tanks);

        assertFalse(result);
        assertEquals(100, tank.getPositionX());
        assertEquals(100, tank.getPositionY());
        assertEquals(150, otherTank.getPositionX());
        assertEquals(100, otherTank.getPositionY());
    }

    @Test
    void testHandleTankCollisions_NotColliding() {
        tank.setAlive(true);
        Tank otherTank = new Tank(200, 100, false, Tank.Direction.STOP, gameClient);
        otherTank.setAlive(true);
        List<Tank> tanks = new ArrayList<>();
        tanks.add(otherTank);
        Rectangle tankBounds = tank.getCollisionBounds();
        Rectangle otherTankBounds = otherTank.getCollisionBounds();

        // Ensure the bounds do not intersect
        assertFalse(tankBounds.intersects(otherTankBounds));

        boolean result = tank.handleTankCollisions(tanks);

        assertFalse(result);
        assertEquals(100, tank.getPositionX());
        assertEquals(100, tank.getPositionY());
        assertEquals(200, otherTank.getPositionX());
        assertEquals(100, otherTank.getPositionY());
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
    @Test
    void testIsValidSpawnPosition() {
        when(gameClient.getWalls()).thenReturn(new Wall[]{});
        when(gameClient.getEnemyTanks()).thenReturn(new ArrayList<>());
        when(gameClient.getPlayerTank()).thenReturn(new Tank(0, 0, true, Tank.Direction.STOP, gameClient));

        assertTrue(tankGenerator.isValidSpawnPosition(100, 100));
        assertFalse(tankGenerator.isValidSpawnPosition(0, 0)); // Too close to screen edge
        assertFalse(tankGenerator.isValidSpawnPosition(TankClient.GAME_WIDTH - Tank.TANK_WIDTH, TankClient.GAME_HEIGHT - Tank.TANK_HEIGHT)); // Too close to screen edge
    }

    @Test
    void testFindValidSpawnPosition() {
        when(gameClient.getWalls()).thenReturn(new Wall[]{});
        when(gameClient.getEnemyTanks()).thenReturn(new ArrayList<>());
        when(gameClient.getPlayerTank()).thenReturn(new Tank(0, 0, true, Tank.Direction.STOP, gameClient));

        Point spawnPoint = tankGenerator.findValidSpawnPosition();
        assertNotNull(spawnPoint);
        assertTrue(tankGenerator.isValidSpawnPosition(spawnPoint.x, spawnPoint.y));
    }

    @Test
    void testSpawnEnemyTanks() {
        when(gameClient.getWalls()).thenReturn(new Wall[]{});
        when(gameClient.getEnemyTanks()).thenReturn(new ArrayList<>());
        when(gameClient.getPlayerTank()).thenReturn(new Tank(0, 0, true, Tank.Direction.STOP, gameClient));

        int count = 5;
        int successfulSpawns = tankGenerator.spawnEnemyTanks(count);
        assertEquals(count, successfulSpawns);
        assertEquals(count, gameClient.getEnemyTanks().size());
    }

    @Test
    void testSpawnEnemyTanksNoValidPosition2() {
        when(gameClient.getWalls()).thenReturn(new Wall[]{new Wall(0, 0, TankClient.GAME_WIDTH, TankClient.GAME_HEIGHT, gameClient)});
        when(gameClient.getEnemyTanks()).thenReturn(new ArrayList<>());
        when(gameClient.getPlayerTank()).thenReturn(new Tank(0, 0, true, Tank.Direction.STOP, gameClient));

        int count = 5;
        int successfulSpawns = tankGenerator.spawnEnemyTanks(count);
        assertEquals(0, successfulSpawns);
        assertTrue(gameClient.getEnemyTanks().isEmpty());
    }

}