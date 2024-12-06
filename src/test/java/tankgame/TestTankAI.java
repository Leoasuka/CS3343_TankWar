package tankgame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Random;

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
    @Test
    void testHasPartialCover_WithPartialCover() {
        // Setup
        Point position = new Point(150, 150);
        Wall wall = mock(Wall.class);
        when(wall.getCollisionBounds()).thenReturn(new Rectangle(100, 100, 20, 20));
        when(wall.getPositionX()).thenReturn(100);
        when(wall.getPositionY()).thenReturn(100);

        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);

        TankAI tankAI = new TankAI(controlledTank, mockClient);

        // Test
        boolean result = tankAI.hasPartialCover(position, wall, playerTank);

        // Verify - position is close to wall but sight line isn't blocked
        assertTrue(result);
    }

    @Test
    void testHasPartialCover_TooFarFromWall() {
        // Setup
        Point position = new Point(300, 300);
        Wall wall = mock(Wall.class);
        when(wall.getCollisionBounds()).thenReturn(new Rectangle(100, 100, 20, 20));
        when(wall.getPositionX()).thenReturn(100);
        when(wall.getPositionY()).thenReturn(100);

        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);

        TankAI tankAI = new TankAI(controlledTank, mockClient);

        // Test
        boolean result = tankAI.hasPartialCover(position, wall, playerTank);

        // Verify - position is too far from wall
        assertFalse(result);
    }

    @Test
    void testHasPartialCover_BlockedByWall() {
        // Setup
        Point position = new Point(90, 90);
        Wall wall = mock(Wall.class);
        when(wall.getCollisionBounds()).thenReturn(new Rectangle(100, 100, 20, 20));
        when(wall.getPositionX()).thenReturn(100);
        when(wall.getPositionY()).thenReturn(100);

        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);

        TankAI tankAI = new TankAI(controlledTank, mockClient);

        // Test
        boolean result = tankAI.hasPartialCover(position, wall, playerTank);

        // Verify - sight line is blocked by wall
        assertFalse(result);
    }

    @Test
    void testEvaluateTacticalPosition_IdealPosition() {
        // Setup
        Point position = new Point(100, 100);
        Wall wall = mock(Wall.class);
        when(wall.getPositionX()).thenReturn(100);
        when(wall.getPositionY()).thenReturn(100);
        when(wall.getCollisionBounds()).thenReturn(new Rectangle(100, 100, 20, 20));

        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);

        TankAI tankAI = spy(new TankAI(controlledTank, mockClient));
        doReturn(true).when(tankAI).hasPartialCover(any(), any(), any());
        doReturn(true).when(tankAI).hasGoodFiringAngle(any(), any());

        // Test
        double score = tankAI.evaluateTacticalPosition(position, wall, playerTank);

        // Verify - position has partial cover, good firing angle, and good distance
        assertTrue(score > 80); // Should get high score for meeting all criteria
    }

    @Test
    void testEvaluateTacticalPosition_NoAdvantages() {
        // Setup
        Point position = new Point(400, 400);
        Wall wall = mock(Wall.class);
        when(wall.getPositionX()).thenReturn(100);
        when(wall.getPositionY()).thenReturn(100);
        when(wall.getCollisionBounds()).thenReturn(new Rectangle(100, 100, 20, 20));

        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);

        TankAI tankAI = spy(new TankAI(controlledTank, mockClient));
        doReturn(false).when(tankAI).hasPartialCover(any(), any(), any());
        doReturn(false).when(tankAI).hasGoodFiringAngle(any(), any());

        // Test
        double score = tankAI.evaluateTacticalPosition(position, wall, playerTank);

        // Verify - position has no tactical advantages
        assertEquals(0, score);
    }

    @Test
    void testEvaluateTacticalPosition_PartialAdvantages() {
        // Setup
        Point position = new Point(150, 150);
        Wall wall = mock(Wall.class);
        when(wall.getPositionX()).thenReturn(100);
        when(wall.getPositionY()).thenReturn(100);
        when(wall.getCollisionBounds()).thenReturn(new Rectangle(100, 100, 20, 20));

        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);

        TankAI tankAI = spy(new TankAI(controlledTank, mockClient));
        doReturn(true).when(tankAI).hasPartialCover(any(), any(), any());
        doReturn(false).when(tankAI).hasGoodFiringAngle(any(), any());

        // Test
        double score = tankAI.evaluateTacticalPosition(position, wall, playerTank);

        // Verify - position has only partial cover advantage
        assertTrue(score > 0 && score < 80); // Should get medium score
    }

    @Test
    void testEvaluateTacticalPosition_OutOfRange() {
        // Setup
        Point position = new Point(100, 100);
        Wall wall = mock(Wall.class);
        when(wall.getPositionX()).thenReturn(100);
        when(wall.getPositionY()).thenReturn(100);
        when(wall.getCollisionBounds()).thenReturn(new Rectangle(100, 100, 20, 20));

        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);

        TankAI tankAI = spy(new TankAI(controlledTank, mockClient));
        doReturn(true).when(tankAI).hasPartialCover(any(), any(), any());
        doReturn(false).when(tankAI).hasGoodFiringAngle(any(), any());

        // Test
        double score = tankAI.evaluateTacticalPosition(position, wall, playerTank);

        // Verify - position is too far from ideal range
        assertTrue(score < 80); // Should get lower score due to distance
    }
    
    @Test
    void testHasLineOfSight_ClearPath() {
        // Setup
        TankAI tankAI = new TankAI(controlledTank, mockClient);
        Tank targetTank = mock(Tank.class);

        when(controlledTank.getPositionX()).thenReturn(50);
        when(controlledTank.getPositionY()).thenReturn(50);
        when(targetTank.getPositionX()).thenReturn(100);
        when(targetTank.getPositionY()).thenReturn(100);
        
        // No walls in the way
        when(mockClient.getWalls()).thenReturn(new Wall[]{});

        // Test
        assertTrue(tankAI.hasLineOfSight(targetTank));
    }

    @Test
    void testHasLineOfSight_WithWallBlocking() {
        // Setup
        TankAI tankAI = new TankAI(controlledTank, mockClient);
        Tank targetTank = mock(Tank.class);
        Wall wall = mock(Wall.class);

        when(controlledTank.getPositionX()).thenReturn(50);
        when(controlledTank.getPositionY()).thenReturn(50);
        when(targetTank.getPositionX()).thenReturn(100);
        when(targetTank.getPositionY()).thenReturn(100);

        // Setup wall that blocks the sight line
        when(wall.getCollisionBounds()).thenReturn(new Rectangle(75, 75, 50, 50));
        when(mockClient.getWalls()).thenReturn(new Wall[]{wall});

        // Test
        assertFalse(tankAI.hasLineOfSight(targetTank));
    }

    @Test
    void testHasLineOfSight_TouchingWall() {
        // Setup
        TankAI tankAI = new TankAI(controlledTank, mockClient);
        Tank targetTank = mock(Tank.class);
        Wall wall = mock(Wall.class);

        when(controlledTank.getPositionX()).thenReturn(50);
        when(controlledTank.getPositionY()).thenReturn(50);
        when(targetTank.getPositionX()).thenReturn(75);
        when(targetTank.getPositionY()).thenReturn(75);

        // Setup wall that touches the sight line
        when(wall.getCollisionBounds()).thenReturn(new Rectangle(75, 75, 50, 50));
        when(mockClient.getWalls()).thenReturn(new Wall[]{wall});

        // Test
        assertFalse(tankAI.hasLineOfSight(targetTank));
    }

    @Test
    void testHasLineOfSight_SlightlyObscured() {
        // Setup
        TankAI tankAI = new TankAI(controlledTank, mockClient);
        Tank targetTank = mock(Tank.class);
        Wall wall = mock(Wall.class);

        when(controlledTank.getPositionX()).thenReturn(50);
        when(controlledTank.getPositionY()).thenReturn(50);
        when(targetTank.getPositionX()).thenReturn(100);
        when(targetTank.getPositionY()).thenReturn(90);

        // Setup wall that does not block the view but is close
        when(wall.getCollisionBounds()).thenReturn(new Rectangle(75, 75, 50, 50));
        when(mockClient.getWalls()).thenReturn(new Wall[]{wall});

        // Test
        assertFalse(tankAI.hasLineOfSight(targetTank));
    }

    @Test
    void testUpdateTargeting() {
        Tank playerTank = mock(Tank.class);
        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);

        Tank controlledTank = new Tank(100, 100, false, Tank.Direction.STOP, mockClient);
        TankAI tankAI = new TankAI(controlledTank, mockClient);

        tankAI.updateTargeting(playerTank);

        assertEquals(Tank.Direction.STOP, controlledTank.getTankMoveDirection());
    }

    @Test
    void testDecideMoveStrategy() {
        // 创建 mock 对象
        Tank controlledTank = mock(Tank.class);  // 被控制的坦克
        TankClient client = mock(TankClient.class);  // 游戏客户端
        Tank playerTank = mock(Tank.class);  // 玩家坦克

        // 模拟游戏环境
        when(client.getPlayerTank()).thenReturn(playerTank);
        when(client.getWalls()).thenReturn(new Wall[0]);  // 明确返回空数组
        when(controlledTank.getPositionX()).thenReturn(100);
        when(controlledTank.getPositionY()).thenReturn(100);

        TankAI tankAI = new TankAI(controlledTank, client);

        // 模拟玩家坦克在远距离
        when(playerTank.getPositionX()).thenReturn(500);
        when(playerTank.getPositionY()).thenReturn(500);

        // 测试远距离逻辑（应该向玩家移动）
        tankAI.decideMoveStrategy(500, playerTank);
        verify(controlledTank).setMovementDirection(Tank.Direction.RD);  // 方向应该为右下（RD）

        // 模拟玩家坦克在过近的距离
        when(playerTank.getPositionX()).thenReturn(105);
        when(playerTank.getPositionY()).thenReturn(105);

        // 测试过近的逻辑（应该远离玩家）
        tankAI.decideMoveStrategy(5, playerTank);
        verify(controlledTank).setMovementDirection(Tank.Direction.LU);  // 方向应该为左上（LU）

        // 模拟玩家坦克在中等距离
        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);

        // 测试中等距离逻辑（应该进行战术移动）
        tankAI.decideMoveStrategy(150, playerTank);
        // 验证是否调用了战术移动的方法
        TankAI spyTankAI = spy(tankAI);
        spyTankAI.decideMoveStrategy(150, playerTank);
        verify(spyTankAI).tacticalMovement();
    }

    @Test
    void testCalculateDistance() {
        // 创建 mock 对象
        Tank controlledTank = mock(Tank.class);
        Tank playerTank = mock(Tank.class);
        TankClient client = mock(TankClient.class);

        // 设置 controlledTank 的位置
        when(controlledTank.getPositionX()).thenReturn(100);
        when(controlledTank.getPositionY()).thenReturn(100);

        // 设置 playerTank 的位置
        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);

        // 创建 TankAI 实例
        TankAI tankAI = new TankAI(controlledTank, client);

        // 计算预期的距离
        double expectedDistance = Math.sqrt(Math.pow(200 - 100, 2) + Math.pow(200 - 100, 2));

        // 调用 calculateDistance 方法
        double actualDistance = tankAI.calculateDistance(playerTank);

        // 验证返回值是否正确
        assertEquals(expectedDistance, actualDistance, 0.001, "The distance calculation is incorrect.");
    }

    @Test
    void testMoveToNearestCover_WhenNearestWallExists() {
        // 创建 mock 对象
        Tank controlledTank = mock(Tank.class);
        TankClient client = mock(TankClient.class);
        Wall nearestWall = mock(Wall.class);

        // 创建 TankAI 的 spy 实例
        TankAI tankAI = spy(new TankAI(controlledTank, client));

        // 模拟 findNearestWall 方法返回最近的墙
        doReturn(nearestWall).when(tankAI).findNearestWall();

        // 模拟 findCoverPosition 方法返回一个掩体位置
        Point coverPosition = new Point(300, 400);
        doReturn(coverPosition).when(tankAI).findCoverPosition(nearestWall);

        // 调用 moveToNearestCover 方法
        tankAI.moveToNearestCover();

        // 验证 moveTowardsPosition 方法是否被正确调用
        verify(tankAI).moveTowardsPosition(coverPosition.x, coverPosition.y);
    }

    @Test
    void testMoveToNearestCover_WhenNoNearestWall() {
        // 创建 mock 对象
        Tank controlledTank = mock(Tank.class);
        TankClient client = mock(TankClient.class);

        // 创建 TankAI 的 spy 实例
        TankAI tankAI = spy(new TankAI(controlledTank, client));

        // 模拟 findNearestWall 方法返回 null
        doReturn(null).when(tankAI).findNearestWall();

        // 调用 moveToNearestCover 方法
        tankAI.moveToNearestCover();

        // 验证 moveTowardsPosition 方法没有被调用
        verify(tankAI, never()).moveTowardsPosition(anyInt(), anyInt());
    }

    @Test
    void testPerformStrafingMovement() {
        // 创建 mock 对象
        Tank controlledTank = mock(Tank.class);          // 模拟被控制的坦克
        Tank playerTank = mock(Tank.class);             // 模拟玩家坦克
        TankClient gameClient = mock(TankClient.class); // 模拟游戏客户端

        // 模拟 gameClient.getPlayerTank() 返回 playerTank
        when(gameClient.getPlayerTank()).thenReturn(playerTank);

        // 模拟坦克位置
        when(controlledTank.getPositionX()).thenReturn(100);
        when(controlledTank.getPositionY()).thenReturn(100);
        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);

        // 创建 TankAI 实例
        TankAI tankAI = new TankAI(controlledTank, gameClient);

        // 调用方法
        tankAI.performStrafingMovement();

        // 验证 setMovementDirection 被调用（这里不验证具体值，只验证调用是否发生）
        verify(controlledTank).setMovementDirection(any());
    }

    @Test
    void testEvaluateCoverPosition() {
        // 创建 mock 对象
        TankClient gameClient = mock(TankClient.class);  // 模拟游戏客户端
        Tank playerTank = mock(Tank.class);             // 模拟玩家坦克
        Wall wall = mock(Wall.class);                   // 模拟墙壁

        // 模拟 gameClient.getPlayerTank() 返回 playerTank
        when(gameClient.getPlayerTank()).thenReturn(playerTank);

        // 模拟玩家坦克的位置
        when(playerTank.getPositionX()).thenReturn(200);
        when(playerTank.getPositionY()).thenReturn(200);

        // 模拟墙壁的位置和大小
        when(wall.getPositionX()).thenReturn(100);
        when(wall.getPositionY()).thenReturn(100);
        when(wall.getWidth()).thenReturn(50);
        when(wall.getHeight()).thenReturn(50);

        // 模拟墙壁的遮挡行为
        when(wall.isPointBehindWall(150, 150, 200, 200)).thenReturn(true);

        // 测试点的位置
        Point position = new Point(150, 150);

        // 创建 TankAI 实例
        TankAI tankAI = new TankAI(null, gameClient);

        // 调用方法
        double score = tankAI.evaluateCoverPosition(position, wall);

        // 验证结果
        // 1. 距离玩家的得分
        TankAI.DistanceCalculator DistanceCalculator = new TankAI.DistanceCalculator();
        double expectedDistanceToPlayer = DistanceCalculator.calculate(150, 150, 200, 200);
        double expectedPlayerScore = -(Math.abs(expectedDistanceToPlayer - 50));

        // 2. 墙的中心点距离
        Point wallCenter = new Point(100 + 50 / 2, 100 + 50 / 2);
        double expectedDistanceToWall = DistanceCalculator.calculate(150, 150, wallCenter.x, wallCenter.y);

        // 3. 判断墙是否遮挡
        double expectedWallScore = wall.isPointBehindWall(150, 150, 200, 200) ? 100 : 0;

        // 4. 墙的距离得分
        double expectedCloseToWallScore = (expectedDistanceToWall < 50) ? 50 : 0;

        double expectedScore = 20;

        // 验证评分是否正确
        assertEquals(expectedScore, score, 1);
    }

    @Test
    void testSeekSafestCover() {
        // 创建 mock 对象
        TankClient gameClient = mock(TankClient.class);    // 模拟游戏客户端
        Wall wall1 = mock(Wall.class);                    // 模拟第一面墙
        Wall wall2 = mock(Wall.class);                    // 模拟第二面墙
        Point position1 = new Point(100, 100);            // 第一面墙的掩体位置
        Point position2 = new Point(200, 200);            // 第二面墙的掩体位置

        // 模拟墙壁数组
        when(gameClient.getWalls()).thenReturn(new Wall[]{wall1, wall2});

        // 模拟墙 1 的掩体位置
        when(wall1.getCoverPositions()).thenReturn(new Point[]{position1});
        // 模拟墙 2 的掩体位置
        when(wall2.getCoverPositions()).thenReturn(new Point[]{position2});

        // 模拟 evaluateCoverPosition 方法的行为
        TankAI tankAI = spy(new TankAI(null, gameClient));
        doReturn(50.0).when(tankAI).evaluateCoverPosition(position1, wall1); // 第一面墙得分
        doReturn(100.0).when(tankAI).evaluateCoverPosition(position2, wall2); // 第二面墙得分

        // 模拟 moveTowardsPosition 方法
        doNothing().when(tankAI).moveTowardsPosition(anyInt(), anyInt());

        // 调用方法
        tankAI.seekSafestCover();

        // 验证最佳掩体位置被选中（第二面墙的 position2）
        verify(tankAI).moveTowardsPosition(200, 200);
    }

    @Test
    void testSeekSafestCover_NoWalls() {
        // 创建 mock 对象
        TankClient gameClient = mock(TankClient.class);

        // 模拟没有墙的情况
        when(gameClient.getWalls()).thenReturn(null);

        // 创建 TankAI 实例
        TankAI tankAI = spy(new TankAI(null, gameClient));

        // 调用方法
        tankAI.seekSafestCover();

        // 验证 moveTowardsPosition 未被调用
        verify(tankAI, never()).moveTowardsPosition(anyInt(), anyInt());
    }

    @Test
    void testSeekSafestCover_EmptyWallsArray() {
        // 创建 mock 对象
        TankClient gameClient = mock(TankClient.class);

        // 模拟墙数组为空
        when(gameClient.getWalls()).thenReturn(new Wall[0]);

        // 创建 TankAI 实例
        TankAI tankAI = spy(new TankAI(null, gameClient));

        // 调用方法
        tankAI.seekSafestCover();

        // 验证 moveTowardsPosition 未被调用
        verify(tankAI, never()).moveTowardsPosition(anyInt(), anyInt());
    }
}