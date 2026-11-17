package com.mojang.blaze3d.vertex;

import org.joml.Matrix4fc;
import org.joml.Vector3f;

public interface VertexConsumer extends net.minecraft.client.render.VertexConsumer {
    default VertexConsumer addVertex(Matrix4fc pose, float x, float y, float z) {
        Vector3f pos = pose.transformPosition(x, y, z, new Vector3f());
        this.vertex(pos.x, pos.y, pos.z);
        return this;
    }

    default VertexConsumer addVertex(float x, float y, float z) {
        this.vertex(x, y, z);
        return this;
    }

    default VertexConsumer setColor(int red, int green, int blue, int alpha) {
        this.color(red, green, blue, alpha);
        return this;
    }

    default VertexConsumer setColor(float red, float green, float blue, float alpha) {
        this.color(red, green, blue, alpha);
        return this;
    }

    default VertexConsumer setColor(int argb) {
        this.color(argb);
        return this;
    }
}