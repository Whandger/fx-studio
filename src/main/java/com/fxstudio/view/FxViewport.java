package com.fxstudio.view;

import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.ParallelCamera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.ArrayList;
import java.util.List;

public class FxViewport extends StackPane {

    private final Group world = new Group();
    private final Group gridGroup = new Group();
    private final Group axesGroup = new Group();
    private final Group geometryGroup = new Group();
    private final Group targetMarkerGroup = new Group();

    private final PerspectiveCamera perspectiveCamera = new PerspectiveCamera(true);
    private final ParallelCamera parallelCamera = new ParallelCamera();

    private final Translate panTranslate = new Translate(0, -0.5, 0);
    private final Rotate yawRotate = new Rotate(-30, Rotate.Y_AXIS);
    private final Rotate pitchRotate = new Rotate(-22, Rotate.X_AXIS);
    private final Translate zoomTranslate = new Translate(0, 0, -11.0);

    private SubScene subScene;
    private boolean orthographic = false;
    private boolean wireframeMode = false;
    private double cameraDistance = 11.0;
    private double mouseX;
    private double mouseY;
    private int currentTriangleCount = 0;

    private final List<MeshView> meshes = new ArrayList<>();

    public FxViewport() {
        setStyle("-fx-background-color: #090b10;");

        setupCameras();
        createGridAndAxes();
        createLighting();
        createTargetMarker();

        world.getChildren().addAll(gridGroup, axesGroup, targetMarkerGroup, geometryGroup);

        subScene = new SubScene(world, 900, 600, true, SceneAntialiasing.BALANCED);
        subScene.widthProperty().bind(widthProperty());
        subScene.heightProperty().bind(heightProperty());
        subScene.setFill(Color.rgb(9, 11, 16));
        subScene.setCamera(perspectiveCamera);

        getChildren().add(subScene);
        setupMouseControls(subScene);
    }

    private void setupCameras() {
        perspectiveCamera.setNearClip(0.01);
        perspectiveCamera.setFarClip(1000.0);
        perspectiveCamera.setFieldOfView(45.0);
        perspectiveCamera.getTransforms().addAll(
                panTranslate,
                yawRotate,
                pitchRotate,
                zoomTranslate
        );
    }

    private void createLighting() {
        AmbientLight ambient = new AmbientLight(Color.rgb(215, 220, 235));
        PointLight keyLight = new PointLight(Color.WHITE);
        keyLight.setTranslateX(8);
        keyLight.setTranslateY(-12);
        keyLight.setTranslateZ(-8);
        world.getChildren().addAll(ambient, keyLight);
    }

    private void createGridAndAxes() {
        PhongMaterial minorMat = new PhongMaterial(Color.rgb(34, 40, 54));
        PhongMaterial majorMat = new PhongMaterial(Color.rgb(55, 65, 86));

        for (int i = -10; i <= 10; i++) {
            PhongMaterial mat = (i % 5 == 0) ? majorMat : minorMat;
            double thickness = (i % 5 == 0) ? 0.014 : 0.008;

            Box lineX = new Box(20, thickness, thickness);
            lineX.setTranslateZ(i);
            lineX.setMaterial(mat);

            Box lineZ = new Box(thickness, thickness, 20);
            lineZ.setTranslateX(i);
            lineZ.setMaterial(mat);

            gridGroup.getChildren().addAll(lineX, lineZ);
        }

        Box xAxis = new Box(12, 0.028, 0.028);
        xAxis.setMaterial(new PhongMaterial(Color.rgb(220, 75, 75)));

        Box yAxis = new Box(0.028, 6, 0.028);
        yAxis.setTranslateY(-3.0);
        yAxis.setMaterial(new PhongMaterial(Color.rgb(80, 210, 110)));

        Box zAxis = new Box(0.028, 0.028, 12);
        zAxis.setMaterial(new PhongMaterial(Color.rgb(75, 135, 235)));

        axesGroup.getChildren().addAll(xAxis, yAxis, zAxis);
    }

