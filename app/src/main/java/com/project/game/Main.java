package com.project.game;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.system.MemoryUtil.NULL;

import com.project.game.engine.camera.Camera;
import com.project.game.engine.input.Input;
import com.project.game.engine.physics.AABB;
import com.project.game.engine.rendering.CubeMesh;
import com.project.game.engine.rendering.ShaderProgram;
import com.project.game.game.player.PlayerController;

public class Main {

    private final int width = 1280;
    private final int height = 720;
    private long window;
    private Camera camera;
    private Input input;

    private double lastX = 640;
    private double lastY = 360;
    private boolean firstMouse = true;

    private double currentX;
    private double currentY;

    private Matrix4f projection;
    private ShaderProgram shader;
    private CubeMesh cube;
    private PlayerController player;

    String vertexShader = """
            #version 330 core
            layout (location = 0) in vec3 aPos;

            uniform mat4 projection;
            uniform mat4 view;
            uniform mat4 model;

            void main() {
                gl_Position = projection * view * model * vec4(aPos, 1.0);
            }
            """;

    String fragmentShader = """
                       #version 330 core
            out vec4 FragColor;

            uniform vec3 color;

            void main() {
                FragColor = vec4(color, 1.0);
            }
                        """;

    public static void main(String[] args) {

        new Main().run();
    }

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Game App", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        projection = new Matrix4f().perspective(
                (float) Math.toRadians(70.0f),
                (float) width / height,
                0.1f,
                1000.0f);

        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(win, true);
            }
        });

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);

        shader = new ShaderProgram(vertexShader, fragmentShader);
        cube = new CubeMesh();

        player = new PlayerController(new Vector3f(0, 0, 3));
        camera = new Camera(new Vector3f(player.getPosition()).add(0.0f, player.getEyeHeight(), 0.0f));
        input = new Input(window);

        // lock cursor
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        // mouse callback
        glfwSetCursorPosCallback(window, (windowHandle, xpos, ypos) -> {
            currentX = xpos;
            currentY = ypos;
        });
    }

    private void loop() {
        float sensitivity = 0.1f;

        float lastFrame = (float) glfwGetTime();

        List<AABB> obstacles = new ArrayList<>();

        // floor
        obstacles.add(AABB.fromCenterSize(
                new Vector3f(0.0f, -1.0f, 0.0f),
                new Vector3f(20.0f, 0.5f, 20.0f)));

        // cube 1
        obstacles.add(AABB.fromCenterSize(
                new Vector3f(0.0f, 0.0f, -5.0f),
                new Vector3f(1.0f, 1.0f, 1.0f)));

        // cube 2
        obstacles.add(AABB.fromCenterSize(
                new Vector3f(2.0f, 0.0f, -8.0f),
                new Vector3f(1.0f, 1.0f, 1.0f)));

        // cube 3
        obstacles.add(AABB.fromCenterSize(
                new Vector3f(-3.0f, 1.0f, -10.0f),
                new Vector3f(1.0f, 1.0f, 1.0f)));

        // raised platform
        obstacles.add(AABB.fromCenterSize(
                new Vector3f(0.0f, 1.5f, -6.0f),
                new Vector3f(2.0f, 0.5f, 2.0f)));

        // stepping stones
        obstacles.add(AABB.fromCenterSize(
                new Vector3f(-2.0f, 0.0f, -4.0f),
                new Vector3f(1.0f, 1.0f, 1.0f)));

        obstacles.add(AABB.fromCenterSize(
                new Vector3f(-2.0f, 1.0f, -6.0f),
                new Vector3f(1.0f, 1.0f, 1.0f)));

        // wall
        obstacles.add(AABB.fromCenterSize(
                new Vector3f(3.0f, 1.5f, -7.0f),
                new Vector3f(1.0f, 3.0f, 4.0f)));

        while (!glfwWindowShouldClose(window)) {
            float currentFrame = (float) glfwGetTime();
            float deltaTime = currentFrame - lastFrame;
            lastFrame = currentFrame;

            glClearColor(0.1f, 0.12f, 0.16f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            shader.use();

            shader.setMatrix4f("projection", projection);
            shader.setMatrix4f("view", camera.getViewMatrix());

            // floor
            Matrix4f floorModel = new Matrix4f()
                    .translate(0.0f, -1.0f, 0.0f)
                    .scale(20.0f, 0.5f, 20.0f);

            shader.setMatrix4f("model", floorModel);
            shader.setVector3f("color", new Vector3f(0.3f, 0.8f, 0.3f)); // green
            cube.render();

            // cube 1
            Matrix4f cubeModel = new Matrix4f()
                    .translate(0.0f, 0.0f, -5.0f);

            shader.setMatrix4f("model", cubeModel);
            shader.setVector3f("color", new Vector3f(0.8f, 0.2f, 0.2f)); // red
            cube.render();

            // cube 2
            Matrix4f cubeModel2 = new Matrix4f()
                    .translate(2.0f, 0.0f, -8.0f);

            shader.setMatrix4f("model", cubeModel2);
            shader.setVector3f("color", new Vector3f(0.2f, 0.2f, 0.8f)); // blue
            cube.render();

            // cube 3
            Matrix4f cubeModel3 = new Matrix4f()
                    .translate(-3.0f, 1.0f, -10.0f);

            shader.setMatrix4f("model", cubeModel3);
            shader.setVector3f("color", new Vector3f(0.8f, 0.8f, 0.2f)); // yellow
            cube.render();

            // platform
            shader.setVector3f("color", new Vector3f(0.4f, 0.9f, 0.4f));
            shader.setMatrix4f("model", new Matrix4f()
                    .translate(0.0f, 1.5f, -6.0f)
                    .scale(2.0f, 0.5f, 2.0f));
            cube.render();

            // stepping cubes
            shader.setVector3f("color", new Vector3f(0.9f, 0.4f, 0.4f));
            shader.setMatrix4f("model", new Matrix4f()
                    .translate(-2.0f, 0.0f, -4.0f));
            cube.render();

            shader.setMatrix4f("model", new Matrix4f()
                    .translate(-2.0f, 1.0f, -6.0f));
            cube.render();

            // wall
            shader.setVector3f("color", new Vector3f(0.4f, 0.4f, 0.9f));
            shader.setMatrix4f("model", new Matrix4f()
                    .translate(3.0f, 1.5f, -7.0f)
                    .scale(1.0f, 3.0f, 4.0f));
            cube.render();

            // ===== MOUSE LOOK =====
            if (firstMouse) {
                lastX = currentX;
                lastY = currentY;
                firstMouse = false;
            }

            float xOffset = (float) (currentX - lastX) * sensitivity;
            float yOffset = (float) (lastY - currentY) * sensitivity;

            lastX = currentX;
            lastY = currentY;

            camera.yaw += xOffset;
            camera.pitch += yOffset;

            camera.pitch = Math.max(-89f, Math.min(89f, camera.pitch));

            // ===== MOVEMENT =====
            Vector3f front = camera.getFront();
            Vector3f right = camera.getRight();

            // keep movement flat on the XZ plane
            front.y = 0;
            front.normalize();

            right.y = 0;
            right.normalize();

            Vector3f movement = new Vector3f();

            if (input.isKeyDown(GLFW_KEY_W))
                movement.add(front);

            if (input.isKeyDown(GLFW_KEY_S))
                movement.sub(front);

            if (input.isKeyDown(GLFW_KEY_A))
                movement.sub(right);

            if (input.isKeyDown(GLFW_KEY_D))
                movement.add(right);

            if (movement.lengthSquared() > 0) {
                float speedMultiplier = player.isCrouching() ? 0.5f : 1.0f;

                movement.normalize().mul(player.getMoveSpeed() * speedMultiplier * deltaTime);

                Vector3f oldPosition = new Vector3f(player.getPosition());

                // ===== X AXIS =====
                player.getPosition().x += movement.x;

                for (AABB obstacle : obstacles) {
                    if (player.getAABB().intersects(obstacle)) {
                        player.getPosition().x = oldPosition.x;
                        break;
                    }
                }

                // ===== Z AXIS =====
                player.getPosition().z += movement.z;

                for (AABB obstacle : obstacles) {
                    if (player.getAABB().intersects(obstacle)) {
                        player.getPosition().z = oldPosition.z;
                        break;
                    }
                }
            }

            // ===== JUMP =====
            if (input.isKeyDown(GLFW_KEY_SPACE) && player.isGrounded()) {
                player.getVelocity().y = player.getJumpForce();
                player.setGrounded(false);
            }

            // ===== GRAVITY =====
            player.getVelocity().y += player.getGravity() * deltaTime;

            // ===== Y AXIS =====

            // store old position BEFORE movement
            float oldY = player.getPosition().y;
            float playerBottomOld = oldY;
            float playerTopOld = playerBottomOld + player.getCurrentHeight();

            // apply vertical movement ONCE
            player.getPosition().y += player.getVelocity().y * deltaTime;

            float playerBottomNew = player.getPosition().y;
            float playerTopNew = playerBottomNew + player.getCurrentHeight();

            boolean grounded = false;

            for (AABB obstacle : obstacles) {

                if (player.getAABB().intersects(obstacle)) {

                    float obstacleTop = obstacle.getMax().y;
                    float obstacleBottom = obstacle.getMin().y;

                    // ===== LANDING =====
                    if (player.getVelocity().y < 0 &&
                            playerBottomOld >= obstacleTop &&
                            playerBottomNew <= obstacleTop) {

                        player.getPosition().y = obstacleTop;
                        grounded = true;
                    }

                    // ===== HEAD HIT =====
                    else if (player.getVelocity().y > 0 &&
                            playerBottomOld <= obstacleBottom &&
                            playerTopNew >= obstacleBottom) {

                        player.getPosition().y = oldY;
                    }

                    player.getVelocity().y = 0;
                    break;
                }
            }

            player.setGrounded(grounded);

            // ===== FALL DEATH =====
            if (player.getPosition().y < -20.0f && player.isAlive()) {
                player.damage(100);
            }

            // ===== RESPAWN =====
            if (!player.isAlive()) {
                player.respawn();
            }

            // ===== CROUCH =====
            if (input.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
                player.setCrouching(true);
            } else {
                player.setCrouching(false);
            }

            camera.position.set(player.getPosition()).add(0.0f, player.getEyeHeight(), 0.0f);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void cleanup() {
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

}