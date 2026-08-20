# TextureArraySpriteBatch
An OpenGL 2.0 Batch implementation for LibGDX that leverages Texture Arrays for performant draw calls spanning multiple textures.

## Usage

```kotlin
val batch = TextureArraySpriteBatch()
batch.begin()
// <-- Draw everything in frame
batch.end()
```

# BindlessStage
A Scene2d.ui Stage implementation that does not force a Batch to begin() or end(), thus not implicitly triggering any binds. It is designed for use with a TextureArraySpriteBatch that might already have textures in its cache from prior draw calls this frame.

## Usage

```kotlin
val batch = TextureArraySpriteBatch()
val stage = BindlessStage(ScreenViewport(),textureArraySpriteBatch)

batch.begin()
// <-- Draw lots of cool stuff under the stage
stage.act()
stage.draw()
// <-- Draw lots of cool stuff over the stage
batch.end()
```