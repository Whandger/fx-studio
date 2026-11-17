package com.fxstudio.model;

public class FxSettings {

    private int targetFps = 60;
    private int renderQuality = 2;
    private boolean showGrid = true;
    private boolean showAxes = true;
    private boolean showWireframe = false;
    private boolean orthographic = false;
    private boolean enableLighting = true;
    private boolean enableAntialiasing = true;
    private boolean autoPlayAfterRender = true;

    private double cameraSensitivity = 1.0;
    private double cameraZoomSpeed = 1.0;

    public int getTargetFps() { return targetFps; }
    public void setTargetFps(int targetFps) { this.targetFps = Math.max(1, Math.min(240, targetFps)); }
    public int getRenderQuality() { return renderQuality; }
    public void setRenderQuality(int renderQuality) { this.renderQuality = Math.max(1, Math.min(4, renderQuality)); }
    public boolean isShowGrid() { return showGrid; }
    public void setShowGrid(boolean showGrid) { this.showGrid = showGrid; }
    public boolean isShowAxes() { return showAxes; }
    public void setShowAxes(boolean showAxes) { this.showAxes = showAxes; }
    public boolean isShowWireframe() { return showWireframe; }
    public void setShowWireframe(boolean showWireframe) { this.showWireframe = showWireframe; }
    public boolean isOrthographic() { return orthographic; }
    public void setOrthographic(boolean orthographic) { this.orthographic = orthographic; }
    public boolean isEnableLighting() { return enableLighting; }
    public void setEnableLighting(boolean enableLighting) { this.enableLighting = enableLighting; }
    public boolean isEnableAntialiasing() { return enableAntialiasing; }
    public void setEnableAntialiasing(boolean enableAntialiasing) { this.enableAntialiasing = enableAntialiasing; }
    public boolean isAutoPlayAfterRender() { return autoPlayAfterRender; }
    public void setAutoPlayAfterRender(boolean autoPlayAfterRender) { this.autoPlayAfterRender = autoPlayAfterRender; }
    public double getCameraSensitivity() { return cameraSensitivity; }
    public void setCameraSensitivity(double cameraSensitivity) { this.cameraSensitivity = cameraSensitivity; }
    public double getCameraZoomSpeed() { return cameraZoomSpeed; }
    public void setCameraZoomSpeed(double cameraZoomSpeed) { this.cameraZoomSpeed = cameraZoomSpeed; }
}