    private void createTargetMarker() {
        Sphere marker = new Sphere(0.12);
        PhongMaterial mat = new PhongMaterial(Color.rgb(255, 140, 50, 0.85));
        marker.setMaterial(mat);
        targetMarkerGroup.getChildren().add(marker);
        setTargetPosition(0.0, 0.5, 6.0);
    }

    public void setTargetPosition(double x, double y, double z) {
        targetMarkerGroup.setTranslateX(x);
        targetMarkerGroup.setTranslateY(-y);
        targetMarkerGroup.setTranslateZ(z);
    }

    private void setupMouseControls(SubScene scene) {
        scene.setOnMousePressed(event -> {
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        });

        scene.setOnMouseDragged(event -> {
            double dx = event.getSceneX() - mouseX;
            double dy = event.getSceneY() - mouseY;

            if (event.getButton() == MouseButton.PRIMARY && !event.isShiftDown()) {
                yawRotate.setAngle(yawRotate.getAngle() + dx * 0.45);
                double nextPitch = pitchRotate.getAngle() - dy * 0.45;
                pitchRotate.setAngle(Math.max(-89.0, Math.min(89.0, nextPitch)));
            } else {
                double panFactor = cameraDistance * 0.0015;
                double rad = Math.toRadians(yawRotate.getAngle());
                panTranslate.setX(panTranslate.getX() - (dx * Math.cos(rad)) * panFactor);
                panTranslate.setZ(panTranslate.getZ() + (dx * Math.sin(rad)) * panFactor);
                panTranslate.setY(panTranslate.getY() - dy * panFactor);
            }

            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        });

        scene.setOnScroll(event -> {
            cameraDistance -= event.getDeltaY() * 0.012;
            cameraDistance = Math.max(1.2, Math.min(120.0, cameraDistance));
            zoomTranslate.setZ(-cameraDistance);
            updateOrthoTransform();
        });
    }

    public void setOrthographic(boolean orthographic) {
        this.orthographic = orthographic;
        if (orthographic) {
            updateOrthoTransform();
            subScene.setCamera(parallelCamera);
        } else {
            subScene.setCamera(perspectiveCamera);
        }
    }

    public boolean isOrthographic() { return orthographic; }

    private void updateOrthoTransform() {
        double w = Math.max(1, getWidth());
        double h = Math.max(1, getHeight());
        double scale = Math.max(15.0, 500.0 / cameraDistance);
        parallelCamera.getTransforms().setAll(
                new Translate(-w / 2.0, -h / 2.0, 0),
                panTranslate,
                yawRotate,
                pitchRotate,
                new Scale(scale, scale, scale)
        );
    }

    public void setGridVisible(boolean visible) { gridGroup.setVisible(visible); }
    public void setAxesVisible(boolean visible) { axesGroup.setVisible(visible); }

    public void setWireframeMode(boolean wireframe) {
        this.wireframeMode = wireframe;
        for (MeshView view : meshes) {
            view.setDrawMode(wireframe ? DrawMode.LINE : DrawMode.FILL);
        }
    }

    public boolean isWireframeMode() { return wireframeMode; }

    public void setMeshViews(List<MeshView> views, int totalTriangles) {
        geometryGroup.getChildren().setAll(views);
        meshes.clear();
        meshes.addAll(views);
        currentTriangleCount = totalTriangles;
    }

    public void clear() {
        geometryGroup.getChildren().clear();
        meshes.clear();
        currentTriangleCount = 0;
    }

    public int getTriangleCount() { return currentTriangleCount; }

    public void resetCamera() {
        panTranslate.setX(0);
        panTranslate.setY(-0.5);
        panTranslate.setZ(0);
        yawRotate.setAngle(-30);
        pitchRotate.setAngle(-22);
        cameraDistance = 11.0;
        zoomTranslate.setZ(-cameraDistance);
        if (orthographic) updateOrthoTransform();
    }
}
