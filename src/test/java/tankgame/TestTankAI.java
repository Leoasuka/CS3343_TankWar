package tankgame;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestTankAI {
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
    void testDecideToShoot() {
        // Create a mock TankClient
        TankClient mockTankClient = mock(TankClient.class);

        // Create a controlled tank
        Tank controlledTank = mock(Tank.class);
        when(controlledTank.isAlive()).thenReturn(true);

        // Create a player tank
        Tank playerTank = mock(Tank.class);
        when(playerTank.isAlive()).thenReturn(true);
        when(mockTankClient.getPlayerTank()).thenReturn(playerTank);

        // Create the TankAI instance
        TankAI tankAI = new TankAI(controlledTank, mockTankClient);

        // Mock the hasLineOfSight method to return true
        TankAI spyTankAI = spy(tankAI);
        doReturn(true).when(spyTankAI).hasLineOfSight(playerTank);

        // Call the decideToShoot method with a distance within shooting range
        spyTankAI.decideToShoot(100);

        // Verify that the controlled tank fires a missile
        verify(controlledTank, atLeastOnce()).fireMissile();
    }
}