package tankgame;

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

        @Test
        public void testHandleWallCollision() {
            int x = 100;
            int y = 200;
            Tank.Direction direction = Tank.Direction.U;
            Missile missile = new Missile(x, y, direction);

            assertTrue(missile.handleWallCollision());
            assertFalse(missile.isActive());
        }

        @Test
        public void testHandleMissileCollision() {
            int x = 100;
            int y = 200;
            Tank.Direction direction = Tank.Direction.U;
            Missile missile = new Missile(x, y, direction);
            Missile otherMissile = new Missile(x, y, direction);

            assertTrue(missile.handleMissileCollision(otherMissile));
            assertFalse(missile.isActive());
            assertFalse(otherMissile.isActive());
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
            Missile missile = new Missile(x, y, direction);
            missile.setActive(false);

            Graphics mockGraphics = mock(Graphics.class);

            missile.render(mockGraphics);

            verify(mockGraphics, never()).fillOval(x, y, Missile.MISSILE_WIDTH, Missile.MISSILE_HEIGHT);
            verify(mockGraphics, never()).fillOval(x-1, y-1, Missile.MISSILE_WIDTH+2, Missile.MISSILE_HEIGHT+2);
        }

    }

