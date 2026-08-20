package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Designed for use with a {@link com.badlogic.gdx.graphics.g2d.TextureArraySpriteBatch} instance. Does not force the
 * Batch to begin() or end() so does not implicitly clear the texture cache, thus avoiding unnecessary in-frame
 * rebinding of textures.
 *
 * @author Fhox
 */
public class BindlessStage extends Stage {

    /** Creates a stage with the specified viewport and batch. This can be used to specify an existing batch or to customize which
     * batch implementation is used.
     * @param batch Will not be disposed if {@link #dispose()} is called, handle disposal yourself. */
    public BindlessStage(Viewport viewport, Batch batch) {
        super(viewport, batch);
        getRoot().setTransform(false);
    }

    @Override
    public void draw() {
        final Camera camera = getViewport().getCamera();
        camera.update();

        if (!getRoot().isVisible()) return;

        final Batch batch = this.getBatch();
        batch.setProjectionMatrix(camera.combined);

        if (!batch.isDrawing()) {
            batch.begin();
        }

        getRoot().draw(batch, 1);
        batch.flush();
    }

}