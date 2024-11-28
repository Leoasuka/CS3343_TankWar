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
                assertEquals(y - missile.getSpeed(), missile.getPositionY());
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

}

