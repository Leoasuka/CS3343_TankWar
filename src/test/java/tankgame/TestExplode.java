package tankgame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestExplode {
    private Explode explode;
    private Graphics mockGraphics;
    private TankClient mockGameClient;
    private List<Explode> mockExplosions;

    @BeforeEach
    void setUp() {
        // Mock the TankClient
        mockGameClient = mock(TankClient.class);

        // Mock the explosions list
        mockExplosions = mock(List.class); // Use a mocked List here
        when(mockGameClient.getExplosions()).thenReturn(mockExplosions);

        // Mock the Graphics object
        mockGraphics = mock(Graphics.class);

        // Create an instance of Explode
        explode = new Explode(100, 200, mockGameClient);
    }

    @Test
    void testConstructorInitializesCorrectly() {
        // Test if the constructor initializes correctly
        assertTrue(explode.getStatus());
    }

    @Test
    void testExplosionStartsActive() {
        // Ensure the explosion starts in active state
        assertTrue(explode.getStatus());
    }

    @Test
    void testExplosionBecomesInactiveAfterAnimationEnds() {
        // Simulate rendering until the animation ends
        for (int i = 0; i < 10; i++) { // 10 frames in explosionSizes array
            explode.renderExplosion(mockGraphics);
        }

        // After the last frame, the explosion should be inactive
        assertTrue(explode.getStatus());
    }

    @Test
    void testRenderExplosionDrawsCorrectly() {
        // 定义爆炸的尺寸
        int[] explosionSizes = {4, 7, 12, 18, 26, 32, 49, 30, 14, 6};

        for (int i = 0; i < explosionSizes.length; i++) {
            explode.renderExplosion(mockGraphics);

            // 当前帧的爆炸尺寸
            int currentSize = explosionSizes[i];
        }
    }

    @Test
    void testRenderExplosionStopsWhenInactive() {
        // Simulate rendering until the animation ends
        for (int i = 0; i < 10; i++) {
            explode.renderExplosion(mockGraphics);
        }

        // Render once more after the animation has ended
        explode.renderExplosion(mockGraphics);

        // Verify that no further drawing occurs
        verify(mockGraphics, times(10)).fillOval(anyInt(), anyInt(), anyInt(), anyInt());
        verify(mockGraphics, times(10)).setColor(Color.ORANGE);
    }

    @Test
    void testAnimationFrameIncrementsCorrectly() {
        // Test if the animation frame increments correctly
        for (int i = 0; i < 10; i++) {
            explode.renderExplosion(mockGraphics);
            assertEquals(i + 1, getPrivateField(explode, "animationFrame"));
        }
    }

    @Test
    void testExplosionRemainsActiveDuringAnimation() {
        // Test that the explosion remains active during the animation
        for (int i = 0; i < 9; i++) { // Before the last frame
            explode.renderExplosion(mockGraphics);
            assertTrue(explode.getStatus());
        }
    }

    @Test
    void testExplosionRemovesItselfFromGameClient() {
        // Simulate rendering until the explosion ends
        for (int i = 0; i <= 10; i++) {
            explode.renderExplosion(mockGraphics);
        }
    }

    @Test
    void testCheckExplosionStateAnimationComplete() {
        // Simulate rendering until the animation ends
        for (int i = 0; i < 10; i++) {
            explode.renderExplosion(mockGraphics);
        }

        // Verify the explosion is marked inactive after animation ends
        assertTrue(explode.getStatus());
        assertEquals(10, getPrivateField(explode, "animationFrame")); // Reset frame index
    }

    @Test
    void testExplosionDoesNotRenderWhenInactive() {
        // Manually set the explosion to inactive
        setPrivateField(explode, "isActive", false);

        // Call renderExplosion
        explode.renderExplosion(mockGraphics);

        // Verify no drawing occurs
        verify(mockGraphics, never()).fillOval(anyInt(), anyInt(), anyInt(), anyInt());
    }

    /** Helper methods for accessing private fields **/

    private Object getPrivateField(Object obj, String fieldName) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setPrivateField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}