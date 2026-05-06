package com.dreggcake.src;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    public Vector3f position;
    public Vector3f front = new Vector3f(0.0f, 0.0f, -1.0f);
    public Vector3f up = new Vector3f();
    public Vector3f right = new Vector3f();
    public Vector3f worldUp;

    public float yaw = -90.0f;
    public float pitch = 0.0f;

    public float movementSpeed = 2.5f;
    public float mouseSensitivity = 0.1f;
    public float zoom = 45.0f;

    public Camera(Vector3f position) {
        this.position = position;
        this.worldUp = new Vector3f(0.0f, 1.0f, 0.0f);
        updateCameraVectors();
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
    }

    public void processKeyboard(String direction, float deltaTime) {
        float velocity = movementSpeed * deltaTime;

        if (direction.equals("FORWARD"))
            position.add(new Vector3f(front).mul(velocity));
        if (direction.equals("BACKWARD"))
            position.sub(new Vector3f(front).mul(velocity));
        if (direction.equals("LEFT"))
            position.sub(new Vector3f(right).mul(velocity));
        if (direction.equals("RIGHT"))
            position.add(new Vector3f(right).mul(velocity));
    }

    public void processMouseMovement(float xoffset, float yoffset) {
        xoffset *= mouseSensitivity;
        yoffset *= mouseSensitivity;

        yaw += xoffset;
        pitch += yoffset;

        // clamp
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;

        updateCameraVectors();
    }

    public void processMouseScroll(float yoffset) {
        zoom -= yoffset;
        if (zoom < 1.0f) zoom = 1.0f;
        if (zoom > 45.0f) zoom = 45.0f;
    }

    private void updateCameraVectors() {
        Vector3f front = new Vector3f();
        front.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));

        this.front = front.normalize();

        // also re-calc right & up
        right = new Vector3f(this.front).cross(worldUp).normalize();
        up = new Vector3f(right).cross(this.front).normalize();
    }
}