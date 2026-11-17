package com.fxstudio.compiler;

import com.fxstudio.model.FxFrame;
import com.fxstudio.renderer.RecordingVertexConsumer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public final class FxCodeExecutor {

    private static final long EXECUTION_TIMEOUT_SECONDS = 8L;

    public record ExecutionResult(
            boolean successful,
            List<String> errors,
            List<FxFrame> frames,
            String resolvedClassName
    ) {
        public ExecutionResult {
            errors = List.copyOf(errors);
            frames = List.copyOf(frames);
        }
    }

    public ExecutionResult execute(
            Path compiledDirectory,
            String preferredClassName,
            List<Path> dependencies,
            double destX,
            double destY,
            double destZ,
            int duration,
            float frameStep
    ) {
        if (compiledDirectory == null || !Files.isDirectory(compiledDirectory)) {
            return failure("Diretório de classes compiladas não encontrado.");
        }
        if (duration <= 0 || frameStep <= 0 || !Float.isFinite(frameStep)) {
            return failure("Duração ou intervalo de frames inválido.");
        }

        ExecutorService sandboxPool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "FXStudio-VFX-Sandbox");
            t.setDaemon(true);
            return t;
        });

        Future<ExecutionResult> future = sandboxPool.submit(() -> runIsolated(
                compiledDirectory,
                preferredClassName,
                dependencies,
                destX, destY, destZ,
                duration,
                frameStep
        ));

        try {
            return future.get(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException timeout) {
            future.cancel(true);
            return failure("Tempo limite excedido (" + EXECUTION_TIMEOUT_SECONDS + "s). Verifique se há um loop infinito no código do efeito.");
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return failure("Execução interrompida.");
        } catch (ExecutionException execEx) {
            Throwable cause = execEx.getCause();
            return failure("Falha na execução isolada: " + (cause != null ? cause.getMessage() : execEx.getMessage()));
        } finally {
            sandboxPool.shutdownNow();
        }
    }

    private ExecutionResult runIsolated(
            Path compiledDirectory,
            String preferredClassName,
            List<Path> dependencies,
            double destX,
            double destY,
            double destZ,
            int duration,
            float frameStep
    ) {
        List<String> errors = new ArrayList<>();
        List<FxFrame> capturedFrames = new ArrayList<>();

        try {
            List<URL> urls = new ArrayList<>();
            urls.add(compiledDirectory.toAbsolutePath().toUri().toURL());
            if (dependencies != null) {
                for (Path dep : dependencies) {
                    if (dep != null && Files.exists(dep)) {
                        urls.add(dep.toAbsolutePath().toUri().toURL());
                    }
                }
            }

            try (URLClassLoader classLoader = new URLClassLoader(
                    urls.toArray(new URL[0]),
                    getClass().getClassLoader()
            )) {
                Class<?> effectClass = resolveEffectClass(compiledDirectory, classLoader, preferredClassName);
                if (effectClass == null) {
                    return failure("Nenhuma classe com o método static draw(Matrix4fc, VertexConsumer, [Vec3], [Vec3], float, int) foi encontrada.");
                }

                Method drawMethod = findDrawMethod(effectClass);
                if (drawMethod == null) {
                    return failure("Assinatura esperada não encontrada em " + effectClass.getName() +
                            ": public static void draw(Matrix4fc, VertexConsumer, [Vec3], [Vec3], float, int)");
                }

                drawMethod.setAccessible(true);
                Class<?>[] paramTypes = drawMethod.getParameterTypes();
                int paramCount = paramTypes.length;

                Object originArg = null;
                Object destinationArg = null;

                if (paramCount >= 5) {
                    Class<?> vecParamType = paramTypes[2];
                    if (vecParamType.equals(Vec3.class)) {
                        originArg = Vec3.ZERO;
                        destinationArg = new Vec3(destX, destY, destZ);
                    } else if (vecParamType.equals(Vec3d.class)) {
                        originArg = Vec3d.ZERO;
                        destinationArg = new Vec3d(destX, destY, destZ);
                    } else {
                        return failure("Tipo de vetor não suportado no parâmetro de draw(): " + vecParamType.getName());
                    }
                }

                RecordingVertexConsumer consumer = new RecordingVertexConsumer();
                Matrix4f identityMatrix = new Matrix4f().identity();

                float maxAge = duration + 6.0f;
                int maxSteps = 2000;
                int stepCount = 0;

                for (float age = 0.0f; age <= maxAge + 0.0001f && stepCount < maxSteps; age += frameStep, stepCount++) {
                    if (Thread.currentThread().isInterrupted()) {
                        return failure("Execução cancelada.");
                    }

                    consumer.clear();
                    try {
                        if (paramCount == 6) {
                            drawMethod.invoke(
                                    null,
                                    (Matrix4fc) identityMatrix,
                                    (VertexConsumer) consumer,
                                    originArg,
                                    destinationArg,
                                    age,
                                    duration
                            );
                        } else if (paramCount == 5) {
                            drawMethod.invoke(
                                    null,
                                    (Matrix4fc) identityMatrix,
                                    (VertexConsumer) consumer,
                                    destinationArg,
                                    age,
                                    duration
                            );
                        } else {
                            drawMethod.invoke(
                                    null,
                                    (Matrix4fc) identityMatrix,
                                    (VertexConsumer) consumer,
                                    age,
                                    duration
                            );
                        }
                        consumer.finish();
                        capturedFrames.add(new FxFrame(age, duration, consumer.getTriangles()));
                    } catch (InvocationTargetException invEx) {
                        Throwable cause = invEx.getCause() != null ? invEx.getCause() : invEx;
                        String location = extractStackLocation(cause, effectClass.getPackageName());
                        errors.add(String.format(
                                "Erro de execução em age=%.2f: %s: %s %s",
                                age,
                                cause.getClass().getSimpleName(),
                                cause.getMessage(),
                                location
                        ));
                        return new ExecutionResult(false, errors, capturedFrames, effectClass.getName());
                    }
                }

                return new ExecutionResult(true, errors, capturedFrames, effectClass.getName());
            }

        } catch (Exception exception) {
            errors.add(exception.getClass().getSimpleName() + ": " + exception.getMessage());
            return new ExecutionResult(false, errors, capturedFrames, preferredClassName);
        }
    }

    private Class<?> resolveEffectClass(Path compiledDirectory, ClassLoader loader, String preferredClassName) throws Exception {
        List<String> classNames = new ArrayList<>();
        try (var stream = Files.walk(compiledDirectory)) {
            stream.filter(Files::isRegularFile)
                  .filter(p -> p.toString().endsWith(".class"))
                  .forEach(p -> {
                      String rel = compiledDirectory.relativize(p).toString();
                      String fqcn = rel.substring(0, rel.length() - 6).replace('/', '.').replace('\\', '.');
                      classNames.add(fqcn);
                  });
        }

        if (preferredClassName != null) {
            for (String fqcn : classNames) {
                if (fqcn.equals(preferredClassName) || fqcn.endsWith("." + preferredClassName)) {
                    Class<?> candidate = Class.forName(fqcn, false, loader);
                    if (findDrawMethod(candidate) != null) return candidate;
                }
            }
        }

        for (String fqcn : classNames) {
            if (fqcn.contains("$")) continue;
            Class<?> candidate = Class.forName(fqcn, false, loader);
            if (findDrawMethod(candidate) != null) return candidate;
        }

        return null;
    }

    private Method findDrawMethod(Class<?> effectClass) {
        for (Method method : effectClass.getDeclaredMethods()) {
            if (!method.getName().equals("draw")) continue;
            if (!Modifier.isStatic(method.getModifiers())) continue;

            Class<?>[] params = method.getParameterTypes();
            if (params.length == 6) {
                boolean matrixOk = Matrix4fc.class.isAssignableFrom(params[0]);
                boolean consumerOk = VertexConsumer.class.isAssignableFrom(params[1]);
                boolean handOk = Vec3.class.isAssignableFrom(params[2]) || Vec3d.class.isAssignableFrom(params[2]);
                boolean destOk = Vec3.class.isAssignableFrom(params[3]) || Vec3d.class.isAssignableFrom(params[3]);
                boolean ageOk = params[4] == float.class || params[4] == double.class;
                boolean durOk = params[5] == int.class;
                if (matrixOk && consumerOk && handOk && destOk && ageOk && durOk) return method;
            } else if (params.length == 5) {
                boolean matrixOk = Matrix4fc.class.isAssignableFrom(params[0]);
                boolean consumerOk = VertexConsumer.class.isAssignableFrom(params[1]);
                boolean vecOk = Vec3.class.isAssignableFrom(params[2]) || Vec3d.class.isAssignableFrom(params[2]);
                boolean ageOk = params[3] == float.class || params[3] == double.class;
                boolean durOk = params[4] == int.class;
                if (matrixOk && consumerOk && vecOk && ageOk && durOk) return method;
            } else if (params.length == 4) {
                boolean matrixOk = Matrix4fc.class.isAssignableFrom(params[0]);
                boolean consumerOk = VertexConsumer.class.isAssignableFrom(params[1]);
                boolean ageOk = params[2] == float.class || params[2] == double.class;
                boolean durOk = params[3] == int.class;
                if (matrixOk && consumerOk && ageOk && durOk) return method;
            }
        }
        return null;
    }

    private String extractStackLocation(Throwable cause, String pkg) {
        for (StackTraceElement el : cause.getStackTrace()) {
            if (pkg != null && !pkg.isEmpty() && el.getClassName().startsWith(pkg)) {
                return "(" + el.getFileName() + ":" + el.getLineNumber() + ")";
            }
        }
        return "";
    }

    private ExecutionResult failure(String message) {
        return new ExecutionResult(false, List.of(message), List.of(), null);
    }
}
