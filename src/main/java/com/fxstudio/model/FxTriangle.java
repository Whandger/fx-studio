package com.fxstudio.model;

import javafx.scene.paint.Color;

public final class FxTriangle {

    private final float[] vertices;
    private final float[] uvs;
    private final float[] normals;
    private final Color color;

    public FxTriangle(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            Color color
    ) {
        this(x1, y1, z1, x2, y2, z2, x3, y3, z3,
             new float[]{0f, 0f, 1f, 0f, 0f, 1f},
             new float[]{0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f},
             color);
    }

    public FxTriangle(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float[] uvs,
            float[] normals,
            Color color
    ) {
        this.vertices = new float[]{
                x1, y1, z1,
                x2, y2, z2,
                x3, y3, z3
        };
        this.uvs = uvs != null && uvs.length == 6 ? uvs.clone() : new float[]{0f, 0f, 1f, 0f, 0f, 1f};
        this.normals = normals != null && normals.length == 9 ? normals.clone() : new float[]{0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f};
        this.color = color != null ? color : Color.WHITE;
    }

    public float[] getVertices() { return vertices.clone(); }
    public float[] getUvs() { return uvs.clone(); }
    public float[] getNormals() { return normals.clone(); }
    public Color getColor() { return color; }
}
