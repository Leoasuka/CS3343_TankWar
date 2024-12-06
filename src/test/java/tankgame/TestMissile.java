package tankgame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestMissile {
    private TankClient mockClient;
    private Tank controlledTank;
    private Tank playerTank;
    private Wall wall;

    @BeforeEach
    void setUp() {
        mockClient = mock(TankClient.class);
        controlledTank = mock(Tank.class);
        playerTank = mock(Tank.class);
        wall = mock(Wall.class);

        when(mockClient.getPlayerTank()).thenReturn(playerTank);
        when(controlledTank.isAlive()).thenReturn(true);
        when(playerTank.isAlive()).thenReturn(true);
    }

    @Test
    public void testBasicConstructor() {
        int x = 100;
        int y = 200;
        Tank.Direction direction = Tank.Direction.U;

        Missile missile = new Missile(x, y, direction);

        assertEquals(x, missile.getPositionX());
        assertEquals(y, missile.getPositionY());
        assertEquals(direction, missile.getDirection());
        assertTrue(missile.isActive());
    }

    @Test
    public void testExtendedConstructor() {
        int x = 100;
        int y = 200;
        boolean isPlayerMissile = true;
        Tank.Direction direction = Tank.Direction.U;
        TankClient client = new TankClient();

        Missile missile = new Missile(x, y, isPlayerMissile, direction, client);

        assertEquals(x, missile.getPositionX());
        assertEquals(y, missile.getPositionY());
        assertEquals(direction, missile.getDirection());
        assertTrue(missile.isActive());
        assertEquals(isPlayerMissile, missile.isFromPlayerTank());
        assertEquals(client, missile.getGameClient());
    }

    @Test
    public void testHandleTankCollision() {
        int x = 100;
        int y = 200;
        Tank.Direction direction = Tank.Direction.U;
        Tank tank = new Tank(x, y, true);
        Missile missile = new Missile(x, y, direction);

        assertTrue(missile.handleTankCollision(tank));
        assertFalse(missile.isActive());
    }

    @Test
    public void testHandleWallCollision() {
        int x = 100;
        int y = 200;
        Tank.Direction direction = Tank.Direction.U;
        Missile missile = new Missile(x, y, direction);

        // Set up the wall mock to return a collision boundary that intersects with the missile's position
        when(wall.getCollisionBounds()).thenReturn(new Rectangle(x, y, 10, 10));

        assertTrue(missile.handleWallCollision(wall));
        assertFalse(missile.isActive());
    }

    @Test
    public void testUpdatePosition() {
        int x = 100;
        int y = 200;
        Tank.Direction direction = Tank.Direction.U;
        Missile missile = new Missile(x, y, direction);

        missile.updatePosition();

        switch (direction) {
            case U:
                assertEquals(185, missile.getPositionY());
                break;
            case D:
                assertEquals(y + missile.getSpeed(), missile.getPositionY());
                break;
            case L:
                assertEquals(x - missile.getSpeed(), missile.getPositionX());
                break;
            case R:
                assertEquals(x + missile.getSpeed(), missile.getPositionX());
                break;
        }
    }

    @Test
    public void testRender() {
        int x = 100;
        int y = 200;
        Tank.Direction direction = Tank.Direction.U;
        Missile missile = new Missile(x, y, direction);

        Graphics mockGraphics = mock(Graphics.class);

        missile.render(mockGraphics);

        verify(mockGraphics, times(1)).fillOval(x, y, Missile.MISSILE_WIDTH, Missile.MISSILE_HEIGHT);
        verify(mockGraphics, times(1)).fillOval(x-1, y-1, Missile.MISSILE_WIDTH+2, Missile.MISSILE_HEIGHT+2);
    }

    @Test
    public void testRenderInactive() {
        int x = 100;
        int y = 200;
        Tank.Direction direction = Tank.Direction.U;
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(x, y, true, direction, mockClient);
        missile.setActive(false);

        Graphics mockGraphics = mock(Graphics.class);
        List<Missile> mockMissiles = mock(List.class);
        when(mockClient.getMissiles()).thenReturn(mockMissiles);

        missile.render(mockGraphics);

        verify(mockGraphics, never()).fillOval(x, y, Missile.MISSILE_WIDTH, Missile.MISSILE_HEIGHT);
        verify(mockGraphics, never()).fillOval(x-1, y-1, Missile.MISSILE_WIDTH+2, Missile.MISSILE_HEIGHT+2);
        verify(mockMissiles, times(1)).remove(missile);
    }

    @Test
    public void testMissileCollision() {
        int x = 100;
        int y = 200;
        Tank.Direction direction = Tank.Direction.U;
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(x, y, true, direction, mockClient);

        Tank mockTank = mock(Tank.class);

        // 模拟坦克的碰撞边界
        when(mockTank.getCollisionBounds()).thenReturn(new Rectangle(x, y, 30, 30));
        when(mockTank.isAlive()).thenReturn(true); // 模拟坦克是活着的
        when(mockTank.isPlayerControlled()).thenReturn(false); // 假设坦克是敌方坦克

        boolean collision = missile.handleTankCollision(mockTank);

        assertTrue(collision); // 碰撞应该发生
        assertFalse(missile.isActive()); // 碰撞后，导弹应该变为非活动状态
    }

    @Test
    public void testMissileWallCollision() {
        int x = 100;
        int y = 200;
        Tank.Direction direction = Tank.Direction.U;
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(x, y, true, direction, mockClient);

        Wall mockWall = mock(Wall.class);
        when(mockWall.getCollisionBounds()).thenReturn(new Rectangle(x, y, 10, 10));

        boolean collision = missile.handleWallCollision(mockWall);

        assertTrue(collision);
    }

    @Test
    public void testMissileNoCollision() {
        int x = 100;
        int y = 200;
        Tank.Direction direction = Tank.Direction.U;
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(x, y, true, direction, mockClient);

        Tank mockTank = mock(Tank.class);

        // 模拟坦克的碰撞边界，设置为一个与导弹不相交的 Rectangle
        when(mockTank.getCollisionBounds()).thenReturn(new Rectangle(x + 50, y + 50, 30, 30));
        when(mockTank.isAlive()).thenReturn(true); // 模拟坦克是活着的
        when(mockTank.isPlayerControlled()).thenReturn(false); // 假设坦克是敌方坦克

        boolean collision = missile.handleTankCollision(mockTank);

        // 断言没有发生碰撞
        assertFalse(collision);
    }

    @Test
    public void testMissileNoWallCollision() {
        int x = 100;
        int y = 200;
        Tank.Direction direction = Tank.Direction.U;
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(x, y, true, direction, mockClient);

        Wall mockWall = mock(Wall.class);
        when(mockWall.getCollisionBounds()).thenReturn(new Rectangle(x + 10, y + 10, 10, 10));

        boolean collision = missile.handleWallCollision(mockWall);

        assertFalse(collision);
    }

    @Test
    public void testHandleTankCollisions() {
        int x = 100;
        int y = 200;
        Tank.Direction direction = Tank.Direction.U;
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(x, y, true, direction, mockClient);

        Tank mockTank = mock(Tank.class);

        // 模拟坦克的碰撞边界
        when(mockTank.getCollisionBounds()).thenReturn(new Rectangle(x, y, 30, 30));
        when(mockTank.isAlive()).thenReturn(true); // 确保坦克是活的

        List<Tank> tanks = List.of(mockTank);

        // 调用方法并断言
        boolean collision = missile.handleTankCollisions(tanks);

        assertTrue(collision);
        verify(mockTank, times(1)).setAlive(false); // 验证坦克的状态被设置为死亡
        assertFalse(missile.isActive()); // 验证导弹已被标记为非活跃状态
    }

    @Test
    public void testHandleTankCollisionsNoCollision() {
        int x = 100;
        int y = 200;
        Tank.Direction direction = Tank.Direction.U;
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(x, y, true, direction, mockClient);

        Tank mockTank = mock(Tank.class);

        // 模拟坦克的碰撞边界，定义一个和导弹无交集的 Rectangle
        when(mockTank.getCollisionBounds()).thenReturn(new Rectangle(x + 50, y + 50, 30, 30));
        when(mockTank.isAlive()).thenReturn(true); // 保证坦克是活着的
        when(mockTank.isPlayerControlled()).thenReturn(false); // 假设坦克是敌方坦克

        List<Tank> tanks = List.of(mockTank);

        // 调用 handleTankCollisions 方法
        boolean collision = missile.handleTankCollisions(tanks);

        // 断言没有发生碰撞
        assertFalse(collision);
    }

    @Test
    public void testHandleWallCollisions() {
        int x = 100;
        int y = 200;
        Tank.Direction direction = Tank.Direction.U;
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(x, y, true, direction, mockClient);

        Wall mockWall = mock(Wall.class);
        when(mockWall.getCollisionBounds()).thenReturn(new Rectangle(x, y, 10, 10));

        boolean collision = missile.handleWallCollision(mockWall);

        assertTrue(collision);
    }

    @Test
    public void testHandleWallCollisionsNoCollision() {
        int x = 100;
        int y = 200;
        Tank.Direction direction = Tank.Direction.U;
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(x, y, true, direction, mockClient);

        Wall mockWall = mock(Wall.class);
        when(mockWall.getCollisionBounds()).thenReturn(new Rectangle(x + 10, y + 10, 10, 10));

        boolean collision = missile.handleWallCollision(mockWall);

        assertFalse(collision);
    }

    @Test
    public void testGetPositionX() {
        int x = 100;
        int y = 200;
        Tank.Direction direction = Tank.Direction.U;
        Missile missile = new Missile(x, y, direction);

        assertEquals(x, missile.getPositionX());
    }

    @Test
    public void testGetPositionY() {
        int x = 100;
        int y = 200;
        Tank.Direction direction = Tank.Direction.U;
        Missile missile = new Missile(x, y, direction);

        assertEquals(y, missile.getPositionY());
    }

    @Test
    public void testUpdatePosition2() {
        // 初始化导弹位置和方向
        int initialX = 100;
        int initialY = 200;
        Tank.Direction direction = Tank.Direction.L; // 测试左移动
        TankClient mockClient = mock(TankClient.class);

        // 创建导弹对象
        Missile missile = new Missile(initialX, initialY, true, direction, mockClient);

        // 设置导弹的移动速度（假设 MOVEMENT_SPEED_X 和 MOVEMENT_SPEED_Y 是常量）
        Missile.MOVEMENT_SPEED_X = 10;
        Missile.MOVEMENT_SPEED_Y = 15;

        // 调用方法
        missile.updatePosition();

        // 验证位置是否正确更新
        assertEquals(initialX - Missile.MOVEMENT_SPEED_X, missile.getPositionX());
        assertEquals(initialY, missile.getPositionY());
    }

    @Test
    public void testUpdatePositionDiagonal() {
        // 初始化导弹位置和方向
        int initialX = 100;
        int initialY = 200;
        Tank.Direction direction = Tank.Direction.LU; // 测试左上角移动
        TankClient mockClient = mock(TankClient.class);

        // 创建导弹对象
        Missile missile = new Missile(initialX, initialY, true, direction, mockClient);

        // 设置导弹的移动速度
        Missile.MOVEMENT_SPEED_X = 10;
        Missile.MOVEMENT_SPEED_Y = 15;

        // 调用方法
        missile.updatePosition();

        // 验证位置是否正确更新
        assertEquals(initialX - Missile.MOVEMENT_SPEED_X, missile.getPositionX());
        assertEquals(initialY - Missile.MOVEMENT_SPEED_Y, missile.getPositionY());
    }

    @Test
    public void testUpdatePositionStop() {
        // 初始化导弹位置和方向
        int initialX = 100;
        int initialY = 200;
        Tank.Direction direction = Tank.Direction.STOP; // 测试静止方向
        TankClient mockClient = mock(TankClient.class);

        // 创建导弹对象
        Missile missile = new Missile(initialX, initialY, true, direction, mockClient);

        // 设置导弹的移动速度
        Missile.MOVEMENT_SPEED_X = 10;
        Missile.MOVEMENT_SPEED_Y = 15;

        // 调用方法
        missile.updatePosition();

        // 验证位置未发生变化
        assertEquals(initialX, missile.getPositionX());
        assertEquals(initialY, missile.getPositionY());
    }

    @Test
    public void testUpdatePositionBoundaryCollision() {
        // 初始化导弹位置和方向
        int initialX = 0; // 边界位置
        int initialY = 0; // 边界位置
        Tank.Direction direction = Tank.Direction.L; // 测试左移动
        TankClient mockClient = mock(TankClient.class);

        // 创建导弹对象
        Missile missile = spy(new Missile(initialX, initialY, true, direction, mockClient));

        // 设置导弹的移动速度
        Missile.MOVEMENT_SPEED_X = 10;
        Missile.MOVEMENT_SPEED_Y = 15;

        // 模拟 checkBoundaryCollision() 方法
        doNothing().when(missile).checkBoundaryCollision();

        // 调用方法
        missile.updatePosition();

        // 验证位置是否更新
        assertEquals(initialX - Missile.MOVEMENT_SPEED_X, missile.getPositionX());
        assertEquals(initialY, missile.getPositionY());

        // 验证是否调用了边界检测
        verify(missile, times(1)).checkBoundaryCollision();
    }

    @Test
    public void testUpdatePositionLeft() {
        int initialX = 100;
        int initialY = 200;
        Tank.Direction direction = Tank.Direction.L; // 向左
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(initialX, initialY, true, direction, mockClient);

        Missile.MOVEMENT_SPEED_X = 10;
        Missile.MOVEMENT_SPEED_Y = 15;

        missile.updatePosition();

        assertEquals(initialX - Missile.MOVEMENT_SPEED_X, missile.getPositionX());
        assertEquals(initialY, missile.getPositionY());
    }

    @Test
    public void testUpdatePositionLeftUp() {
        int initialX = 100;
        int initialY = 200;
        Tank.Direction direction = Tank.Direction.LU; // 左上
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(initialX, initialY, true, direction, mockClient);

        Missile.MOVEMENT_SPEED_X = 10;
        Missile.MOVEMENT_SPEED_Y = 15;

        missile.updatePosition();

        assertEquals(initialX - Missile.MOVEMENT_SPEED_X, missile.getPositionX());
        assertEquals(initialY - Missile.MOVEMENT_SPEED_Y, missile.getPositionY());
    }

    @Test
    public void testUpdatePositionUp() {
        int initialX = 100;
        int initialY = 200;
        Tank.Direction direction = Tank.Direction.U; // 向上
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(initialX, initialY, true, direction, mockClient);

        Missile.MOVEMENT_SPEED_X = 10;
        Missile.MOVEMENT_SPEED_Y = 15;

        missile.updatePosition();

        assertEquals(initialX, missile.getPositionX());
        assertEquals(initialY - Missile.MOVEMENT_SPEED_Y, missile.getPositionY());
    }

    @Test
    public void testUpdatePositionRightUp() {
        int initialX = 100;
        int initialY = 200;
        Tank.Direction direction = Tank.Direction.RU; // 右上
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(initialX, initialY, true, direction, mockClient);

        Missile.MOVEMENT_SPEED_X = 10;
        Missile.MOVEMENT_SPEED_Y = 15;

        missile.updatePosition();

        assertEquals(initialX + Missile.MOVEMENT_SPEED_X, missile.getPositionX());
        assertEquals(initialY - Missile.MOVEMENT_SPEED_Y, missile.getPositionY());
    }

    @Test
    public void testUpdatePositionRight() {
        int initialX = 100;
        int initialY = 200;
        Tank.Direction direction = Tank.Direction.R; // 向右
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(initialX, initialY, true, direction, mockClient);

        Missile.MOVEMENT_SPEED_X = 10;
        Missile.MOVEMENT_SPEED_Y = 15;

        missile.updatePosition();

        assertEquals(initialX + Missile.MOVEMENT_SPEED_X, missile.getPositionX());
        assertEquals(initialY, missile.getPositionY());
    }

    @Test
    public void testUpdatePositionRightDown() {
        int initialX = 100;
        int initialY = 200;
        Tank.Direction direction = Tank.Direction.RD; // 右下
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(initialX, initialY, true, direction, mockClient);

        Missile.MOVEMENT_SPEED_X = 10;
        Missile.MOVEMENT_SPEED_Y = 15;

        missile.updatePosition();

        assertEquals(initialX + Missile.MOVEMENT_SPEED_X, missile.getPositionX());
        assertEquals(initialY + Missile.MOVEMENT_SPEED_Y, missile.getPositionY());
    }

    @Test
    public void testUpdatePositionDown() {
        int initialX = 100;
        int initialY = 200;
        Tank.Direction direction = Tank.Direction.D; // 向下
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(initialX, initialY, true, direction, mockClient);

        Missile.MOVEMENT_SPEED_X = 10;
        Missile.MOVEMENT_SPEED_Y = 15;

        missile.updatePosition();

        assertEquals(initialX, missile.getPositionX());
        assertEquals(initialY + Missile.MOVEMENT_SPEED_Y, missile.getPositionY());
    }

    @Test
    public void testUpdatePositionLeftDown() {
        int initialX = 100;
        int initialY = 200;
        Tank.Direction direction = Tank.Direction.LD; // 左下
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(initialX, initialY, true, direction, mockClient);

        Missile.MOVEMENT_SPEED_X = 10;
        Missile.MOVEMENT_SPEED_Y = 15;

        missile.updatePosition();

        assertEquals(initialX - Missile.MOVEMENT_SPEED_X, missile.getPositionX());
        assertEquals(initialY + Missile.MOVEMENT_SPEED_Y, missile.getPositionY());
    }

    @Test
    public void testUpdatePositionStop2() {
        int initialX = 100;
        int initialY = 200;
        Tank.Direction direction = Tank.Direction.STOP; // 静止
        TankClient mockClient = mock(TankClient.class);
        Missile missile = new Missile(initialX, initialY, true, direction, mockClient);

        Missile.MOVEMENT_SPEED_X = 10;
        Missile.MOVEMENT_SPEED_Y = 15;

        missile.updatePosition();

        assertEquals(initialX, missile.getPositionX());
        assertEquals(initialY, missile.getPositionY());
    }
}