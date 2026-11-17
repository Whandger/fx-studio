# Minecraft FX Studio

**Minecraft FX Studio** is a standalone Java desktop application for editing, compiling, executing, and previewing Java-based Minecraft visual effects (VFX) in an interactive 3D viewport — without launching Minecraft.

Built with **Java 21, JavaFX, and JOML**, its purpose is to make it easier to iterate on the geometry and animation logic of compatible Minecraft VFX classes directly from a desktop workspace.

> **Important:** FX Studio is a lightweight VFX preview environment, not a Minecraft client or a complete Minecraft renderer. It does not launch Fabric, load a Minecraft world, or reproduce Minecraft's full rendering pipeline, shaders, textures, lighting engine, entities, or game state. It provides compatibility interfaces for the vertex and vector APIs used by supported effect classes and renders captured geometry with its own JavaFX renderer.

## Features

### Java Source Workspace

- Browse project files through a built-in project explorer.
- Open and edit Java source files using the integrated editor.
- Display source line numbers and file modification status.
- Import external `.java` files into the active project.
- Save edited source files.
- Delete Java source files and JAR dependencies with confirmation.
- Organize effects and shared helper classes within the same project.

### Multi-File Java Compilation

- Compile all Java source files found under the project's `src` directory in a single compilation.
- Resolve references between effect classes and their helper classes.
- Support external JAR dependencies.
- Automatically load dependencies stored in the project's `libs` directory.
- Display compiler errors and warnings in the integrated console.
- Report source filenames, line numbers, and column numbers when available.
- Compile source code in memory and generate class files in a temporary directory.

### Effect Execution and Frame Capture

- Discover compatible effect classes and invoke their static `draw` methods.
- Execute effects at successive animation ages.
- Capture generated geometry into individual animation frames.
- Run effect generation on a separate worker thread.
- Apply an 8-second execution timeout.
- Limit frame generation to a maximum of 2,000 steps.
- Display runtime errors and source locations when available.

The execution timeout is a reliability safeguard, **not a security sandbox**. Imported Java code executes with the permissions of the application process. Only run source code and dependencies you trust.

### Minecraft-Style Vertex Capture

FX Studio includes a recording vertex consumer compatible with the Minecraft vertex-consumer method shapes used by supported source code.

It captures:

- Vertex positions.
- Per-vertex RGBA color values.
- UV coordinates.
- Vertex normals.
- Triangle and quad geometry.

Quad primitives are converted into two triangles for rendering in the 3D viewport.

The preview renderer groups captured geometry by color and renders it using JavaFX materials. Although UV coordinates and normals are captured, Minecraft textures, light maps, overlays, and shaders are not reproduced.

### Interactive 3D Viewport

- Preview captured effect geometry as 3D meshes.
- Orbit the camera using the **left mouse button**.
- Pan the camera using the **right mouse button**.
- Zoom using the **mouse wheel**.
- Switch between perspective and orthographic camera modes.
- Toggle the ground grid.
- Toggle XYZ coordinate axes.
- Enable wireframe rendering.
- Reset the camera to its default position.
- Display the current triangle count.

### Animation Timeline and Playback

- Generate animation frames by evaluating effects at successive `age` values.
- Scrub through the animation using the timeline slider.
- Select individual frames through the frame timeline.
- Play, pause, and reset animation playback.
- Step backward or forward one frame at a time.
- Adjust playback speed:
  - 0.25x
  - 0.5x
  - 1.0x
  - 1.5x
  - 2.0x
  - 4.0x
- Inspect the selected frame's age, duration, and triangle count.

### Effect Parameters

Configure effect parameters directly from the application interface:

| Parameter | Description |
| --- | --- |
| `duration` | Effect duration passed to the Java `draw` method. |
| `step` | Increment applied to `age` between generated frames. Smaller values generate more frames. |
| `dest.x` | Destination X coordinate. |
| `dest.y` | Destination Y coordinate. |
| `dest.z` | Destination Z coordinate. |

The executor samples animation ages from `0` through `duration + 6`, using the configured frame step, subject to the 2,000-step limit.

## Supported Effect Method

FX Studio searches for a static method named `draw` using one of the supported signatures:

**1. Effect without a vector argument**

```java
public static void draw(
    Matrix4fc matrix,
    VertexConsumer vertices,
    float age,
    int duration
)
```

**2. Effect receiving a destination vector**

```java
public static void draw(
    Matrix4fc matrix,
    VertexConsumer vertices,
    Vec3 destination,
    float age,
    int duration
)
```

**3. Effect receiving origin and destination vectors**

```java
public static void draw(
    Matrix4fc matrix,
    VertexConsumer vertices,
    Vec3 origin,
    Vec3 destination,
    float age,
    int duration
)
```

