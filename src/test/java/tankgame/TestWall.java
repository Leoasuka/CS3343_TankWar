package tankgame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;

import static org.junit.jupiter.api.Assertions.*;

class TestWall {
    private Wall wall;
    private TankClient mockClient;
    private Tank tank;
    @BeforeEach
    void setUp() {
        mockClient = new TankClient(); // Assuming TankClient has a default constructor
        wall = new Wall(100, 100, 50, 50, mockClient);
        tank = new Tank(0, 0, false, Tank.Direction.STOP, mockClient);
    }

    @Test
    void testRender() {
        // Create a mock Graphics object
        Graphics graphics = new MockGraphics();
        wall.render(graphics);
        // Verify that the graphics methods were called with the correct parameters
        assertTrue(((MockGraphics) graphics).isFillRectCalled());
    }

    @Test
    void testHandleMissileCollision() {
        Missile missile = tank.fireMissile(); // Assuming Missile has this constructor
        assertFalse(wall.handleMissileCollision(missile));
    }

    @Test
    void testGetNearestPoint() {
        Point nearestPoint = wall.getNearestPoint(110, 110);
        assertEquals(new Point(110, 110), nearestPoint);

        nearestPoint = wall.getNearestPoint(80, 80);
        assertEquals(new Point(100, 100), nearestPoint); // Nearest point should be at the wall's corner

        nearestPoint = wall.getNearestPoint(200, 200);
        assertEquals(new Point(150, 150), nearestPoint); // Nearest point should be at the wall's edge
    }

    @Test
    void testIsPointBehindWall() {
        assertTrue(wall.isPointBehindWall(130, 130, 120, 120)); // Point is behind the wall
        assertFalse(wall.isPointBehindWall(80, 80, 120, 120)); // Point is in front of the wall
    }

    @Test
    void testGetCoverPositions() {
        Point[] coverPositions = wall.getCoverPositions();
        assertEquals(4, coverPositions.length);
        assertEquals(new Point(60, 100), coverPositions[0]); // Left
        assertEquals(new Point(190, 100), coverPositions[1]); // Right
        assertEquals(new Point(100, 60), coverPositions[2]); // Top
        assertEquals(new Point(100, 190), coverPositions[3]); // Bottom
    }

    @Test
    void testGetCollisionBounds() {
        Rectangle bounds = wall.getCollisionBounds();
        assertEquals(new Rectangle(100, 100, 50, 50), bounds);
    }

    @Test
    void testGetPositionX() {
        assertEquals(100, wall.getPositionX());
    }

    @Test
    void testGetPositionY() {
        assertEquals(100, wall.getPositionY());
    }

    @Test
    void testGetWidth() {
        assertEquals(50, wall.getWidth());
    }

    @Test
    void testGetHeight() {
        assertEquals(50, wall.getHeight());
    }

    // Mock classes for testing
    static class MockGraphics extends Graphics {
        private boolean fillRectCalled = false;

        @Override
        public void fillRect(int x, int y, int width, int height) {
            fillRectCalled = true;
        }

        public boolean isFillRectCalled() {
            return fillRectCalled;
        }

		@Override
		public Graphics create() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void translate(int x, int y) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Color getColor() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setColor(Color c) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setPaintMode() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setXORMode(Color c1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Font getFont() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setFont(Font font) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public FontMetrics getFontMetrics(Font f) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Rectangle getClipBounds() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void clipRect(int x, int y, int width, int height) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setClip(int x, int y, int width, int height) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Shape getClip() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setClip(Shape clip) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void copyArea(int x, int y, int width, int height, int dx, int dy) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void drawLine(int x1, int y1, int x2, int y2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void clearRect(int x, int y, int width, int height) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void drawOval(int x, int y, int width, int height) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void fillOval(int x, int y, int width, int height) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void drawString(String str, int x, int y) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void drawString(AttributedCharacterIterator iterator, int x, int y) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor,
				ImageObserver observer) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
				ImageObserver observer) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
				Color bgcolor, ImageObserver observer) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}

    }




}