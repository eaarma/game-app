package com.project.game.engine.physics;

import org.joml.Vector3f;

public class AABB {

    private final Vector3f min;
    private final Vector3f max;

    public AABB(Vector3f min, Vector3f max) {
        this.min = new Vector3f(min);
        this.max = new Vector3f(max);
    }

    public Vector3f getMin() {
        return min;
    }

    public Vector3f getMax() {
        return max;
    }

    // ===== INTERSECTION TEST =====
    public boolean intersects(AABB other) {
        return this.max.x > other.min.x && this.min.x < other.max.x &&
                this.max.y > other.min.y && this.min.y < other.max.y &&
                this.max.z > other.min.z && this.min.z < other.max.z;
    }

    // ===== FACTORY METHOD =====
    public static AABB fromCenterSize(Vector3f center, Vector3f size) {
        Vector3f half = new Vector3f(size).mul(0.5f);
        return new AABB(
                new Vector3f(center).sub(half),
                new Vector3f(center).add(half));
    }

    // ===== RAY INTERSECTION (returns distance or null if no hit) =====
    public Float intersectRay(Vector3f origin, Vector3f direction) {

        float tMin = (min.x - origin.x) / direction.x;
        float tMax = (max.x - origin.x) / direction.x;

        if (tMin > tMax) {
            float temp = tMin;
            tMin = tMax;
            tMax = temp;
        }

        float tyMin = (min.y - origin.y) / direction.y;
        float tyMax = (max.y - origin.y) / direction.y;

        if (tyMin > tyMax) {
            float temp = tyMin;
            tyMin = tyMax;
            tyMax = temp;
        }

        if ((tMin > tyMax) || (tyMin > tMax)) {
            return null;
        }

        if (tyMin > tMin)
            tMin = tyMin;
        if (tyMax < tMax)
            tMax = tyMax;

        float tzMin = (min.z - origin.z) / direction.z;
        float tzMax = (max.z - origin.z) / direction.z;

        if (tzMin > tzMax) {
            float temp = tzMin;
            tzMin = tzMax;
            tzMax = temp;
        }

        if ((tMin > tzMax) || (tzMin > tMax)) {
            return null;
        }

        if (tzMin > tMin)
            tMin = tzMin;

        return tMin;
    }

    // ===== DEBUG (optional but useful) =====
    @Override
    public String toString() {
        return "AABB[min=" + min + ", max=" + max + "]";
    }
}