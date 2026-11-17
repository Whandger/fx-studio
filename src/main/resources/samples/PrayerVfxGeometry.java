package com.github.ob_yekt.simpleskills.client.vfx;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

public final class PrayerVfxGeometry {

    private PrayerVfxGeometry() {}

    public static void sphere(
            Matrix4fc m,
            VertexConsumer out,
            Vec3 center,
            double radius,
            int rings,
            int sectors,
            float r, float g, float b, float a
    ) {
        for (int i = 0; i < rings; i++) {
            double v0 = (double) i / rings;
            double v1 = (double) (i + 1) / rings;
            double phi0 = Math.PI * (v0 - 0.5);
            double phi1 = Math.PI * (v1 - 0.5);

            double y0 = Math.sin(phi0) * radius;
            double r0 = Math.cos(phi0) * radius;
            double y1 = Math.sin(phi1) * radius;
            double r1 = Math.cos(phi1) * radius;

            for (int j = 0; j < sectors; j++) {
                double u0 = (double) j / sectors;
                double u1 = (double) (j + 1) / sectors;
                double theta0 = 2.0 * Math.PI * u0;
                double theta1 = 2.0 * Math.PI * u1;

                Vec3 p0 = center.add(Math.cos(theta0) * r0, y0, Math.sin(theta0) * r0);
                Vec3 p1 = center.add(Math.cos(theta1) * r0, y0, Math.sin(theta1) * r0);
                Vec3 p2 = center.add(Math.cos(theta1) * r1, y1, Math.sin(theta1) * r1);
                Vec3 p3 = center.add(Math.cos(theta0) * r1, y1, Math.sin(theta0) * r1);

                PrayerVfxPrimitives.quad(m, out, p0, p1, p2, p3, r, g, b, a);
            }
        }
    }

    public static void ribbonSegment(
            Matrix4fc m,
            VertexConsumer out,
            Vec3 aPos,
            Vec3 bPos,
            double radiusA,
            double radiusB,
            int sides,
            float r, float g, float b, float a
    ) {
        Vec3 dir = bPos.subtract(aPos).normalize();
        Vec3 up = Math.abs(dir.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 side1 = dir.cross(up).normalize();
        Vec3 side2 = dir.cross(side1).normalize();

        for (int i = 0; i < sides; i++) {
            double ang0 = (2.0 * Math.PI * i) / sides;
            double ang1 = (2.0 * Math.PI * (i + 1)) / sides;

            Vec3 off0 = side1.scale(Math.cos(ang0)).add(side2.scale(Math.sin(ang0)));
            Vec3 off1 = side1.scale(Math.cos(ang1)).add(side2.scale(Math.sin(ang1)));

            Vec3 p0 = aPos.add(off0.scale(radiusA));
            Vec3 p1 = aPos.add(off1.scale(radiusA));
            Vec3 p2 = bPos.add(off1.scale(radiusB));
            Vec3 p3 = bPos.add(off0.scale(radiusB));

            PrayerVfxPrimitives.quad(m, out, p0, p1, p2, p3, r, g, b, a);
        }
    }
}
