package com.fxstudio.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FxProject {

    private String name;
    private Path projectDirectory;
    private Path mainSourceFile;
    private final List<Path> sourceFiles = new ArrayList<>();
    private final List<Path> dependencies = new ArrayList<>();

    private int duration = 40;
    private float frameStep = 0.5f;
    private double destinationX = 0.0;
    private double destinationY = 0.5;
    private double destinationZ = 6.0;

    public FxProject(String name, Path projectDirectory) {
        this.name = name;
        this.projectDirectory = projectDirectory;
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Nome do projeto vazio.");
        this.name = name;
    }

    public Path getProjectDirectory() { return projectDirectory; }
    public void setProjectDirectory(Path directory) {
        if (directory == null) throw new IllegalArgumentException("Diretório inválido.");
        this.projectDirectory = directory;
    }

    public Path getMainSourceFile() { return mainSourceFile; }
    public void setMainSourceFile(Path sourceFile) {
        if (sourceFile != null && !Files.exists(sourceFile)) {
            throw new IllegalArgumentException("Arquivo principal não encontrado: " + sourceFile);
        }
        this.mainSourceFile = sourceFile;
        if (sourceFile != null && !sourceFiles.contains(sourceFile)) {
            sourceFiles.add(sourceFile);
        }
    }

    public void addSourceFile(Path sourceFile) {
        if (sourceFile != null && Files.exists(sourceFile) && sourceFile.toString().endsWith(".java")) {
            if (!sourceFiles.contains(sourceFile)) sourceFiles.add(sourceFile);
        }
    }

    public void removeSourceFile(Path sourceFile) {
        sourceFiles.remove(sourceFile);
        if (sourceFile != null && sourceFile.equals(mainSourceFile)) mainSourceFile = null;
    }

    public List<Path> getSourceFiles() { return Collections.unmodifiableList(sourceFiles); }

    public void addDependency(Path dependency) {
        if (dependency != null && Files.exists(dependency) && !dependencies.contains(dependency)) {
            dependencies.add(dependency);
        }
    }

    public void removeDependency(Path dependency) { dependencies.remove(dependency); }
    public List<Path> getDependencies() { return Collections.unmodifiableList(dependencies); }

    public int getDuration() { return duration; }
    public void setDuration(int duration) {
        if (duration < 1 || duration > 600) throw new IllegalArgumentException("Duração deve estar entre 1 e 600 ticks.");
        this.duration = duration;
    }

    public float getFrameStep() { return frameStep; }
    public void setFrameStep(float frameStep) {
        if (!Float.isFinite(frameStep) || frameStep <= 0.01f || frameStep > 10f) {
            throw new IllegalArgumentException("Intervalo de frames inválido.");
        }
        this.frameStep = frameStep;
    }

    public double getDestinationX() { return destinationX; }
    public void setDestinationX(double destinationX) { this.destinationX = destinationX; }
    public double getDestinationY() { return destinationY; }
    public void setDestinationY(double destinationY) { this.destinationY = destinationY; }
    public double getDestinationZ() { return destinationZ; }
    public void setDestinationZ(double destinationZ) { this.destinationZ = destinationZ; }

    public void setDestination(double x, double y, double z) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            throw new IllegalArgumentException("Coordenadas inválidas.");
        }
        this.destinationX = x;
        this.destinationY = y;
        this.destinationZ = z;
    }
}
