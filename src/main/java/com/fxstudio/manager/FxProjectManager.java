package com.fxstudio.manager;

import com.fxstudio.model.FxProject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class FxProjectManager {

    private static final String CONFIG_FILE = "fxstudio.properties";

    private FxProjectManager() {}

    public static FxProject createProject(String name, Path directory) throws IOException {
        Files.createDirectories(directory);
        Files.createDirectories(directory.resolve("src"));
        Files.createDirectories(directory.resolve("libs"));

        FxProject project = new FxProject(name, directory);
        saveProject(project);
        return project;
    }

    public static FxProject openProject(Path directory) throws IOException {
        Path config = directory.resolve(CONFIG_FILE);
        if (!Files.exists(config)) {
            throw new IOException("Projeto FX Studio não encontrado: " + config);
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(config)) {
            properties.load(input);
        }

        String name = properties.getProperty("name", "SimpleSkills VFX");
        FxProject project = new FxProject(name, directory);

        project.setDuration(Integer.parseInt(properties.getProperty("duration", "40")));
        project.setFrameStep(Float.parseFloat(properties.getProperty("frameStep", "0.5")));

        double x = Double.parseDouble(properties.getProperty("destinationX", "0.0"));
        double y = Double.parseDouble(properties.getProperty("destinationY", "0.5"));
        double z = Double.parseDouble(properties.getProperty("destinationZ", "6.0"));
        project.setDestination(x, y, z);

        Path sourceDirectory = directory.resolve("src");
        if (Files.exists(sourceDirectory)) {
            try (var paths = Files.walk(sourceDirectory)) {
                paths.filter(Files::isRegularFile)
                     .filter(path -> path.toString().endsWith(".java"))
                     .sorted()
                     .forEach(project::addSourceFile);
            }
        }

        String mainSource = properties.getProperty("mainSource");
        if (mainSource != null && !mainSource.isBlank()) {
            Path source = directory.resolve(mainSource).normalize();
            if (source.startsWith(directory.normalize()) && Files.exists(source)) {
                project.setMainSourceFile(source);
            }
        }
        if (project.getMainSourceFile() == null && !project.getSourceFiles().isEmpty()) {
            project.setMainSourceFile(project.getSourceFiles().get(0));
        }

        Path dependencyDirectory = directory.resolve("libs");
        if (Files.exists(dependencyDirectory)) {
            try (var paths = Files.list(dependencyDirectory)) {
                paths.filter(Files::isRegularFile)
                     .filter(path -> path.toString().endsWith(".jar"))
                     .forEach(project::addDependency);
            }
        }

        return project;
    }

    public static void saveProject(FxProject project) throws IOException {
        Path directory = project.getProjectDirectory();
        Files.createDirectories(directory);

        Properties properties = new Properties();
        properties.setProperty("name", project.getName());
        properties.setProperty("duration", String.valueOf(project.getDuration()));
        properties.setProperty("frameStep", String.valueOf(project.getFrameStep()));
        properties.setProperty("destinationX", String.valueOf(project.getDestinationX()));
        properties.setProperty("destinationY", String.valueOf(project.getDestinationY()));
        properties.setProperty("destinationZ", String.valueOf(project.getDestinationZ()));

        Path mainSource = project.getMainSourceFile();
        if (mainSource != null) {
            Path normalizedDir = directory.toAbsolutePath().normalize();
            Path normalizedSrc = mainSource.toAbsolutePath().normalize();
            if (normalizedSrc.startsWith(normalizedDir)) {
                properties.setProperty("mainSource", normalizedDir.relativize(normalizedSrc).toString());
            }
        }

        try (OutputStream output = Files.newOutputStream(directory.resolve(CONFIG_FILE))) {
            properties.store(output, "FX Studio Project Configuration");
        }
    }
}
