package com.dreggcake.src;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    // ===================== POSITION & DIRECTION =====================

    public Vector3f position;
    // Where the camera is in world space

    public Vector3f front = new Vector3f(0.0f, 0.0f, -1.0f);
    // Direction the camera is looking (forward vector)

    public Vector3f up = new Vector3f();
    // Camera's "up" direction (changes when you look around)

    public Vector3f right = new Vector3f();
    // Perpendicular to front & up → used for left/right movement

    public Vector3f worldUp;
    // Constant global up direction (usually (0,1,0))
    // Used as reference to calculate right & up


    // ===================== ROTATION =====================

    public float yaw = -90.0f;
    // Horizontal rotation (left/right)
    // -90 makes initial front point toward -Z (OpenGL default)

    public float pitch = 0.0f;
    // Vertical rotation (up/down)


    // ===================== SETTINGS =====================

    public float movementSpeed = 2.5f;
    // How fast camera moves with WASD

    public float mouseSensitivity = 0.1f;
    // How sensitive mouse movement is

    public float zoom = 45.0f;
    // Field of view (FOV), used in projection matrix


    // ===================== CONSTRUCTOR =====================

    public Camera(Vector3f position) {
        this.position = position;

        // Set global up direction (does NOT change)
        this.worldUp = new Vector3f(0.0f, 1.0f, 0.0f);

        // Calculate initial front/right/up vectors
        updateCameraVectors();
    }


    // ===================== VIEW MATRIX =====================

    public Matrix4f getViewMatrix() {
        // Creates view matrix using position + direction
        // Equivalent to glm::lookAt
        return new Matrix4f().lookAt(
                position,
                new Vector3f(position).add(front), // where camera is looking
                up
        );
    }


    // ===================== KEYBOARD INPUT =====================

    public void processKeyboard(String direction, float deltaTime) {

        // Scale movement so it's frame-rate independent
        float velocity = movementSpeed * deltaTime;

        if (direction.equals("FORWARD")) {
            Vector3f xzVector = new Vector3f(front.x, 0.0f, front.z);
            position.add(new Vector3f(xzVector).mul(velocity));
        }

        if (direction.equals("BACKWARD")) {
            Vector3f xzVector = new Vector3f(front.x, 0.0f, front.z);
            position.sub(new Vector3f(xzVector).mul(velocity));
        }

        if (direction.equals("LEFT")) {
            position.sub(new Vector3f(right).mul(velocity));
        }

        if (direction.equals("RIGHT")) {
            position.add(new Vector3f(right).mul(velocity));
        }
    }


    // ===================== MOUSE MOVEMENT =====================

    public void processMouseMovement(float xoffset, float yoffset) {

        // Apply sensitivity scaling
        xoffset *= mouseSensitivity;
        yoffset *= mouseSensitivity;

        // Update rotation angles
        yaw += xoffset;
        pitch += yoffset;

        // Prevent looking straight up/down (avoids LookAt flip)
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;

        // Recalculate direction vectors
        updateCameraVectors();
    }


    // ===================== SCROLL (ZOOM) =====================

    public void processMouseScroll(float yoffset) {

        // Scroll changes field of view
        zoom -= yoffset;

        // Clamp FOV to avoid extreme distortion
        if (zoom < 1.0f) zoom = 1.0f;
        if (zoom > 45.0f) zoom = 45.0f;
    }


    // ===================== CORE MATH =====================

    private void updateCameraVectors() {

        // Convert yaw & pitch (angles) → direction vector

        Vector3f front = new Vector3f();

        front.x = (float) Math.cos(Math.toRadians(yaw)) *
                (float) Math.cos(Math.toRadians(pitch));

        front.y = (float) Math.sin(Math.toRadians(pitch));

        front.z = (float) Math.sin(Math.toRadians(yaw)) *
                (float) Math.cos(Math.toRadians(pitch));

        // Normalize so length = 1
        this.front = front.normalize();


        // Calculate RIGHT vector
        // cross(front, worldUp) gives perpendicular direction
        right = new Vector3f(this.front)
                .cross(worldUp)
                .normalize();

        // Calculate UP vector
        // cross(right, front) ensures orthogonal system
        up = new Vector3f(right)
                .cross(this.front)
                .normalize();
    }
}