package com.project.game.engine.camera;

import org.joml.Vector3f;
import org.joml.Matrix4f;

public class Camera {

    public Vector3f position;
    public float pitch;
    public float yaw;

    public Camera(Vector3f position) {
        this.position = position;
        this.pitch = 0;
        this.yaw = -90;
    }

    public Vector3f getFront() {
        Vector3f front = new Vector3f();

        front.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));

        return front.normalize();
    }

    public Vector3f getRight() {
        return new Vector3f(getFront()).cross(0, 1, 0).normalize();
    }

    public Matrix4f getViewMatrix() {
        Vector3f center = new Vector3f(position).add(getFront());

        return new Matrix4f().lookAt(
                position,
                center,
                new Vector3f(0, 1, 0));
    }
}