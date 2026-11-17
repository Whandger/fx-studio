package com.github.ob_yekt.simpleskills.client.vfx;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

public final class PrayerFireballVfx {

    private PrayerFireballVfx() {}

    public static void draw(
            Matrix4fc m,
            VertexConsumer out,
            Vec3 destination,
            float age,
            int duration
    ) {
        float progress = Math.max(0.0f, Math.min(1.0f, age / Math.max(1.0f, (float) duration)));
        double arcHeight = Math.sin(progress * Math.PI) * 1.15;
        Vec3 origin = new Vec3(0.0, 0.8, 0.0);
        Vec3 currentPos = origin.lerp(destination, progress).add(0.0, arcHeight, 0.0);

        if (age <= duration) {
            float pulse = 1.0f + 0.14f * (float) Math.sin(age * 1.35);
            double coreRadius = 0.28 * pulse;
            double auraRadius = 0.46 * pulse;

            PrayerVfxGeometry.sphere(m, out, currentPos, coreRadius, 10, 14, 1.0f, 0.92f, 0.45f, 0.95f);
            PrayerVfxGeometry.sphere(m, out, currentPos, auraRadius, 10, 14, 1.0f, 0.36f, 0.08f, 0.48f);

            int trailSegments = 8;
            for (int i = 0; i < trailSegments; i++) {
                float t0 = Math.max(0.0f, progress - (i * 0.035f));
                float t1 = Math.max(0.0f, progress - ((i + 1) * 0.035f));
                if (t0 <= t1) continue;

                Vec3 p0 = origin.lerp(destination, t0).add(0.0, Math.sin(t0 * Math.PI) * 1.15, 0.0);
                Vec3 p1 = origin.lerp(destination, t1).add(0.0, Math.sin(t1 * Math.PI) * 1.15, 0.0);

                float fade = 1.0f - ((float) i / trailSegments);
                PrayerVfxGeometry.ribbonSegment(
                        m, out, p0, p1,
                        coreRadius * 0.85 * fade,
                        coreRadius * 0.55 * fade,
                        8,
                        1.0f, 0.45f * fade, 0.1f, 0.65f * fade
                );
            }
        } else {
            float impactAge = age - duration;
            float impactT = Math.min(1.0f, impactAge / 6.0f);
            double blastRadius = 0.4 + impactT * 1.85;
            float alpha = (1.0f - impactT) * 0.75f;

            PrayerVfxGeometry.sphere(m, out, destination, blastRadius * 0.65, 10, 14, 1.0f, 0.85f, 0.35f, alpha);
            PrayerVfxGeometry.sphere(m, out, destination, blastRadius, 12, 16, 0.95f, 0.25f, 0.05f, alpha * 0.55f);
        }
    }
}
