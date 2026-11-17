# Minecraft FX Studio

Aplicativo desktop independente em **Java 21 + JavaFX + JOML** para criar, editar, compilar e visualizar efeitos visuais (VFX) do Minecraft Fabric em uma viewport 3D interativa, executando a matemática real do código Java sem precisar iniciar o jogo.

## O que foi resolvido nesta entrega

1. **Implementação Real de `VertexConsumer` (`RecordingVertexConsumer`)**:
   - Implementa `net.minecraft.client.render.VertexConsumer` (compatível com Minecraft 1.20.x e 1.21.x, Yarn `Vec3d` e Mojmap `Vec3`).
   - Intercepta `vertex()`, `color()`, `texture()`, `overlay()`, `light()` e `normal()`.
   - Converte automaticamente primitivas `QUADS` (padrão do Minecraft) em pares de triângulos preservando posições, cores RGBA, UVs e normais.
2. **Compilação Multi-Arquivo (`FxCodeCompiler`)**:
   - Compila simultaneamente todas as classes `.java` do projeto (`PrayerFireballVfx.java`, `PrayerVfxGeometry.java`, `PrayerVfxPrimitives.java`, etc.) sem conflitos de dependência entre elas, reportando arquivo, linha e coluna exatos em caso de erro.
3. **Execução Isolada com Proteção contra Loops Infinitos (`FxCodeExecutor`)**:
   - Carrega as classes compiladas em um `URLClassLoader` isolado e invoca o método `draw(Matrix4fc, VertexConsumer, Vec3/Vec3d, float, int)` em uma thread separada com timeout de segurança.
4. **Renderização 3D em Lote e Controles Completos (`FxMeshRenderer` + `FxViewport`)**:
   - Agrupa triângulos por cor/material para renderização fluida a 60+ FPS.
   - Controles de câmera (Orbit, Pan, Zoom, Perspectiva vs. Ortográfica, Reset), toggles de Grade, Eixos XYZ e Wireframe, além de edição em tempo real de `duration`, `frameStep` e `destination (x, y, z)`.

## Como Executar no Windows

1. Certifique-se de ter o **JDK 21** instalado.
2. Dê um duplo clique em `run-fx-studio.bat` (ou execute `gradle run` no terminal).
3. Para empacotar um executável `.exe` independente do Windows, execute `build-windows-app.bat`.
