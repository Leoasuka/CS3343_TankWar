package test;

import resources.Blood;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BloodTest {
    private Blood blood;
    private Graphics mockGraphics;

    @BeforeEach
    void setUp() {
        blood = new Blood();
        mockGraphics = mock(Graphics.class);
    }

    @Test
    void testBloodInitialization() {
        // test for the initial position (350, 300)
        Rectangle bounds = blood.getCollisionBounds();
        assertEquals(350, bounds.x);
        assertEquals(300, bounds.y);
        assertEquals(15, bounds.width);
        assertEquals(15, bounds.height);
        assertTrue(blood.isActive());
    }

    @Test
    void testRenderHealthPack() {
        // test for the original colour
        Color originalColor = Color.BLACK;
        when(mockGraphics.getColor()).thenReturn(originalColor);

        blood.renderHealthPack(mockGraphics);

        // test for the usage of graphics
        verify(mockGraphics).setColor(Color.MAGENTA);
        verify(mockGraphics).fillRect(anyInt(), anyInt(), eq(15), eq(15));
        verify(mockGraphics).setColor(originalColor);
    }

    @Test
    void testRenderHealthPackWhenInactive() {
        blood.setActive(false);
        blood.renderHealthPack(mockGraphics);

        // test for no graphics when no health pack is activated
        verify(mockGraphics, never()).setColor(any(Color.class));
        verify(mockGraphics, never()).fillRect(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void testUpdatePosition() {
        // test for updating position (360, 300)
        blood.renderHealthPack(mockGraphics);

        Rectangle newBounds = blood.getCollisionBounds();
        // test for the new position sequences
        assertEquals(360, newBounds.x);
        assertEquals(300, newBounds.y);
    }

    @Test
    void testCompletePathCycle() {
        // test for the complete loop
        // path lenghth of 7, after the loop return to the starting position.
        for (int i = 0; i < 7; i++) {
            blood.renderHealthPack(mockGraphics);
        }

        Rectangle bounds = blood.getCollisionBounds();
        // test for returning to the starting position (350, 300)
        assertEquals(350, bounds.x);
        assertEquals(300, bounds.y);
    }

    @Test
    void testActiveStatusToggle() {
        assertTrue(blood.isActive());

        blood.setActive(false);
        assertFalse(blood.isActive());

        blood.setActive(true);
        assertTrue(blood.isActive());
    }

    @Test
    void testCollisionBoundsAccuracy() {
        Rectangle bounds = blood.getCollisionBounds();

        // test the collision bondaries
        assertEquals(15, bounds.width);
        assertEquals(15, bounds.height);

        // position test.
        assertTrue(bounds.x >= 0);
        assertTrue(bounds.y >= 0);
    }
}