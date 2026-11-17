package net.minecraft.util.math;

public class Vec3d {
    public static final Vec3d ZERO = new Vec3d(0.0, 0.0, 0.0);
    public final double x;
    public final double y;
    public final double z;

    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }

    public Vec3d add(double x, double y, double z) {
        return new Vec3d(this.x + x, this.y + y, this.z + z);
    }

    public Vec3d add(Vec3d vec) {
        return add(vec.x, vec.y, vec.z);
    }

    public Vec3d subtract(double x, double y, double z) {
        return new Vec3d(this.x - x, this.y - y, this.z - z);
    }

    public Vec3d subtract(Vec3d vec) {
        return subtract(vec.x, vec.y, vec.z);
    }

    public Vec3d multiply(double value) {
        return new Vec3d(this.x * value, this.y * value, this.z * value);
    }

    public Vec3d multiply(double x, double y, double z) {
        return new Vec3d(this.x * x, this.y * y, this.z * z);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double lengthSquared() {
        return x * x + y * y + z * z;
    }

    public Vec3d normalize() {
        double len = length();
        return len < 1.0E-4 ? ZERO : new Vec3d(x / len, y / len, z / len);
    }

    public double dotProduct(Vec3d vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }

    public Vec3d crossProduct(Vec3d vec) {
        return new Vec3d(
            this.y * vec.z - this.z * vec.y,
            this.z * vec.x - this.x * vec.z,
            this.x * vec.y - this.y * vec.x
        );
    }

    public Vec3d lerp(Vec3d to, double delta) {
        return new Vec3d(
            this.x + (to.x - this.x) * delta,
            this.y + (to.y - this.y) * delta,
            this.z + (to.z - this.z) * delta
        );
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
