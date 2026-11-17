package com.fxstudio.compiler;

import javax.tools.*;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class FxCodeCompiler {

    public record CompilationResult(
            boolean successful,
            Path outputDirectory,
            List<String> errors,
            List<String> warnings
    ) {
        public CompilationResult {
            errors = List.copyOf(errors);
            warnings = List.copyOf(warnings);
        }
    }

    private static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();

    public CompilationResult compileProjectSources(
            Map<String, String> sourceMap,
            List<Path> dependencies
    ) {
        if (COMPILER == null) {
            return failure("JDK não encontrado. Execute o FX Studio com um JDK 21 completo (não apenas JRE).");
        }
        if (sourceMap == null || sourceMap.isEmpty()) {
            return failure("Nenhum arquivo .java fornecido para compilação.");
        }

        Path outputDirectory = null;
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        try {
            outputDirectory = Files.createTempDirectory("fxstudio-classes-");

            List<JavaFileObject> compilationUnits = new ArrayList<>();
            for (Map.Entry<String, String> entry : sourceMap.entrySet()) {
                String fileName = entry.getKey();
                String code = entry.getValue();
                if (code != null && !code.isBlank()) {
                    compilationUnits.add(new InMemoryJavaSource(fileName, code));
                }
            }

            if (compilationUnits.isEmpty()) {
                deleteDirectory(outputDirectory);
                return failure("Os arquivos Java estão vazios.");
            }

            List<String> options = new ArrayList<>();
            options.add("-d");
            options.add(outputDirectory.toString());
            options.add("-proc:none");
            options.add("-encoding");
            options.add("UTF-8");
            options.add("-g");
            options.add("-classpath");
            options.add(buildClasspath(dependencies));

            boolean success;
            try (StandardJavaFileManager manager = COMPILER.getStandardFileManager(
                    diagnostics,
                    Locale.ROOT,
                    StandardCharsets.UTF_8
            )) {
                success = COMPILER.getTask(
                        null,
                        manager,
                        diagnostics,
                        options,
                        null,
                        compilationUnits
                ).call();
            }

            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                String message = formatDiagnostic(diagnostic);
                if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                    errors.add(message);
                } else if (diagnostic.getKind() == Diagnostic.Kind.WARNING
                        || diagnostic.getKind() == Diagnostic.Kind.MANDATORY_WARNING) {
                    warnings.add(message);
                }
            }

            if (!success) {
                deleteDirectory(outputDirectory);
                return new CompilationResult(false, null, errors, warnings);
            }

            return new CompilationResult(true, outputDirectory, errors, warnings);

        } catch (Exception exception) {
            if (outputDirectory != null) {
                deleteDirectory(outputDirectory);
            }
            return failure("Erro interno do compilador: " + exception.getMessage());
        }
    }

    public CompilationResult compile(String className, String sourceCode, List<Path> dependencies) {
        return compileProjectSources(Map.of(className.replace('.', '/') + ".java", sourceCode), dependencies);
    }

    private String buildClasspath(List<Path> dependencies) {
        List<String> entries = new ArrayList<>();
        String currentClasspath = System.getProperty("java.class.path");
        if (currentClasspath != null && !currentClasspath.isBlank()) {
            entries.add(currentClasspath);
        }
        if (dependencies != null) {
            for (Path dependency : dependencies) {
                if (dependency != null && Files.exists(dependency)) {
                    entries.add(dependency.toAbsolutePath().toString());
                }
            }
        }
        return String.join(System.getProperty("path.separator"), entries);
    }

    private String formatDiagnostic(Diagnostic<? extends JavaFileObject> diagnostic) {
        String fileName = "Source.java";
        if (diagnostic.getSource() != null) {
            String uriPath = diagnostic.getSource().toUri().getPath();
            if (uriPath != null && !uriPath.isEmpty()) {
                int slash = uriPath.lastIndexOf('/');
                fileName = slash >= 0 ? uriPath.substring(slash + 1) : uriPath;
            }
        }
        return String.format(
                Locale.ROOT,
                "%s [Linha %d, Col %d]: %s",
                fileName,
                diagnostic.getLineNumber(),
                diagnostic.getColumnNumber(),
                diagnostic.getMessage(Locale.ROOT)
        );
    }

    private CompilationResult failure(String message) {
        return new CompilationResult(false, null, List.of(message), List.of());
    }

    public static void deleteDirectory(Path directory) {
        try {
            if (directory == null || !Files.exists(directory)) return;
            try (var paths = Files.walk(directory)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
            }
        } catch (IOException ignored) {
        }
    }

    private static final class InMemoryJavaSource extends SimpleJavaFileObject {
        private final String source;

        InMemoryJavaSource(String filePathOrName, String source) {
            super(createUri(filePathOrName, source), Kind.SOURCE);
            this.source = source;
        }

        private static URI createUri(String fileName, String source) {
            String cleanName = fileName.endsWith(".java") ? fileName : fileName + ".java";
            int slash = cleanName.lastIndexOf('/');
            if (slash >= 0) cleanName = cleanName.substring(slash + 1);
            int backslash = cleanName.lastIndexOf('\\');
            if (backslash >= 0) cleanName = cleanName.substring(backslash + 1);

            String pkgPath = "";
            for (String line : source.split("\\R")) {
                String trimmed = line.trim();
                if (trimmed.startsWith("package ") && trimmed.endsWith(";")) {
                    String pkg = trimmed.substring(8, trimmed.length() - 1).trim();
                    pkgPath = pkg.replace('.', '/') + "/";
                    break;
                }
            }
            return URI.create("string:///" + pkgPath + cleanName);
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }
}