The vector parameter can use either the compatibility `net.minecraft.world.phys.Vec3` type or `net.minecraft.util.math.Vec3d` type.

For the six-argument signature, FX Studio supplies a zero vector as the origin and the configured destination as the destination. For the five-argument signature, it supplies the configured destination.

The project includes compatibility API stubs for the Minecraft and JOML types currently required by the preview environment. These stubs cover specific method signatures and are not replacements for the complete Minecraft or Fabric APIs.

Effects that depend on unavailable game systems, assets, runtime objects, or unsupported methods may need to be adapted before they can run in FX Studio.

## Getting Started

### Requirements

- Windows for the provided launch and packaging scripts.
- **JDK 21** — a full JDK installation, not only a JRE.
- Gradle can be run through the included Gradle wrapper. A global Gradle installation is only necessary if the wrapper is unavailable.

A full JDK is required because FX Studio uses the system Java compiler to compile imported effect sources.

### Run from Source

1. Clone the repository:

```bash
git clone https://github.com/Whandger/fx-studio.git
cd fx-studio
```

2. Make sure JDK 21 is installed and available on your `PATH`.

3. Run the Windows launcher:

```bat
run-fx-studio.bat
```

Alternatively, launch the application through Gradle:

```bat
gradlew.bat run
```

On startup, FX Studio loads the bundled `sample-project` workspace and compiles its selected effect for the initial preview.

### Build a Standalone Windows Application

With JDK 21 installed, including `jpackage` available on your `PATH`, execute:

```bat
build-windows-app.bat
```

The script builds the application JAR and uses `jpackage` to generate a Windows application image.

The resulting executable is located at:

```text
dist\MinecraftFXStudio\MinecraftFXStudio.exe
```

This produces an application image, not an installer. Distributable builds can be packaged separately for release.

## Project Structure

A typical FX Studio project uses the following structure:

```text
my-vfx-project/
├── fxstudio.properties
├── src/
│   ├── MyEffectVfx.java
│   ├── VfxGeometry.java
│   └── VfxPrimitives.java
└── libs/
    └── dependency.jar
```

- `fxstudio.properties` stores the project name, selected main source file, duration, frame step, and destination coordinates.
- `src/` contains Java effect sources and helper classes. All Java files under this directory are included in project compilation.
- `libs/` contains external JAR dependencies.

The repository's `sample-project/` directory contains example effects and helper classes that can be used as a starting point.

## Included Example Effects

The bundled sample project contains a collection of Java VFX implementations, including:

### Fire and Flame Effects

- Fireball
- Flame Burst
- Burning Ground
- Inferno

### Ice Effects

- Frost Nova
- Ice Shard
- Ice Prison
- Blizzard
- Absolute Zero

### Lightning Effects

- Lightning
- Chain Lightning
- Thunderstorm

### Divine Effects

- Divine Healing
- Divine Protection
- Holy Bolt
- Holy Light
- Sanctuary
- Purification
- Radiant Nova
- Celestial Wrath

### Unholy Effects

- Dark Pact
- Shadow Bolt
- Shadowstep
- Life Drain
- Soul Harvest

### Other Effects

- Meteor
- Storm Judgment
- Light Orb
- Spark

The sample project also includes shared geometry and primitive helper classes:

- `PrayerVfxGeometry.java`
- `PrayerVfxPrimitives.java`

These are sample Java effect implementations, not separate built-in effects or a guarantee that every Minecraft rendering feature used by a mod is supported.

## Technology Stack

| Technology | Purpose |
| --- | --- |
| Java 21 | Application runtime and in-memory Java compilation |
| JavaFX 21.0.6 | Desktop interface, 3D scene, mesh rendering, and animation loop |
| JOML 1.10.5 | Matrix and vector mathematics |
| Gradle | Build and dependency management |
| JUnit 5 | Automated testing |

The repository includes an automated execution test that compiles and previews the sample Fireball effect.

## Current Limitations

- FX Studio does not launch Minecraft or Fabric.
- It does not load Minecraft worlds or access live game state.
- It does not reproduce Minecraft's complete rendering pipeline.
- Resource packs, Minecraft textures, shaders, light maps, overlays, entities, and the game's lighting engine are not reproduced.
- Compatibility stubs implement only the API surface currently needed by the preview environment.
- Captured UV coordinates and normals are not currently used to recreate Minecraft's textured or shader-based rendering.
- The execution timeout does not isolate untrusted code or restrict filesystem, network, or process access.
- The supplied launch and packaging scripts target Windows.

## License

See [LICENSE](LICENSE) for the license and its terms.
