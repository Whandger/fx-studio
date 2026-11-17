package net.minecraft.world.phys;

import net.minecraft.util.math.Vec3d;

public class Vec3 extends Vec3d {
    public static final Vec3 ZERO = new Vec3(0.0, 0.0, 0.0);

    public Vec3(double x, double y, double z) {
        super(x, y, z);
    }

    public Vec3(Vec3d v) {
        super(v.x, v.y, v.z);
    }

    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }

    public double lengthSqr() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public double distanceToSqr(Vec3 vec) {
        double dx = vec.x - this.x;
        double dy = vec.y - this.y;
        double dz = vec.z - this.z;
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    public Vec3 add(double x, double y, double z) {
        return new Vec3(this.x + x, this.y + y, this.z + z);
    }

    public Vec3 add(Vec3 vec) {
        return add(vec.x, vec.y, vec.z);
    }

    @Override
    public Vec3 subtract(double x, double y, double z) {
        return new Vec3(this.x - x, this.y - y, this.z - z);
    }

    public Vec3 subtract(Vec3 vec) {
        return subtract(vec.x, vec.y, vec.z);
    }

    public Vec3 scale(double factor) {
        return new Vec3(this.x * factor, this.y * factor, this.z * factor);
    }

    @Override
    public Vec3 normalize() {
        double len = length();
        return len < 1.0E-4 ? ZERO : new Vec3(x / len, y / len, z / len);
    }

    public double dot(Vec3 vec) {
        return dotProduct(vec);
    }

    public Vec3 cross(Vec3 vec) {
        return new Vec3(
            this.y * vec.z - this.z * vec.y,
            this.z * vec.x - this.x * vec.z,
            this.x * vec.y - this.y * vec.x
        );
    }

    public Vec3 lerp(Vec3 to, double delta) {
        return new Vec3(
            this.x + (to.x - this.x) * delta,
            this.y + (to.y - this.y) * delta,
            this.z + (to.z - this.z) * delta
        );
    }
}