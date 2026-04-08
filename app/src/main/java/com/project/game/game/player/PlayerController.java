package com.project.game.game.player;

import org.joml.Vector3f;

import com.project.game.engine.physics.AABB;

public class PlayerController {
    private final Vector3f position;
    private final Vector3f velocity;

    private float moveSpeed;
    private float jumpForce;
    private float gravity;

    private boolean grounded;
    private boolean crouching;

    private float standingHeight;
    private float crouchingHeight;
    private float width;

    public PlayerController(Vector3f spawnPosition) {
        this.position = new Vector3f(spawnPosition);
        this.velocity = new Vector3f();

        this.moveSpeed = 3.5f;
        this.jumpForce = 5.5f;
        this.gravity = -11.0f;

        this.grounded = false;
        this.crouching = false;
        this.width = 0.6f;

        this.standingHeight = 1.8f;
        this.crouchingHeight = 1.0f;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public float getJumpForce() {
        return jumpForce;
    }

    public float getGravity() {
        return gravity;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public boolean isCrouching() {
        return crouching;
    }

    public void setCrouching(boolean crouching) {
        this.crouching = crouching;
    }

    public float getCurrentHeight() {
        return crouching ? crouchingHeight : standingHeight;
    }

    public float getEyeHeight() {
        return crouching ? 0.9f : 1.6f;
    }

    public AABB getAABB() {
        float halfWidth = width / 2.0f;

        Vector3f min = new Vector3f(
                position.x - halfWidth,
                position.y,
                position.z - halfWidth);

        Vector3f max = new Vector3f(
                position.x + halfWidth,
                position.y + getCurrentHeight(),
                position.z + halfWidth);

        return new AABB(min, max);
    }
}