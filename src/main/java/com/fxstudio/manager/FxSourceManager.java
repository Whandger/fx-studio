package com.fxstudio.manager;

import com.fxstudio.model.FxProject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class FxSourceManager {

    private FxSourceManager() {}

    public static Path importSource(FxProject project, Path sourceFile) throws IOException {
        if (project == null || sourceFile == null) {
            throw new IllegalArgumentException("Projeto ou arquivo inválido.");
        }
        if (!Files.isRegularFile(sourceFile) || !sourceFile.toString().endsWith(".java")) {
            throw new IOException("Selecione um arquivo .java válido.");
        }

        Path sourceDirectory = project.getProjectDirectory().resolve("src");
        Files.createDirectories(sourceDirectory);

        Path destination = sourceDirectory.resolve(sourceFile.getFileName());
        if (!Files.exists(destination) || !Files.isSameFile(sourceFile, destination)) {
            Files.copy(sourceFile, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        project.addSourceFile(destination);
        return destination;
    }

    public static String readSource(Path sourceFile) throws IOException {
        if (sourceFile == null || !Files.isRegularFile(sourceFile)) {
            throw new IOException("Arquivo Java não encontrado: " + sourceFile);
        }
        return Files.readString(sourceFile, StandardCharsets.UTF_8);
    }

    public static void saveSource(FxProject project, Path sourceFile, String content) throws IOException {
        if (project == null || sourceFile == null) {
            throw new IllegalArgumentException("Projeto ou arquivo inválido.");
        }
        Files.createDirectories(sourceFile.getParent());
        Files.writeString(sourceFile, content == null ? "" : content, StandardCharsets.UTF_8);
        project.addSourceFile(sourceFile);
    }

    public static List<Path> listSources(FxProject project) throws IOException {
        if (project == null) return List.of();
        Path sourceDirectory = project.getProjectDirectory().resolve("src");
        if (!Files.exists(sourceDirectory)) return List.of();

        List<Path> result = new ArrayList<>();
        try (var paths = Files.walk(sourceDirectory)) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".java"))
                 .sorted()
                 .forEach(result::add);
        }
        return result;
    }
}
