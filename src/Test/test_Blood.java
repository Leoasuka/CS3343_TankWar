package Test;

import Resources.Blood;
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
        // 测试初始位置是否正确 (350, 300)
        Rectangle bounds = blood.getCollisionBounds();
        assertEquals(350, bounds.x);
        assertEquals(300, bounds.y);
        assertEquals(15, bounds.width);
        assertEquals(15, bounds.height);
        assertTrue(blood.isActive());
    }

    @Test
    void testRenderHealthPack() {
        // 保存原始颜色
        Color originalColor = Color.BLACK;
        when(mockGraphics.getColor()).thenReturn(originalColor);

        blood.renderHealthPack(mockGraphics);

        // 验证绘图方法调用
        verify(mockGraphics).setColor(Color.MAGENTA);
        verify(mockGraphics).fillRect(anyInt(), anyInt(), eq(15), eq(15));
        verify(mockGraphics).setColor(originalColor);
    }

    @Test
    void testRenderHealthPackWhenInactive() {
        blood.setActive(false);
        blood.renderHealthPack(mockGraphics);

        // 验证当血包不活跃时不会调用任何绘图方法
        verify(mockGraphics, never()).setColor(any(Color.class));
        verify(mockGraphics, never()).fillRect(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void testUpdatePosition() {
        // 触发位置更新，移动到第二个位置 (360, 300)
        blood.renderHealthPack(mockGraphics);

        Rectangle newBounds = blood.getCollisionBounds();
        // 验证新位置是路径中的第二个点
        assertEquals(360, newBounds.x);
        assertEquals(300, newBounds.y);
    }

    @Test
    void testCompletePathCycle() {
        // 测试完整路径循环
        // 路径长度是7，所以循环7次后应该回到起点
        for (int i = 0; i < 7; i++) {
            blood.renderHealthPack(mockGraphics);
        }

        Rectangle bounds = blood.getCollisionBounds();
        // 验证回到起始位置 (350, 300)
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

        // 验证碰撞箱大小
        assertEquals(15, bounds.width);
        assertEquals(15, bounds.height);

        // 验证位置
        assertTrue(bounds.x >= 0);
        assertTrue(bounds.y >= 0);
    }
}