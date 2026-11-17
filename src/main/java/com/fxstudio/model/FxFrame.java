package com.fxstudio.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FxFrame {

    private final float age;
    private final int duration;
    private final List<FxTriangle> triangles;

    public FxFrame(float age, int duration, List<FxTriangle> triangles) {
        this.age = age;
        this.duration = duration;
        this.triangles = triangles == null ? new ArrayList<>() : new ArrayList<>(triangles);
    }

    public float getAge() { return age; }
    public int getDuration() { return duration; }
    public List<FxTriangle> getTriangles() { return Collections.unmodifiableList(triangles); }
    public int getTriangleCount() { return triangles.size(); }
    public boolean isEmpty() { return triangles.isEmpty(); }
}
