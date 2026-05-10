package com.dreggcake.src;

import com.dreggcake.src.shaders.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class Main {

    static float deltaTime = 0.0f;
    // time between current frame and previous frame

    static float lastFrame = 0.0f;
    // stores previous frame time

    static Camera camera = new Camera(
            new Vector3f(0.0f, 0.0f, 3.0f)
    );

    static float lastX = 400;
    static float lastY = 300;

    static boolean firstMouse = true;

    static Vector3f lightPos = new Vector3f(
            1.2f,
            1.0f,
            2.0f
    );

    public static void main(String[] args) {

        // print GLFW errors in console
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        // tell GLFW we want OpenGL 3.3 core profile
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(
                GLFW.GLFW_OPENGL_PROFILE,
                GLFW.GLFW_OPENGL_CORE_PROFILE
        );

        long window = GLFW.glfwCreateWindow(
                800,
                600,
                "LearnOpenGL",
                0,
                0
        );

        if (window == MemoryUtil.NULL) {
            System.out.println("Failed to create GLFW window");
            GLFW.glfwTerminate();
            System.exit(-1);
        }

        // make OpenGL context active
        GLFW.glfwMakeContextCurrent(window);

        // initialize LWJGL OpenGL bindings
        GL.createCapabilities();

        GL11.glViewport(0, 0, 800, 600);

        // update viewport when window resizes
        GLFW.glfwSetFramebufferSizeCallback(
                window,
                (win, width, height) -> GL11.glViewport(0, 0, width, height)
        );

        // hide cursor and lock it to window
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED
        );

        GLFW.glfwSetCursorPosCallback(window, Main::mouseCallback
        );

        GLFW.glfwSetScrollCallback(window, Main::scrollCallback
        );

        // enables proper 3D depth rendering
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        // VERTEX DATA
        // cube vertices
        // ONLY positions now (x y z)
        // no texture coords

        float[] vertices = {

                // back face
                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                -0.5f, 0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,

                // front face
                -0.5f, -0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,
                -0.5f, -0.5f, 0.5f,

                // left face
                -0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,

                // right face
                0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,

                // bottom face
                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, 0.5f,
                0.5f, -0.5f, 0.5f,
                -0.5f, -0.5f, 0.5f,
                -0.5f, -0.5f, -0.5f,

                // top face
                -0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, -0.5f,
                0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, 0.5f,
                -0.5f, 0.5f, -0.5f
        };

        // VBO stores vertex data on GPU
        int VBO = GL15.glGenBuffers();

        // bind VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO
        );

        // allocate native memory to prepare vertex data for sending
        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);

        vertexBuffer.put(vertices).flip();

        // send vertex data to GPU
        GL15.glBufferData(
                GL15.GL_ARRAY_BUFFER,
                vertexBuffer,
                GL15.GL_STATIC_DRAW
        );

        // VAO stores:
        // - vertex attribute config
        // - which VBO is used

        int cubeVAO = GL30.glGenVertexArrays();

        GL30.glBindVertexArray(cubeVAO);

        // bind VBO while configuring VAO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO
        );

        // tell OpenGL how vertex data is laid out

        GL20.glVertexAttribPointer(
                0,
                3,
                GL11.GL_FLOAT,
                false,
                3 * Float.BYTES,
                0
        );

        // enable attribute
        GL20.glEnableVertexAttribArray(0);

        GL30.glBindVertexArray(0);

        // ===================== LIGHT VAO =====================

        // separate VAO for lamp cube
        int lightCubeVAO = GL30.glGenVertexArrays();

        GL30.glBindVertexArray(lightCubeVAO);

        GL15.glBindBuffer(
                GL15.GL_ARRAY_BUFFER,
                VBO
        );

        GL20.glVertexAttribPointer(
                0,
                3,
                GL11.GL_FLOAT,
                false,
                3 * Float.BYTES,
                0
        );

        GL20.glEnableVertexAttribArray(0);

        GL30.glBindVertexArray(0);

        // free CPU memory
        // GPU already has a copy
        MemoryUtil.memFree(vertexBuffer);

        Shader lightingShader = new Shader(
                "/shaders/colors/colors.vert",
                "/shaders/colors/colors.frag"
        );

        Shader lightCubeShader = new Shader(
                "/shaders/light_cube/light_cube.vert",
                "/shaders/light_cube/light_cube.frag"
        );

        lightingShader.use();

        lightingShader.setVec3(
                "objectColor",
                new Vector3f(1.0f, 0.5f, 0.31f)
        );

        lightingShader.setVec3(
                "lightColor",
                new Vector3f(1.0f, 1.0f, 1.0f)
        );

        // reusable matrix buffer
        FloatBuffer matrixBuffer =
                MemoryUtil.memAllocFloat(16);

        // ===================== RENDER LOOP =====================

        while (!GLFW.glfwWindowShouldClose(window)) {

            float currentFrame =
                    (float) GLFW.glfwGetTime();

            deltaTime = currentFrame - lastFrame;
            lastFrame = currentFrame;

            processInput(window);

            GL11.glClearColor(
                    0.1f,
                    0.1f,
                    0.1f,
                    1.0f
            );

            GL11.glClear(
                    GL11.GL_COLOR_BUFFER_BIT |
                            GL11.GL_DEPTH_BUFFER_BIT
            );

            // ================= MATRICES =================

            Matrix4f projection = new Matrix4f()
                    .perspective(
                            (float) Math.toRadians(camera.zoom),
                            800.0f / 600.0f,
                            0.1f,
                            100.0f
                    );

            Matrix4f view =
                    camera.getViewMatrix();

            lightingShader.use();

            matrixBuffer.clear();

            projection.get(matrixBuffer);

            GL20.glUniformMatrix4fv(
                    GL20.glGetUniformLocation(
                            lightingShader.ID,
                            "projection"
                    ),
                    false,
                    matrixBuffer
            );

            matrixBuffer.clear();

            view.get(matrixBuffer);

            GL20.glUniformMatrix4fv(
                    GL20.glGetUniformLocation(
                            lightingShader.ID,
                            "view"
                    ),
                    false,
                    matrixBuffer
            );

            Matrix4f model = new Matrix4f();

            matrixBuffer.clear();

            model.get(matrixBuffer);

            GL20.glUniformMatrix4fv(
                    GL20.glGetUniformLocation(
                            lightingShader.ID,
                            "model"
                    ),
                    false,
                    matrixBuffer
            );

            // draw cube
            GL30.glBindVertexArray(cubeVAO);

            GL11.glDrawArrays(
                    GL11.GL_TRIANGLES,
                    0,
                    36
            );

            // LIGHT CUBE

            lightCubeShader.use();
            matrixBuffer.clear();
            projection.get(matrixBuffer);

            GL20.glUniformMatrix4fv(
                    GL20.glGetUniformLocation(lightCubeShader.ID, "projection"
                    ), false,
                    matrixBuffer
            );

            matrixBuffer.clear();

            view.get(matrixBuffer);

            GL20.glUniformMatrix4fv(
                    GL20.glGetUniformLocation(
                            lightCubeShader.ID,
                            "view"
                    ),
                    false,
                    matrixBuffer
            );

            Matrix4f lightModel = new Matrix4f()
                    .translate(lightPos)
                    .scale(0.2f);

            matrixBuffer.clear();

            lightModel.get(matrixBuffer);

            GL20.glUniformMatrix4fv(
                    GL20.glGetUniformLocation(
                            lightCubeShader.ID,
                            "model"
                    ),
                    false,
                    matrixBuffer
            );

            // draw lamp cube
            GL30.glBindVertexArray(lightCubeVAO);

            GL11.glDrawArrays(
                    GL11.GL_TRIANGLES,
                    0,
                    36
            );

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }

        GLFW.glfwTerminate();
    }

    public static void processInput(long window) {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {

            GLFW.glfwSetWindowShouldClose(
                    window,
                    true
            );
        }

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS)

            camera.processKeyboard(
                    "FORWARD",
                    deltaTime
            );

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS)

            camera.processKeyboard(
                    "BACKWARD",
                    deltaTime
            );

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS)
            camera.processKeyboard(
                    "LEFT",
                    deltaTime
            );

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS)

            camera.processKeyboard(
                    "RIGHT",
                    deltaTime
            );
    }

    public static void mouseCallback(
            long window,
            double xpos,
            double ypos
    ) {

        float x = (float) xpos;
        float y = (float) ypos;

        if (firstMouse) {
            lastX = x;
            lastY = y;
            firstMouse = false;
        }

        float xoffset = x - lastX;

        float yoffset = lastY - y;

        lastX = x;
        lastY = y;

        camera.processMouseMovement(
                xoffset,
                yoffset
        );
    }

    public static void scrollCallback(
            long window,
            double xoffset,
            double yoffset
    ) {

        camera.processMouseScroll(
                (float) yoffset
        );
    }
}