package test;
/*
import resources.Explode;
import resources.TankClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

public class test_Explode {
	

    private TankClient mockTankClient;
    private Explode explode;
    private Graphics mockGraphics;

    @BeforeEach
    void setUp() {
        // Mock the TankClient and Graphics objects
        mockTankClient = Mockito.mock(TankClient.class);
        mockGraphics = Mockito.mock(Graphics.class);
        when(mockTankClient.getExplosions()).thenReturn(new ArrayList<>());
        
        // Initialize the Explode object
        explode = new Explode(100, 100, mockTankClient);
    }

    @Test
    void testInitialState() {
        assertTrue(
        	"Explosion should be active upon creation",
        	explode.getStatus()
        );
    }

    @Test
    void testRenderExplosionUpdatesFrame() {
        explode.renderExplosion(mockGraphics);
        assertFalse(
        	"Explosion should not be active after rendering all frames",
        	explode.getStatus()
        );
    }

    @Test
    void testRenderExplosionRemovesFromClient() {
        // Render all frames of the explosion
        for (int i = 0; i < 10; i++) {
            explode.renderExplosion(mockGraphics);
        }

        // Verify that the explosion was removed from the TankClient
        verify(mockTankClient.getExplosions()).remove(explode);
    }

    @Test
    void testExplosionSizeRendering() {
        // Render the first frame
        explode.renderExplosion(mockGraphics);
        
        // Verify that the correct size is rendered
        verify(mockGraphics).fillOval(98, 98, 4, 4); // First size is 4
    }

    @Test
    void testExplosionCentering() {
        // Render the second frame
        explode.renderExplosion(mockGraphics);
        
        // Verify that the second size is rendered centered
        verify(mockGraphics).fillOval(96, 96, 7, 7); // Second size is 7
    }
}*/
