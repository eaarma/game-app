package com.project.game.game.enemy;

import java.util.List;

import org.joml.Vector3f;

import com.project.game.engine.physics.AABB;
import com.project.game.game.player.PlayerController;

public class Enemy {

    private Vector3f position;
    private float speed;
    private int health;
    private boolean alive;

    private float attackRange = 1.5f;
    private float attackDamage = 10f;
    private float attackCooldown = 1.0f; // seconds

    private float attackTimer = 0f;

    private float deathTimer = 0f;
    private float deathDuration = 0.5f; // half second

    private float hitTimer = 0f;
    private float hitDuration = 0.15f;

    public Enemy(Vector3f spawnPosition) {
        this.position = new Vector3f(spawnPosition);
        this.speed = 2.0f;
        this.health = 50;
        this.alive = true;

    }

    public Vector3f getPosition() {
        return position;
    }

    public boolean isAlive() {
        return alive;
    }

    public void update(Vector3f playerPosition, float deltaTime, List<AABB> obstacles) {
        if (!alive)
            return;

        if (hitTimer > 0f) {
            hitTimer -= deltaTime;
        }
        Vector3f direction = new Vector3f(playerPosition).sub(position);
        direction.y = 0;

        float distance = direction.length();

        if (distance > attackRange) {
            direction.normalize();

            Vector3f movement = new Vector3f(direction).mul(speed * deltaTime);
            Vector3f oldPosition = new Vector3f(position);

            // X
            position.x += movement.x;
            for (AABB obstacle : obstacles) {
                if (getAABB().intersects(obstacle)) {
                    position.x = oldPosition.x;
                    break;
                }
            }

            // Z
            position.z += movement.z;
            for (AABB obstacle : obstacles) {
                if (getAABB().intersects(obstacle)) {
                    position.z = oldPosition.z;
                    break;
                }
            }

            if (!alive) {
                deathTimer -= deltaTime;
                return;
            }
        }
    }

    public void damage(int amount) {
        health -= amount;

        hitTimer = hitDuration;

        if (health <= 0 && alive) {
            alive = false;
            deathTimer = deathDuration;
        }
    }

    public AABB getAABB() {
        float width = 0.8f;
        float height = 1.8f;
        float halfWidth = width / 2.0f;

        Vector3f min = new Vector3f(
                position.x - halfWidth,
                position.y,
                position.z - halfWidth);

        Vector3f max = new Vector3f(
                position.x + halfWidth,
                position.y + height,
                position.z + halfWidth);

        return new AABB(min, max);
    }

    public void tryAttack(PlayerController player, float deltaTime) {
        if (!alive)
            return;

        attackTimer -= deltaTime;

        float distance = new Vector3f(player.getPosition())
                .sub(position)
                .length();

        if (distance <= attackRange && attackTimer <= 0f) {
            player.damage((int) attackDamage);
            attackTimer = attackCooldown;

            System.out.println("Player hit!");
        }
    }

    public boolean isRemovable() {
        return !alive && deathTimer <= 0f;
    }

    public boolean isHit() {
        return hitTimer > 0f;
    }
}