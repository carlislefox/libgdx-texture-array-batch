# TextureArraySpriteBatch
An OpenGL 2.0 Batch implementation for LibGDX that leverages Texture Arrays for performant draw calls spanning multiple textures. This is a drop-in replacement for `SpriteBatch`, and has been tested to maturity on Android, Nvidia, and AMD hardware.

## Gradle

```groovy
dependencies {
    implementation 'com.github.carlislefox:libgdx-texture-array-batch:1.0'
}
```

## Usage

```kotlin
val batch = TextureArraySpriteBatch()

fun update(delta: Float) {
   batch.begin()
   // <-- Draw everything in frame
   batch.end()
}
```

## BindlessStage
A Scene2d.ui Stage implementation that does not force a `Batch` to `begin()` or `end()`, thus not implicitly triggering any binds. It is designed for use with a `TextureArraySpriteBatch` that might already have textures in its cache from prior draw calls this frame.

```kotlin
val batch = TextureArraySpriteBatch()
val stage = BindlessStage(ScreenViewport(), textureArraySpriteBatch)

fun update(delta: Float) { 
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

## Special Thanks
I cannot stress enough how much I have benefitted from code, time, and patience shared by numerous LibGDX community members to get to this point.  A massive special thank you to:

- VaTTeRGeR _(base implementation)_
- fgnm _(fragment shader workarounds for AMD compatability)_
- tommyettinger _(the man, the myth, the legend)_