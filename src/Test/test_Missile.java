package Test;

import Resources.Missile;
import Resources.Tank;
import Resources.TankClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class test_Missile {

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
    }
