# TextureArraySpriteBatch
An OpenGL 2.0 Batch implementation for LibGDX that leverages Texture Arrays for performant draw calls spanning multiple textures. This is a drop-in replacement for `SpriteBatch`, and has been tested to maturity on Android, Nvidia, and AMD hardware.

## Gradle

```groovy
dependencies {
    implementation 'com.github.carlislefox:libgdx-texture-array-batch:v1.1'
}
```

## Usage
Texture Array Sprite Batches are created and used exactly like a vanilla SpriteBatch.

### Kotlin
```kotlin
val batch = TextureArraySpriteBatch()

fun update(delta: Float) {
   batch.begin()
   // <-- Draw everything in frame
   batch.end()
}
```
### Java
```java
final TextureArraySpriteBatch batch = new TextureArraySpriteBatch();

public void update(float delta) {
   batch.begin();
   // <-- Draw everything in frame
   batch.end();
}
```

## Legacy Device Support
It is good practice to fall back to a vanilla implementation on instantiation, ensuring older devices that do not support Texture Arrays still work, albeit at a performance defecit.

### Kotlin
```kotlin
val batch = try {
   TextureArraySpriteBatch()
} catch (e: Exception) {
   SpriteBatch()
}
```
### Java
```java
Batch batch;

try {
   batch = new TextureArraySpriteBatch();
} catch (Exception e) {
   batch = new SpriteBatch();
}
```

## BindlessStage
A Scene2d.ui Stage implementation that does not force a `Batch` to `begin()` or `end()`, thus not implicitly triggering any binds. It is designed for use with a `TextureArraySpriteBatch` that might already have textures in its cache from prior draw calls this frame, or may be performing subsequent draws that leverage textures already used during the rendering of the scene.

### Kotlin
```kotlin
val batch: Batch = TextureArraySpriteBatch()
val stage: Stage = BindlessStage(ScreenViewport(), textureArraySpriteBatch)

fun update(delta: Float) {
   Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT) // Clear the frame buffer
    
   batch.begin() 
   // <-- Draw lots of cool stuff under the stage
   stage.apply {
       viewport.apply()
       act(delta)
       draw()
   }
   // <-- Draw lots of cool stuff over the stage
   batch.end()
}
```
### Java
```java
final Batch batch = new TextureArraySpriteBatch();
final Stage stage = new BindlessStage(new ScreenViewport(), batch);

public void update(float delta) {
   Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the frame buffer
   
   batch.begin();
   // <-- Draw lots of cool stuff under the stage
   stage.getViewport().apply();
   stage.act(delta);
   stage.draw();
   // <-- Draw lots of cool stuff over the stage
   batch.end();
}
```

## Special Thanks
I cannot stress enough how much I have benefitted from code, time, and patience shared by numerous LibGDX community members to get to this point.  A massive special thank you to:

- VaTTeRGeR _(base implementation)_
- fgnm _(fragment shader workarounds for AMD compatability)_
- tommyettinger _(the man, the myth, the legend)_