package com.fxstudio.renderer;

import com.fxstudio.model.FxTriangle;
import com.fxstudio.view.FxViewport;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FxMeshRenderer {

    private final FxViewport viewport;

    public FxMeshRenderer(FxViewport viewport) {
        this.viewport = viewport;
    }

    public void render(List<FxTriangle> triangles) {
        viewport.clear();
        if (triangles == null || triangles.isEmpty()) return;

        Map<Integer, List<FxTriangle>> batches = new HashMap<>();
        for (FxTriangle tri : triangles) {
            if (tri == null) continue;
            int key = quantizeColor(tri.getColor());
            batches.computeIfAbsent(key, k -> new ArrayList<>()).add(tri);
        }

        boolean wireframe = viewport.isWireframeMode();
        List<MeshView> views = new ArrayList<>(batches.size());

        for (Map.Entry<Integer, List<FxTriangle>> entry : batches.entrySet()) {
            List<FxTriangle> group = entry.getValue();
            Color color = group.get(0).getColor();

            TriangleMesh mesh = new TriangleMesh();
            float[] points = new float[group.size() * 9];
            float[] texCoords = new float[group.size() * 6];
            int[] faces = new int[group.size() * 6];

            int pIdx = 0;
            int tIdx = 0;
            int fIdx = 0;
            int vCounter = 0;

            for (FxTriangle tri : group) {
                float[] v = tri.getVertices();
                float[] uv = tri.getUvs();

                points[pIdx++] = v[0];
                points[pIdx++] = -v[1];
                points[pIdx++] = v[2];

                points[pIdx++] = v[3];
                points[pIdx++] = -v[4];
                points[pIdx++] = v[5];

                points[pIdx++] = v[6];
                points[pIdx++] = -v[7];
                points[pIdx++] = v[8];

                for (int i = 0; i < 6; i++) {
                    texCoords[tIdx++] = uv[i];
                }

                faces[fIdx++] = vCounter;
                faces[fIdx++] = vCounter;
                faces[fIdx++] = vCounter + 1;
                faces[fIdx++] = vCounter + 1;
                faces[fIdx++] = vCounter + 2;
                faces[fIdx++] = vCounter + 2;
                vCounter += 3;
            }

            mesh.getPoints().setAll(points);
            mesh.getTexCoords().setAll(texCoords);
            mesh.getFaces().setAll(faces);

            PhongMaterial material = new PhongMaterial(color);
            material.setSpecularColor(color.brighter());
            material.setSpecularPower(16.0);

            MeshView meshView = new MeshView(mesh);
            meshView.setMaterial(material);
            meshView.setCullFace(CullFace.NONE);
            meshView.setDrawMode(wireframe ? DrawMode.LINE : DrawMode.FILL);
            views.add(meshView);
        }

        viewport.setMeshViews(views, triangles.size());
    }

    public void clear() {
        viewport.clear();
    }

    private static int quantizeColor(Color c) {
        int r = (int) Math.round(c.getRed() * 31.0);
        int g = (int) Math.round(c.getGreen() * 31.0);
        int b = (int) Math.round(c.getBlue() * 31.0);
        int a = (int) Math.round(c.getOpacity() * 15.0);
        return (a << 15) | (r << 10) | (g << 5) | b;
    }
}
