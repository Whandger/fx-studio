package com.fxstudio;

import com.fxstudio.compiler.FxCodeCompiler;
import com.fxstudio.compiler.FxCodeExecutor;
import com.fxstudio.manager.FxProjectManager;
import com.fxstudio.model.FxProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FxStudioExecutionTest {

    @Test
    void compilesAndExecutesPrayerFireballVfx(@TempDir Path tempDir) throws Exception {
        FxProject project = FxProjectManager.createProject("TestFireball", tempDir);
        SampleEffectBootstrap.populateDefaultSamples(project);

        Map<String, String> sources = new LinkedHashMap<>();
        for (Path src : project.getSourceFiles()) {
            sources.put(src.getFileName().toString(), Files.readString(src, StandardCharsets.UTF_8));
        }

        FxCodeCompiler compiler = new FxCodeCompiler();
        FxCodeCompiler.CompilationResult comp = compiler.compileProjectSources(sources, List.of());
        assertTrue(comp.successful(), "Erros de compilação: " + comp.errors());

        FxCodeExecutor executor = new FxCodeExecutor();
        FxCodeExecutor.ExecutionResult exec = executor.execute(
                comp.outputDirectory(),
                "PrayerFireballVfx",
                List.of(),
                0.0, 0.5, 6.0,
                20,
                1.0f
        );

        assertTrue(exec.successful(), "Erros de execução: " + exec.errors());
        assertFalse(exec.frames().isEmpty(), "Deveria capturar frames da Fireball");
        assertTrue(exec.frames().get(5).getTriangleCount() > 0, "Frame 5 deve conter geometria 3D real");

        FxCodeCompiler.deleteDirectory(comp.outputDirectory());
    }
}
