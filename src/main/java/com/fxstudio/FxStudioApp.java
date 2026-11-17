package com.fxstudio;

import com.fxstudio.compiler.FxCodeCompiler;
import com.fxstudio.compiler.FxCodeExecutor;
import com.fxstudio.manager.FxAnimationController;
import com.fxstudio.manager.FxProjectManager;
import com.fxstudio.manager.FxSourceManager;
import com.fxstudio.model.FxAnimation;
import com.fxstudio.model.FxFrame;
import com.fxstudio.model.FxProject;
import com.fxstudio.model.FxSettings;
import com.fxstudio.renderer.FxMeshRenderer;
import com.fxstudio.view.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FxStudioApp extends Application {

    private final FxSettings settings = new FxSettings();
    private final FxAnimation animation = new FxAnimation();
    private final FxAnimationController animationController = new FxAnimationController(animation);
    private final FxCodeCompiler compiler = new FxCodeCompiler();
    private final FxCodeExecutor executor = new FxCodeExecutor();

    private FxProject currentProject;
    private Path lastCompiledDir;

    private FxProjectExplorer projectExplorer;
    private FxEditor codeEditor;
    private FxViewport viewport;
    private FxMeshRenderer meshRenderer;
    private FxTimeline timeline;
    private FxConsole console;

    private Button playPauseButton;
    private Label statusLabel;

    private TextField durationField;
    private TextField frameStepField;
    private TextField destXField;
    private TextField destYField;
    private TextField destZField;
    private ComboBox<String> speedBox;

    @Override
    public void start(Stage stage) {
        viewport = new FxViewport();
        meshRenderer = new FxMeshRenderer(viewport);
        projectExplorer = new FxProjectExplorer();
        codeEditor = new FxEditor();
        timeline = new FxTimeline();
        console = new FxConsole();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #111318;");
        root.setTop(createToolbar(stage));
        root.setCenter(createMainWorkspace());
        root.setBottom(createStatusBar());

        setupCallbacks();
        initializeDefaultWorkspace();

        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (animationController.update()) {
                    renderCurrentFrame();
                    timeline.updateSelection();
                }
            }
        };
        loop.start();

        Scene scene = new Scene(root, 1540, 920);
        stage.setTitle("Minecraft FX Studio - Real Java VFX Engine");
        stage.setMinWidth(1150);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> FxCodeCompiler.deleteDirectory(lastCompiledDir));
        stage.show();

        compileAndRenderEffect();
    }

    private HBox createToolbar(Stage stage) {
        Label logo = new Label("MINECRAFT FX STUDIO");
        logo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ff8b42;");

        Button importJavaBtn = createButton("Import .java", "#292e3a");
        Button saveBtn = createButton("Save File", "#292e3a");
        Button compileRenderBtn = createButton("Compile & Render", "#2563eb");
        Button addJarBtn = createButton("Add JAR Dep", "#292e3a");

        CheckBox gridCheck = new CheckBox("Grid");
        gridCheck.setSelected(true);
        gridCheck.setStyle("-fx-text-fill: #d1d5db;");
        gridCheck.selectedProperty().addListener((o, a, b) -> viewport.setGridVisible(b));

        CheckBox axesCheck = new CheckBox("Axes");
        axesCheck.setSelected(true);
        axesCheck.setStyle("-fx-text-fill: #d1d5db;");
        axesCheck.selectedProperty().addListener((o, a, b) -> viewport.setAxesVisible(b));

        CheckBox wireCheck = new CheckBox("Wireframe");
        wireCheck.setSelected(false);
        wireCheck.setStyle("-fx-text-fill: #d1d5db;");
        wireCheck.selectedProperty().addListener((o, a, b) -> viewport.setWireframeMode(b));

        ToggleButton orthoToggle = new ToggleButton("Ortho");
        orthoToggle.setStyle("-fx-background-color: #292e3a; -fx-text-fill: #e5e7eb; -fx-background-radius: 4;");
        orthoToggle.selectedProperty().addListener((o, a, b) -> {
            viewport.setOrthographic(b);
            orthoToggle.setText(b ? "Perspective" : "Ortho");
        });

        Button resetCamBtn = createButton("Reset Camera", "#292e3a");
        resetCamBtn.setOnAction(e -> viewport.resetCamera());

        importJavaBtn.setOnAction(e -> handleImportJava(stage));
        saveBtn.setOnAction(e -> handleSaveCurrentFile());
        compileRenderBtn.setOnAction(e -> compileAndRenderEffect());
        addJarBtn.setOnAction(e -> handleAddDependency(stage));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(
                10,
                logo,
                new Separator(Orientation.VERTICAL),
                importJavaBtn,
                saveBtn,
                addJarBtn,
                compileRenderBtn,
                spacer,
                gridCheck,
                axesCheck,
                wireCheck,
                orthoToggle,
                resetCamBtn
        );
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 16, 10, 16));
        toolbar.setStyle("-fx-background-color: #191c23; -fx-border-color: #292e3a; -fx-border-width: 0 0 1 0;");
        return toolbar;
    }

    private SplitPane createMainWorkspace() {
        projectExplorer.setPrefWidth(210);
        projectExplorer.setMinWidth(160);

        SplitPane leftSplit = new SplitPane(projectExplorer, codeEditor);
        leftSplit.setDividerPositions(0.28);

        VBox rightSide = createViewportAndControlsPanel();

        SplitPane mainSplit = new SplitPane(leftSplit, rightSide);
        mainSplit.setDividerPositions(0.46);
        return mainSplit;
    }

    private VBox createViewportAndControlsPanel() {
        VBox.setVgrow(viewport, Priority.ALWAYS);
        viewport.setMinHeight(320);

        HBox parametersBar = createEffectParametersBar();
        HBox playbackBar = createPlaybackBar();

        console.setPrefHeight(165);
        console.setMinHeight(110);

        VBox panel = new VBox(
                viewport,
                parametersBar,
                playbackBar,
                timeline,
                console
        );
        panel.setStyle("-fx-background-color: #14171f;");
        return panel;
    }

    private HBox createEffectParametersBar() {
        durationField = createCompactField("40", 52);
        frameStepField = createCompactField("0.5", 52);
        destXField = createCompactField("0.0", 55);
        destYField = createCompactField("0.5", 55);
        destZField = createCompactField("6.0", 55);

        Button applyParamsBtn = createButton("Apply & Re-Render", "#16a34a");
        applyParamsBtn.setOnAction(e -> compileAndRenderEffect());

        HBox bar = new HBox(
                8,
                createParamLabel("duration:"), durationField,
                createParamLabel("step:"), frameStepField,
                createParamLabel("dest.x:"), destXField,
                createParamLabel("dest.y:"), destYField,
                createParamLabel("dest.z:"), destZField,
                applyParamsBtn
        );
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 12, 8, 12));
        bar.setStyle("-fx-background-color: #1a1e29; -fx-border-color: #292e3a; -fx-border-width: 1 0 1 0;");
        return bar;
    }

    private HBox createPlaybackBar() {
        playPauseButton = createButton("Pause", "#292e3a");
        Button resetBtn = createButton("Reset", "#292e3a");
        Button prevFrameBtn = createButton("< Frame", "#292e3a");
        Button nextFrameBtn = createButton("Frame >", "#292e3a");

        speedBox = new ComboBox<>();
        speedBox.getItems().addAll("0.25x", "0.5x", "1.0x", "1.5x", "2.0x", "4.0x");
        speedBox.setValue("1.0x");
        speedBox.setStyle("-fx-background-color: #292e3a; -fx-text-fill: white;");
        speedBox.setOnAction(e -> {
            String raw = speedBox.getValue().replace("x", "");
            animationController.setPlaybackSpeed(Double.parseDouble(raw));
        });

        playPauseButton.setOnAction(e -> {
            if (animationController.isPlaying()) {
                animationController.pause();
                playPauseButton.setText("Play");
            } else {
                animationController.play();
                playPauseButton.setText("Pause");
            }
        });

        resetBtn.setOnAction(e -> {
            animationController.stop();
            playPauseButton.setText("Play");
            renderCurrentFrame();
            timeline.updateSelection();
        });

        prevFrameBtn.setOnAction(e -> {
            animationController.pause();
            playPauseButton.setText("Play");
            animationController.previousFrame();
            renderCurrentFrame();
            timeline.updateSelection();
        });

        nextFrameBtn.setOnAction(e -> {
            animationController.pause();
            playPauseButton.setText("Play");
            animationController.nextFrame();
            renderCurrentFrame();
            timeline.updateSelection();
        });

        Label hint = new Label("Mouse Esq: Orbitar | Mouse Dir: Pan | Scroll: Zoom");
        hint.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(
                8,
                playPauseButton,
                resetBtn,
                prevFrameBtn,
                nextFrameBtn,
                createParamLabel("Speed:"),
                speedBox,
                spacer,
                hint
        );
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(7, 12, 7, 12));
        bar.setStyle("-fx-background-color: #161922;");
        return bar;
    }

    private HBox createStatusBar() {
        statusLabel = new Label("Pronto.");
        statusLabel.setStyle("-fx-text-fill: #9da6b8; -fx-font-size: 11px;");
        HBox bar = new HBox(statusLabel);
        bar.setPadding(new Insets(6, 14, 6, 14));
        bar.setStyle("-fx-background-color: #191c23; -fx-border-color: #292e3a; -fx-border-width: 1 0 0 0;");
        return bar;
    }

    private void setupCallbacks() {
        projectExplorer.setOnFileSelected(path -> {
            try {
                saveActiveEditorSilently();
                String content = FxSourceManager.readSource(path);
                codeEditor.openFile(path, content);
                currentProject.setMainSourceFile(path);
                setStatus("Arquivo aberto: " + path.getFileName());
            } catch (IOException ex) {
                console.error("Erro ao abrir arquivo: " + ex.getMessage());
            }
        });

        projectExplorer.setOnFileDeleted(this::handleDeleteProjectFile);

        timeline.setOnFrameSelected(frameIndex -> {
            animationController.pause();
            playPauseButton.setText("Play");
            renderCurrentFrame();
        });
    }

    private void initializeDefaultWorkspace() {
        try {
            Path workspaceDir = Path.of(System.getProperty("user.dir"), "sample-project");
            if (Files.exists(workspaceDir.resolve("fxstudio.properties"))) {
                currentProject = FxProjectManager.openProject(workspaceDir);
            } else {
                currentProject = FxProjectManager.createProject("SimpleSkills-Fireball", workspaceDir);
                SampleEffectBootstrap.populateDefaultSamples(currentProject);
                FxProjectManager.saveProject(currentProject);
            }

            syncFieldsFromProject();
            projectExplorer.loadProject(currentProject.getProjectDirectory());

            Path mainFile = currentProject.getMainSourceFile();
            if (mainFile != null && Files.exists(mainFile)) {
                codeEditor.openFile(mainFile, FxSourceManager.readSource(mainFile));
            }
        } catch (IOException ex) {
            console.error("Erro ao inicializar workspace padrão: " + ex.getMessage());
        }
    }

    private void compileAndRenderEffect() {
        saveActiveEditorSilently();

        int duration;
        float frameStep;
        double dx, dy, dz;

        try {
            duration = Integer.parseInt(durationField.getText().trim());
            frameStep = Float.parseFloat(frameStepField.getText().trim().replace(',', '.'));
            dx = Double.parseDouble(destXField.getText().trim().replace(',', '.'));
            dy = Double.parseDouble(destYField.getText().trim().replace(',', '.'));
            dz = Double.parseDouble(destZField.getText().trim().replace(',', '.'));

            currentProject.setDuration(duration);
            currentProject.setFrameStep(frameStep);
            currentProject.setDestination(dx, dy, dz);
            viewport.setTargetPosition(dx, dy, dz);
            FxProjectManager.saveProject(currentProject);
        } catch (Exception ex) {
            console.error("Parâmetros inválidos: " + ex.getMessage());
            return;
        }

        try {
            Map<String, String> sources = new LinkedHashMap<>();
            for (Path file : FxSourceManager.listSources(currentProject)) {
                String content = file.equals(codeEditor.getCurrentFile())
                        ? codeEditor.getCode()
                        : Files.readString(file, StandardCharsets.UTF_8);
                sources.put(file.getFileName().toString(), content);
            }

            if (sources.isEmpty() && !codeEditor.getCode().isBlank()) {
                sources.put("EffectVfx.java", codeEditor.getCode());
            }

            console.info("Compilando " + sources.size() + " arquivo(s) Java...");
            FxCodeCompiler.CompilationResult compResult =
                    compiler.compileProjectSources(sources, currentProject.getDependencies());

            for (String warn : compResult.warnings()) {
                console.warning(warn);
            }

            if (!compResult.successful()) {
                for (String err : compResult.errors()) {
                    console.error(err);
                }
                setStatus("Falha na compilação (" + compResult.errors().size() + " erro(s)).");
                return;
            }

            FxCodeCompiler.deleteDirectory(lastCompiledDir);
            lastCompiledDir = compResult.outputDirectory();

            String preferredClass = null;
            if (codeEditor.getCurrentFile() != null) {
                String name = codeEditor.getCurrentFile().getFileName().toString();
                preferredClass = name.substring(0, name.length() - 5);
            }

            FxCodeExecutor.ExecutionResult execResult = executor.execute(
                    lastCompiledDir,
                    preferredClass,
                    currentProject.getDependencies(),
                    dx, dy, dz,
                    duration,
                    frameStep
            );

            if (!execResult.successful()) {
                for (String err : execResult.errors()) {
                    console.error(err);
                }
                setStatus("Erro durante a execução do efeito.");
                return;
            }

            animation.clear();
            animation.addFrames(execResult.frames());
            timeline.setAnimation(animation);

            int totalTris = execResult.frames().stream().mapToInt(FxFrame::getTriangleCount).sum();
            console.success(String.format(
                    Locale.ROOT,
                    "Renderizado %s: %d frames gerados (%d triângulos totais).",
                    execResult.resolvedClassName(),
                    execResult.frames().size(),
                    totalTris
            ));

            renderCurrentFrame();
            if (settings.isAutoPlayAfterRender() && !animation.isEmpty()) {
                animationController.play();
                playPauseButton.setText("Pause");
            }
            setStatus("Executando: " + execResult.resolvedClassName());

        } catch (Exception ex) {
            console.error("Erro inesperado: " + ex.getMessage());
        }
    }

    private void renderCurrentFrame() {
        FxFrame frame = animation.getCurrentFrame();
        if (frame == null) {
            meshRenderer.clear();
            return;
        }
        meshRenderer.render(frame.getTriangles());
    }

    private void handleImportJava(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Importar arquivos .java do SimpleSkills");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Java (*.java)", "*.java"));
        List<File> selectedFiles = chooser.showOpenMultipleDialog(stage);
        if (selectedFiles == null || selectedFiles.isEmpty()) return;

        try {
            Path lastImported = null;
            for (File file : selectedFiles) {
                lastImported = FxSourceManager.importSource(currentProject, file.toPath());
                console.info("Importado: " + lastImported.getFileName());
            }
            projectExplorer.loadProject(currentProject.getProjectDirectory());
            if (lastImported != null) {
                codeEditor.openFile(lastImported, FxSourceManager.readSource(lastImported));
                currentProject.setMainSourceFile(lastImported);
            }
            FxProjectManager.saveProject(currentProject);
        } catch (IOException ex) {
            console.error("Erro ao importar .java: " + ex.getMessage());
        }
    }

    private void handleAddDependency(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Adicionar dependência .jar ao Classpath");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos JAR (*.jar)", "*.jar"));
        File jar = chooser.showOpenDialog(stage);
        if (jar == null) return;

        try {
            Path libsDir = currentProject.getProjectDirectory().resolve("libs");
            Files.createDirectories(libsDir);
            Path dest = libsDir.resolve(jar.getName());
            Files.copy(jar.toPath(), dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            currentProject.addDependency(dest);
            projectExplorer.loadProject(currentProject.getProjectDirectory());
            console.success("Dependência adicionada: " + jar.getName());
        } catch (IOException ex) {
            console.error("Erro ao adicionar JAR: " + ex.getMessage());
        }
    }

    private void handleDeleteProjectFile(Path target) {
        if (currentProject == null || target == null) return;

        try {
            Path normalizedTarget = target.toAbsolutePath().normalize();
            Path projectDir = currentProject.getProjectDirectory().toAbsolutePath().normalize();
            if (!normalizedTarget.startsWith(projectDir)) {
                console.error("Arquivo fora do diretório do projeto.");
                return;
            }

            boolean wasActiveInEditor = codeEditor.getCurrentFile() != null
                    && codeEditor.getCurrentFile().toAbsolutePath().normalize().equals(normalizedTarget);

            Files.deleteIfExists(normalizedTarget);

            Path activeBeforeReload = wasActiveInEditor ? null : codeEditor.getCurrentFile();
            currentProject = FxProjectManager.openProject(currentProject.getProjectDirectory());
            if (activeBeforeReload != null && Files.exists(activeBeforeReload)) {
                currentProject.setMainSourceFile(activeBeforeReload);
            }

            projectExplorer.loadProject(currentProject.getProjectDirectory());

            if (wasActiveInEditor) {
                Path next = currentProject.getMainSourceFile();
                if (next != null && Files.exists(next)) {
                    codeEditor.openFile(next, FxSourceManager.readSource(next));
                } else {
                    codeEditor.openFile(null, "");
                }
            }

            FxProjectManager.saveProject(currentProject);
            console.warning("Arquivo removido: " + target.getFileName());
            setStatus("Removido: " + target.getFileName());
            compileAndRenderEffect();
        } catch (IOException ex) {
            console.error("Erro ao excluir arquivo: " + ex.getMessage());
        }
    }

    private void handleSaveCurrentFile() {
        Path activeFile = codeEditor.getCurrentFile();
        if (activeFile == null) return;
        try {
            FxSourceManager.saveSource(currentProject, activeFile, codeEditor.getCode());
            codeEditor.markSaved();
            console.info("Arquivo salvo: " + activeFile.getFileName());
        } catch (IOException ex) {
            console.error("Erro ao salvar arquivo: " + ex.getMessage());
        }
    }

    private void saveActiveEditorSilently() {
        Path activeFile = codeEditor.getCurrentFile();
        if (activeFile != null) {
            try {
                FxSourceManager.saveSource(currentProject, activeFile, codeEditor.getCode());
                codeEditor.markSaved();
            } catch (IOException ignored) {
            }
        }
    }

    private void syncFieldsFromProject() {
        durationField.setText(String.valueOf(currentProject.getDuration()));
        frameStepField.setText(String.valueOf(currentProject.getFrameStep()));
        destXField.setText(String.valueOf(currentProject.getDestinationX()));
        destYField.setText(String.valueOf(currentProject.getDestinationY()));
        destZField.setText(String.valueOf(currentProject.getDestinationZ()));
        viewport.setTargetPosition(
                currentProject.getDestinationX(),
                currentProject.getDestinationY(),
                currentProject.getDestinationZ()
        );
    }

    private Button createButton(String text, String bgHex) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + bgHex + ";" +
                "-fx-text-fill: #f3f4f6;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;" +
                "-fx-background-radius: 5;" +
                "-fx-padding: 6px 12px;" +
                "-fx-cursor: hand;"
        );
        return button;
    }

    private TextField createCompactField(String initial, double width) {
        TextField tf = new TextField(initial);
        tf.setPrefWidth(width);
        tf.setStyle(
                "-fx-background-color: #11141c;" +
                "-fx-text-fill: #f3f4f6;" +
                "-fx-border-color: #374151;" +
                "-fx-border-radius: 4;" +
                "-fx-font-family: 'Consolas';" +
                "-fx-font-size: 11px;"
        );
        return tf;
    }

    private Label createParamLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");
        return label;
    }

    private void setStatus(String message) { statusLabel.setText(message); }

    public static void main(String[] args) {
        launch(args);
    }
}