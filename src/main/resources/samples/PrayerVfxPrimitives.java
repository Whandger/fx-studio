package com.github.ob_yekt.simpleskills.client.vfx;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

public final class PrayerVfxPrimitives {

    private PrayerVfxPrimitives() {}

    public static void quad(
            Matrix4fc m,
            VertexConsumer out,
            Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3,
            float r, float g, float b, float a
    ) {
        Vec3 e1 = p1.subtract(p0);
        Vec3 e2 = p3.subtract(p0);
        Vec3 n = e1.cross(e2).normalize();
        float nx = (float) n.x;
        float ny = (float) n.y;
        float nz = (float) n.z;

        emitVertex(m, out, p0, r, g, b, a, 0f, 0f, nx, ny, nz);
        emitVertex(m, out, p1, r, g, b, a, 1f, 0f, nx, ny, nz);
        emitVertex(m, out, p2, r, g, b, a, 1f, 1f, nx, ny, nz);
        emitVertex(m, out, p3, r, g, b, a, 0f, 1f, nx, ny, nz);
    }

    private static void emitVertex(
            Matrix4fc m,
            VertexConsumer out,
            Vec3 p,
            float r, float g, float b, float a,
            float u, float v,
            float nx, float ny, float nz
    ) {
        out.vertex(m, (float) p.x, (float) p.y, (float) p.z)
           .color(r, g, b, a)
           .texture(u, v)
           .overlay(0, 10)
           .light(0xF000F0)
           .normal(nx, ny, nz);
    }
}
