package tankgame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import main.java.tankgame.Tank;
import main.java.tankgame.TankAI;
import main.java.tankgame.TankClient;
import main.java.tankgame.Wall;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TestTankAI {

    private TankClient mockClient;
    private Tank controlledTank;
    private Tank playerTank;

    @BeforeEach
    void setUp() {
        mockClient = mock(TankClient.class);
        controlledTank = mock(Tank.class);
        playerTank = mock(Tank.class);

        when(mockClient.getPlayerTank()).thenReturn(playerTank);
        when(controlledTank.isAlive()).thenReturn(true);
        when(playerTank.isAlive()).thenReturn(true);
    }

    @Test
    void testConstructor() {
        TankAI tankAI = new TankAI(controlledTank, mockClient);
        assertNotNull(tankAI);
    }

    @Test
    void testUpdate_DeadPlayer() throws InterruptedException {
        TankAI tankAI = new TankAI(controlledTank, mockClient);

        // 设置空walls数组避免NPE
        Wall[] emptyWalls = new Wall[]{};
        when(mockClient.getWalls()).thenReturn(emptyWalls);

        Thread.sleep(1100);
        when(playerTank.isAlive()).thenReturn(false);

        tankAI.update();

        verify(controlledTank, never()).setBarrelDirection(any(Tank.Direction.class));
    }

    @Test
    void testUpdate_DeadControlledTank() throws InterruptedException {
        TankAI tankAI = new TankAI(controlledTank, mockClient);

        // 设置空walls数组避免NPE
        Wall[] emptyWalls = new Wall[]{};
        when(mockClient.getWalls()).thenReturn(emptyWalls);

        Thread.sleep(1100);
        when(controlledTank.isAlive()).thenReturn(false);

        tankAI.update();

        verify(controlledTank, never()).setBarrelDirection(any(Tank.Direction.class));
    }

    @Test
    void testFindNearestWall() {
        // Create a mock TankClient
        TankClient mockTankClient = mock(TankClient.class);

        // Create a controlled tank
        Tank controlledTank = new Tank(100, 100, true, Tank.Direction.STOP, mockTankClient);

        // Create walls at different distances
        Wall wall1 = new Wall(50, 50, 10, 10, mockTankClient);
        Wall wall2 = new Wall(200, 200, 10, 10, mockTankClient);
        Wall wall3 = new Wall(150, 150, 10, 10, mockTankClient);

        // Mock the getWalls method to return the walls
        when(mockTankClient.getWalls()).thenReturn(new Wall[]{wall1, wall2, wall3});

        // Create the TankAI instance
        TankAI tankAI = new TankAI(controlledTank, mockTankClient);

        // Call the findNearestWall method
        Wall nearestWall = tankAI.findNearestWall();

        // Verify that the nearest wall is wall1
        assertEquals(wall1, nearestWall);
    }

    @Test
    void testMoveTowardsPosition() {
        // Create a mock TankClient
        TankClient mockTankClient = mock(TankClient.class);

        // Create a controlled tank
        Tank controlledTank = new Tank(100, 100, true, Tank.Direction.STOP, mockTankClient);

        // Create the TankAI instance
        TankAI tankAI = new TankAI(controlledTank, mockTankClient);

        // Call the moveTowardsPosition method
        tankAI.moveTowardsPosition(200, 200);

        // Verify that the tank's movement direction is set correctly
        assertEquals(Tank.Direction.RD, controlledTank.getMoveDirection());
    }

    @Test
    void testFindCoverPosition() {
        // Create a mock TankClient
        TankClient mockTankClient = mock(TankClient.class);

        // Create a controlled tank
        Tank controlledTank = new Tank(100, 100, true, Tank.Direction.STOP, mockTankClient);

        // Create a player tank
        Tank playerTank = new Tank(50, 50, true, Tank.Direction.STOP, mockTankClient);
        when(mockTankClient.getPlayerTank()).thenReturn(playerTank);

        // Create a wall
        Wall wall = new Wall(200, 200, 10, 10, mockTankClient);

        // Create the TankAI instance
        TankAI tankAI = new TankAI(controlledTank, mockTankClient);

        // Call the findCoverPosition method
        Point coverPosition = tankAI.findCoverPosition(wall);

        // Verify that the cover position is calculated correctly
        double distance = Math.sqrt(150 * 150 + 150 * 150);
        int expectedX = 200 + (int)(150 / distance * 100);
        int expectedY = 200 + (int)(150 / distance * 100);
        assertEquals(new Point(expectedX, expectedY), coverPosition);
    }

    @Test
    void testNoFireMissile_WhenOutOfRange() {
        TankAI tankAI = new TankAI(controlledTank, mockClient);
        // 距离在射击范围外
        when(playerTank.getPositionX()).thenReturn(500);
        when(playerTank.getPositionY()).thenReturn(500);
        when(controlledTank.getPositionX()).thenReturn(0);
        when(controlledTank.getPositionY()).thenReturn(0);

        // 模拟有清晰视线
        TankAI spyTankAI = spy(tankAI);
        doReturn(true).when(spyTankAI).hasLineOfSight(playerTank);

        // 调用方法
        spyTankAI.decideToShoot(300);

        // 验证fireMissile未被调用
        verify(controlledTank, never()).fireMissile();
    }

    @Test
    void testNoFireMissile_WhenNoLineOfSight() {
        TankAI tankAI = new TankAI(controlledTank, mockClient);
        // 距离在射击范围内
        when(playerTank.getPositionX()).thenReturn(100);
        when(playerTank.getPositionY()).thenReturn(100);
        when(controlledTank.getPositionX()).thenReturn(0);
        when(controlledTank.getPositionY()).thenReturn(0);

        // 模拟没有清晰视线
        TankAI spyTankAI = spy(tankAI);
        doReturn(false).when(spyTankAI).hasLineOfSight(playerTank);

        // 调用方法
        spyTankAI.decideToShoot(150);

        // 验证fireMissile未被调用
        verify(controlledTank, never()).fireMissile();
    }
    @Test
    void testSeekTacticalAdvantage_NoWalls() {
        // Setup
    	TankAI tankAI = new TankAI(controlledTank, mockClient);
        when(mockClient.getWalls()).thenReturn(new Wall[]{});
        when(controlledTank.getPositionX()).thenReturn(100);
        when(controlledTank.getPositionY()).thenReturn(100);
        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);

        // Test
        tankAI.seekTacticalAdvantage();

        // Since there are no walls, the tank shouldn't move
        verify(controlledTank, never()).setMovementDirection(any(Tank.Direction.class));
    }

    @Test
    void testSeekTacticalAdvantage_WithWalls() {
        // Create walls with cover positions
        Wall wall1 = mock(Wall.class);
        when(wall1.getCoverPositions()).thenReturn(new Point[]{
            new Point(150, 150)
        });
        when(wall1.getPositionX()).thenReturn(140);
        when(wall1.getPositionY()).thenReturn(140);
        when(wall1.getCollisionBounds()).thenReturn(new Rectangle(140, 140, 20, 20));

        // Setup game state
        when(mockClient.getWalls()).thenReturn(new Wall[]{wall1});
        when(controlledTank.getPositionX()).thenReturn(100);
        when(controlledTank.getPositionY()).thenReturn(100);
        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);

        TankAI tankAI = new TankAI(controlledTank, mockClient);
		// Test
        tankAI.seekTacticalAdvantage();

        // Verify that the tank moves towards the tactical position
        verify(controlledTank).setMovementDirection(any(Tank.Direction.class));
    }

    @Test
    void testHasGoodFiringAngle_NoObstacles() {
        // Setup
        Point position = new Point(100, 100);
        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);
        when(mockClient.getWalls()).thenReturn(new Wall[]{});
        TankAI tankAI = new TankAI(controlledTank, mockClient);
        // Test
        boolean result = tankAI.hasGoodFiringAngle(position, playerTank);

        // Verify
        assertTrue(result);
    }

    @Test
    void testHasGoodFiringAngle_WithObstacle() {
        // Setup
        Point position = new Point(100, 100);
        Wall wall = mock(Wall.class);
        when(wall.getCollisionBounds()).thenReturn(new Rectangle(150, 150, 20, 20));
        
        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);
        when(mockClient.getWalls()).thenReturn(new Wall[]{wall});
        TankAI tankAI = new TankAI(controlledTank, mockClient);
        // Test
        boolean result = tankAI.hasGoodFiringAngle(position, playerTank);

        // Verify
        assertFalse(result);
    }

    @Test
    void testHasGoodFiringAngle_MultipleWalls() {
        // Setup
    	TankAI tankAI = new TankAI(controlledTank, mockClient);
        Point position = new Point(100, 100);
        Wall wall1 = mock(Wall.class);
        Wall wall2 = mock(Wall.class);
        when(wall1.getCollisionBounds()).thenReturn(new Rectangle(80, 80, 20, 20));
        when(wall2.getCollisionBounds()).thenReturn(new Rectangle(150, 150, 20, 20));
        
        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);
        when(mockClient.getWalls()).thenReturn(new Wall[]{wall1, wall2});

        // Test
        boolean result = tankAI.hasGoodFiringAngle(position, playerTank);

        // Verify
        assertFalse(result);
    }

    @Test
    void testSeekTacticalAdvantage_BestPositionSelection() {
        // Create multiple walls with different cover positions
        Wall wall1 = mock(Wall.class);
        Wall wall2 = mock(Wall.class);
        
        when(wall1.getCoverPositions()).thenReturn(new Point[]{
            new Point(150, 150)
        });
        when(wall2.getCoverPositions()).thenReturn(new Point[]{
            new Point(180, 180)
        });
        
        when(wall1.getPositionX()).thenReturn(140);
        when(wall1.getPositionY()).thenReturn(140);
        when(wall2.getPositionX()).thenReturn(170);
        when(wall2.getPositionY()).thenReturn(170);
        
        when(wall1.getCollisionBounds()).thenReturn(new Rectangle(140, 140, 20, 20));
        when(wall2.getCollisionBounds()).thenReturn(new Rectangle(170, 170, 20, 20));

        // Setup game state
        when(mockClient.getWalls()).thenReturn(new Wall[]{wall1, wall2});
        when(controlledTank.getPositionX()).thenReturn(100);
        when(controlledTank.getPositionY()).thenReturn(100);
        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);
        TankAI tankAI = new TankAI(controlledTank, mockClient);
        // Test
        tankAI.seekTacticalAdvantage();

        // Verify that the tank moves towards a position
        verify(controlledTank).setMovementDirection(any(Tank.Direction.class));
    }
}