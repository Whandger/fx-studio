package net.minecraft.util.math;

public final class MathHelper {
    public static final float PI = (float) Math.PI;
    public static final float TAU = (float) (Math.PI * 2.0);

    private MathHelper() {}

    public static float sin(float value) { return (float) Math.sin(value); }
    public static float cos(float value) { return (float) Math.cos(value); }
    public static float sqrt(float value) { return (float) Math.sqrt(value); }
    public static float clamp(float value, float min, float max) {
        return value < min ? min : Math.min(value, max);
    }
    public static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }
    public static int clamp(int value, int min, int max) {
        return value < min ? min : Math.min(value, max);
    }
    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }
    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }
}
