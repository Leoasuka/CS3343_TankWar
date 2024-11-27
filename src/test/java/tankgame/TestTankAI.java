package tankgame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestTankAI {

    private Tank controlledTank;
    private TankClient gameClient;
    private Tank playerTank;
    private TankAI tankAI;

    @BeforeEach
    void setUp() {
        // Mock所有依赖
        controlledTank = mock(Tank.class);
        gameClient = mock(TankClient.class);
        playerTank = mock(Tank.class);

        // 创建TankAI实例
        tankAI = new TankAI(controlledTank, gameClient);

        // 默认设置：玩家坦克存活，AI坦克存活
        when(playerTank.isAlive()).thenReturn(true);
        when(controlledTank.isAlive()).thenReturn(true);
        when(gameClient.getPlayerTank()).thenReturn(playerTank);
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
}