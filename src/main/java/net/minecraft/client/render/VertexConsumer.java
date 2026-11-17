package net.minecraft.client.render;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

public interface VertexConsumer {

    VertexConsumer vertex(float x, float y, float z);

    default VertexConsumer vertex(Matrix4fc matrix, float x, float y, float z) {
        Vector4f pos = new Vector4f(x, y, z, 1.0f).mul(matrix);
        return vertex(pos.x, pos.y, pos.z);
    }

    default VertexConsumer vertex(double x, double y, double z) {
        return vertex((float) x, (float) y, (float) z);
    }

    default VertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
        Vector4f pos = new Vector4f(x, y, z, 1.0f).mul(matrix);
        return vertex(pos.x, pos.y, pos.z);
    }

    VertexConsumer color(int red, int green, int blue, int alpha);

    default VertexConsumer color(float red, float green, float blue, float alpha) {
        return color(
            (int) (Math.max(0f, Math.min(1f, red)) * 255.0f),
            (int) (Math.max(0f, Math.min(1f, green)) * 255.0f),
            (int) (Math.max(0f, Math.min(1f, blue)) * 255.0f),
            (int) (Math.max(0f, Math.min(1f, alpha)) * 255.0f)
        );
    }

    default VertexConsumer color(int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;
        return color(r, g, b, a);
    }

    VertexConsumer texture(float u, float v);

    default VertexConsumer uv(float u, float v) {
        return texture(u, v);
    }

    VertexConsumer overlay(int u, int v);

    default VertexConsumer overlay(int uv) {
        return overlay(uv & 0xFFFF, (uv >> 16) & 0xFFFF);
    }

    VertexConsumer light(int u, int v);

    default VertexConsumer light(int uv) {
        return light(uv & 0xFFFF, (uv >> 16) & 0xFFFF);
    }

    VertexConsumer normal(float x, float y, float z);

    default VertexConsumer normal(Matrix3f matrix, float x, float y, float z) {
        Vector3f n = new Vector3f(x, y, z).mul(matrix);
        return normal(n.x, n.y, n.z);
    }

    default void next() {
    }

    default void vertex(
            float x, float y, float z,
            int color,
            float u, float v,
            int overlay, int light,
            float normalX, float normalY, float normalZ
    ) {
        this.vertex(x, y, z)
            .color(color)
            .texture(u, v)
            .overlay(overlay)
            .light(light)
            .normal(normalX, normalY, normalZ);
    }
}
