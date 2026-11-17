package com.fxstudio.renderer;

import com.fxstudio.model.FxTriangle;
import javafx.scene.paint.Color;
import net.minecraft.client.render.VertexConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RecordingVertexConsumer implements com.mojang.blaze3d.vertex.VertexConsumer {

    public enum PrimitiveMode {
        QUADS,
        TRIANGLES
    }

    private static final class VertexData {
        final float x, y, z;
        final int r, g, b, a;
        final float u, v;
        final float nx, ny, nz;

        VertexData(float x, float y, float z, int r, int g, int b, int a,
                   float u, float v, float nx, float ny, float nz) {
            this.x = x; this.y = y; this.z = z;
            this.r = r; this.g = g; this.b = b; this.a = a;
            this.u = u; this.v = v;
            this.nx = nx; this.ny = ny; this.nz = nz;
        }
    }

    private final List<FxTriangle> triangles = new ArrayList<>();
    private final List<VertexData> buffer = new ArrayList<>(4);
    private PrimitiveMode mode = PrimitiveMode.QUADS;

    private boolean hasPendingVertex = false;
    private float curX, curY, curZ;
    private int curR = 255, curG = 255, curB = 255, curA = 255;
    private float curU = 0f, curV = 0f;
    private float curNx = 0f, curNy = 1f, curNz = 0f;

    public void setPrimitiveMode(PrimitiveMode mode) {
        flushVertex();
        this.mode = mode == null ? PrimitiveMode.QUADS : mode;
        this.buffer.clear();
    }

    public PrimitiveMode getPrimitiveMode() { return mode; }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        flushVertex();
        this.curX = x;
        this.curY = y;
        this.curZ = z;
        this.curR = 255;
        this.curG = 255;
        this.curB = 255;
        this.curA = 255;
        this.curU = 0f;
        this.curV = 0f;
        this.curNx = 0f;
        this.curNy = 1f;
        this.curNz = 0f;
        this.hasPendingVertex = true;
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        this.curR = clampByte(red);
        this.curG = clampByte(green);
        this.curB = clampByte(blue);
        this.curA = clampByte(alpha);
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        this.curU = u;
        this.curV = v;
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) { return this; }

    @Override
    public VertexConsumer light(int u, int v) { return this; }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        this.curNx = x;
        this.curNy = y;
        this.curNz = z;
        return this;
    }

    @Override
    public void next() { flushVertex(); }

    public void finish() {
        flushVertex();
        if (buffer.size() == 3) {
            emitTriangle(buffer.get(0), buffer.get(1), buffer.get(2));
        }
        buffer.clear();
    }

    private void flushVertex() {
        if (!hasPendingVertex) return;
        hasPendingVertex = false;

        buffer.add(new VertexData(
                curX, curY, curZ,
                curR, curG, curB, curA,
                curU, curV,
                curNx, curNy, curNz
        ));

        int targetVertices = (mode == PrimitiveMode.QUADS) ? 4 : 3;
        if (buffer.size() == targetVertices) {
            if (mode == PrimitiveMode.QUADS) {
                VertexData v0 = buffer.get(0);
                VertexData v1 = buffer.get(1);
                VertexData v2 = buffer.get(2);
                VertexData v3 = buffer.get(3);
                emitTriangle(v0, v1, v2);
                emitTriangle(v0, v2, v3);
            } else {
                emitTriangle(buffer.get(0), buffer.get(1), buffer.get(2));
            }
            buffer.clear();
        }
    }

    private void emitTriangle(VertexData a, VertexData b, VertexData c) {
        double r = (a.r + b.r + c.r) / (3.0 * 255.0);
        double g = (a.g + b.g + c.g) / (3.0 * 255.0);
        double bl = (a.b + b.b + c.b) / (3.0 * 255.0);
        double al = (a.a + b.a + c.a) / (3.0 * 255.0);

        if (al <= 0.002) return;

        Color avgColor = Color.color(clamp01(r), clamp01(g), clamp01(bl), clamp01(al));
        float[] uvs = new float[]{a.u, a.v, b.u, b.v, c.u, c.v};
        float[] normals = new float[]{a.nx, a.ny, a.nz, b.nx, b.ny, b.nz, c.nx, c.ny, c.nz};

        triangles.add(new FxTriangle(
                a.x, a.y, a.z,
                b.x, b.y, b.z,
                c.x, c.y, c.z,
                uvs,
                normals,
                avgColor
        ));
    }

    public List<FxTriangle> getTriangles() { return Collections.unmodifiableList(triangles); }
    public int getTriangleCount() { return triangles.size(); }

    public void clear() {
        triangles.clear();
        buffer.clear();
        hasPendingVertex = false;
    }

    private static int clampByte(int v) { return Math.max(0, Math.min(255, v)); }
    private static double clamp01(double v) { return !Double.isFinite(v) ? 0.0 : Math.max(0.0, Math.min(1.0, v)); }
}
