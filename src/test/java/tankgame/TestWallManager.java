package tankgame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestWallManager {
    private WallManager wallManager;
    private TankClient mockClient;

    @BeforeEach
    void setUp() {
        mockClient = new TankClient();
        wallManager = new WallManager(mockClient);
    }

    @Test
    void testAddPermanentWall() {
        wallManager.addPermanentWall(100, 200, 20, 150);
        List<Wall> walls = Arrays.asList(wallManager.getAllWalls());
        assertEquals(10, walls.size());
        assertTrue(walls.get(0) instanceof Wall);
    }

    @Test
    void testAddBreakableWall() {
        wallManager.addBreakableWall(500, 300, 100, 20);
        List<Wall> walls = Arrays.asList(wallManager.getAllWalls());
        assertEquals(10, walls.size());
        assertFalse(walls.get(0) instanceof BreakableWall);
    }

    @Test
    void testAddFortifiedWall() {
        wallManager.addFortifiedWall(400, 200, 20, 100);
        List<Wall> walls = Arrays.asList(wallManager.getAllWalls());
        assertEquals(10, walls.size());
        assertFalse(walls.get(0) instanceof BreakableWall);
    }

    @Test
    void testAddTemporaryWall() {
        wallManager.addTemporaryWall(100, 100, 50, 50);
        List<Wall> walls = Arrays.asList(wallManager.getAllWalls());
        assertEquals(10, walls.size());
        assertFalse(walls.get(0) instanceof TemporaryWall);
    }

    @Test
    void testClearWalls() {
        wallManager.addPermanentWall(100, 200, 20, 150);
        wallManager.addBreakableWall(500, 300, 100, 20);
        wallManager.addTemporaryWall(100, 100, 50, 50);
        wallManager.cleanWalls();
        List<Wall> walls = Arrays.asList(wallManager.getAllWalls());
        assertEquals(0, walls.size());
    }

    @Test
    void testGenerateRandomWalls() {
        wallManager.cleanWalls();
        wallManager.generateRandomWalls(5);
        List<Wall> walls = Arrays.asList(wallManager.getAllWalls());
        assertEquals(5, walls.size());
    }

    @Test
    void testUpdate() {
        wallManager.addTemporaryWall(100, 100, 50, 50);
        TemporaryWall tempWall = null;
        for (Wall wall : wallManager.getAllWalls()) {
            if (wall instanceof TemporaryWall) {
                tempWall = (TemporaryWall) wall;
                break;
            }
        }
        assertNotNull(tempWall);
        tempWall.hasExpired(System.currentTimeMillis() + 20000); // Simulate expiration
        wallManager.update(System.currentTimeMillis() + 20000);
        List<Wall> walls = Arrays.asList(wallManager.getAllWalls());
        assertEquals(8, walls.size());
    }

    @Test
    void testRender() {
        wallManager.addPermanentWall(100, 200, 20, 150);
        BufferedImage bufferedImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = bufferedImage.getGraphics();
        wallManager.render(graphics);
        // No assertions, just ensure no exceptions are thrown
    }

    @Test
    void testHandleMissileCollision() {
        wallManager.addBreakableWall(100, 100, 50, 50);
        Missile missile = new Missile(120, 120, true, Tank.Direction.LU, mockClient);
        assertTrue(wallManager.handleMissileCollision(missile));
    }

    @Test
    void testHandleTankCollision() {
        wallManager.addPermanentWall(100, 100, 50, 50);
        Tank tank = new Tank(120, 120, true, Tank.Direction.LU, mockClient);
        wallManager.handleTankCollision(tank);
        // No assertions, just ensure no exceptions are thrown
    }

    @Test
    void testTemporaryWallExpiration() {
        wallManager.addTemporaryWall(100, 100, 50, 50);
        TemporaryWall tempWall = null;
        for (Wall wall : wallManager.getAllWalls()) {
            if (wall instanceof TemporaryWall) {
                tempWall = (TemporaryWall) wall;
                break;
            }
        }
        assertNotNull(tempWall);
        assertFalse(tempWall.hasExpired(System.currentTimeMillis()));
        assertTrue(tempWall.hasExpired(System.currentTimeMillis() + WallManager.TEMPORARY_WALL_DURATION + 1));
    }

    @Test
    void testBreakableWallDamage() {
        wallManager.addBreakableWall(100, 100, 50, 50);
        BreakableWall breakableWall = null;
        for (Wall wall : wallManager.getAllWalls()) {
            if (wall instanceof BreakableWall) {
                breakableWall = (BreakableWall) wall;
                break;
            }
        }
        assertNotNull(breakableWall);
        breakableWall.damage(50);
        assertEquals(450, breakableWall.getHealth());
        breakableWall.damage(60);
        assertTrue(breakableWall.isAlive());
    }

    @Test
    void testFortifiedWallHealth() {
        wallManager.addFortifiedWall(100, 100, 50, 50);
        BreakableWall fortifiedWall = null;
        for (Wall wall : wallManager.getAllWalls()) {
            if (wall instanceof BreakableWall) {
                fortifiedWall = (BreakableWall) wall;
                break;
            }
        }
        assertNotNull(fortifiedWall);
        assertEquals(WallManager.MAX_FORTIFIED_HEALTH, fortifiedWall.getHealth());
    }
}