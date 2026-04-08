package com.project.game.engine.input;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;

public class Input {

    private long window;

    public Input(long window) {
        this.window = window;
    }

    public boolean isKeyDown(int key) {
        return glfwGetKey(window, key) == GLFW_PRESS;
    }

    public boolean isMouseButtonDown(int button) {
        return glfwGetMouseButton(window, button) == GLFW_PRESS;
    }
}