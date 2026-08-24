package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxNativesLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.nio.IntBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TextureArraySpriteBatchTest {
    private GL20 previousGl;
    private GL20 previousGl20;
    private GL30 previousGl30;
    private Graphics previousGraphics;

    private GL20 gl;
    private ShaderProgram shader;

    @Before
    public void setUp() throws Exception {
        GdxNativesLoader.load();

        previousGl = Gdx.gl;
        previousGl20 = Gdx.gl20;
        previousGl30 = Gdx.gl30;
        previousGraphics = Gdx.graphics;

        gl = mock(GL20.class);
        doAnswer(invocation -> {
            IntBuffer values = invocation.getArgument(1);
            values.put(0, 4);
            return null;
        }).when(gl).glGetIntegerv(anyInt(), any(IntBuffer.class));
        when(gl.glGenBuffer()).thenReturn(1);

        Graphics graphics = mock(Graphics.class);
        when(graphics.getWidth()).thenReturn(800);
        when(graphics.getHeight()).thenReturn(600);

        Gdx.gl = gl;
        Gdx.gl20 = gl;
        Gdx.gl30 = null;
        Gdx.graphics = graphics;

        shader = mock(ShaderProgram.class);
        // Shader capability probing requires a real driver; these unit tests exercise batch behavior instead.
        resetStaticField("maxTextureUnits", 4);
        resetStaticField("shaderErrorLog", null);
    }

    @After
    public void tearDown() throws Exception {
        resetStaticField("maxTextureUnits", -1);
        resetStaticField("shaderErrorLog", null);
        Gdx.gl = previousGl;
        Gdx.gl20 = previousGl20;
        Gdx.gl30 = previousGl30;
        Gdx.graphics = previousGraphics;
    }

    @Test
    public void rejectsDrawOutsideBeginEnd() {
        TextureArraySpriteBatch batch = new TextureArraySpriteBatch(1, shader);
        Texture texture = texture(1);

        assertThrows(IllegalStateException.class, () -> batch.draw(texture, 0, 0));

        batch.dispose();
    }

    @Test
    public void bulkDrawHonorsOffsetAndInjectsTextureIndex() throws Exception {
        TextureArraySpriteBatch batch = new TextureArraySpriteBatch(1, shader);
        Texture texture = texture(7);
        float[] source = new float[40];
        for (int i = 0; i < source.length; i++) source[i] = i;

        batch.begin();
        batch.draw(texture, source, 20, 20);

        float[] output = field(batch, "vertices", float[].class);
        assertEquals(24, (int)field(batch, "idx", Integer.class));
        for (int vertex = 0; vertex < 4; vertex++) {
            for (int attribute = 0; attribute < 5; attribute++) {
                assertEquals(source[20 + vertex * 5 + attribute], output[vertex * 6 + attribute], 0f);
            }
            assertEquals(0f, output[vertex * 6 + 5], 0f);
        }

        batch.dispose();
    }

    @Test
    public void bulkDrawRejectsPartialSprites() {
        TextureArraySpriteBatch batch = new TextureArraySpriteBatch(1, shader);
        Texture texture = texture(1);
        batch.begin();

        assertThrows(IllegalArgumentException.class, () -> batch.draw(texture, new float[19], 0, 19));

        batch.dispose();
    }

    @Test
    public void bulkDrawFlushesWhenBatchIsFull() throws Exception {
        TextureArraySpriteBatch batch = new TextureArraySpriteBatch(1, shader);
        Texture texture = texture(1);
        batch.begin();

        batch.draw(texture, new float[40], 0, 40);

        assertEquals(1, batch.renderCalls);
        assertEquals(24, (int)field(batch, "idx", Integer.class));
        verify(shader, times(1)).bind();

        batch.dispose();
    }

    @Test
    public void consecutiveDrawsReuseTheTextureSlot() {
        TextureArraySpriteBatch batch = new TextureArraySpriteBatch(2, shader);
        Texture texture = texture(11);
        batch.begin();

        batch.draw(texture, 0, 0);
        batch.draw(texture, 10, 10);

        verify(texture, times(1)).bind(0);
        assertEquals(1, batch.getTextureLFUSize());

        batch.dispose();
    }

    @Test
    public void flushDoesNotReapplyBlendState() {
        TextureArraySpriteBatch batch = new TextureArraySpriteBatch(1, shader);
        Texture texture = texture(1);
        batch.begin();

        batch.draw(texture, 0, 0);
        batch.flush();
        batch.draw(texture, 0, 0);
        batch.flush();

        verify(gl, times(1)).glEnable(GL20.GL_BLEND);
        verify(gl, times(1)).glBlendFuncSeparate(
                GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA,
                GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        verify(gl, never()).glDisable(GL20.GL_BLEND);

        batch.dispose();
    }

    @Test
    public void changingBlendStateFlushesBeforeApplyingIt() {
        TextureArraySpriteBatch batch = new TextureArraySpriteBatch(1, shader);
        Texture texture = texture(1);
        batch.begin();
        batch.draw(texture, 0, 0);

        batch.disableBlending();

        assertEquals(1, batch.renderCalls);
        verify(gl, times(1)).glDisable(GL20.GL_BLEND);
        batch.dispose();
    }

    private Texture texture(int handle) {
        Texture texture = mock(Texture.class);
        when(texture.getWidth()).thenReturn(16);
        when(texture.getHeight()).thenReturn(16);
        when(texture.getTextureObjectHandle()).thenReturn(handle);
        return texture;
    }

    private static void resetStaticField(String name, Object value) throws Exception {
        Field field = TextureArraySpriteBatch.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    private static <T> T field(Object target, String name, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }
}
