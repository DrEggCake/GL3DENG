package com.dreggcake.src;

import com.dreggcake.src.shaders.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Main {

    static float deltaTime = 0.0f; // time b/w current frame and last frame
    static float lastFrame = 0.0f; // time of last frame

    static Camera camera = new Camera(new Vector3f(0.0f, 0.0f, 3.0f));

    // for mouse movement
    static float lastX = 400, lastY = 300;
    static boolean firstMouse = true;

    static float fov = 45.0f;

    public static void main(String[] args) {

        // optional but useful → prints GLFW errors
        GLFWErrorCallback.createPrint(System.err).set();

        // initialize GLFW (window system)
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // tell GLFW we want OpenGL 3.3 core profile
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        // create window (width, height, title)
        long window = GLFW.glfwCreateWindow(800, 600, "LearnOpenGL", 0, 0);

        // if failed → exit
        if (window == MemoryUtil.NULL) {
            System.out.println("Failed to create GLFW window");
            GLFW.glfwTerminate();
            System.exit(-1);
        }

        // make this window’s OpenGL context active on current thread
        GLFW.glfwMakeContextCurrent(window);

        // initialize OpenGL bindings (VERY IMPORTANT)
        GL.createCapabilities();
//        GLFW.glfwSwapInterval(1); // this is vsync

        // define viewport (rendering area)
        GL11.glViewport(0, 0, 800, 600);

        // update viewport when window resizes
        GLFW.glfwSetFramebufferSizeCallback(window, (win, width, height) -> {
            GL11.glViewport(0, 0, width, height);
        });

        // set mouse input
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        GLFW.glfwSetCursorPosCallback(window, Main::mouseCallback);

        GLFW.glfwSetScrollCallback(window, Main::scroll_callback);

        // ======================= ALL OPENGL SETUP GOES BEFORE RENDER LOOP =======================

        float[] vertices = {
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
                0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,

                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
                -0.5f, 0.5f, 0.5f, 0.0f, 1.0f,
                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,

                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                -0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

                0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

                -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
                0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
                0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
                -0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f
        };

        // indices → tells GPU how to form triangles using above vertices
        int[] indices = {
                0, 1, 3,
                1, 2, 3
        };

        // VBO → stores vertex data in GPU memory
        int VBO = GL15.glGenBuffers();

        // EBO → stores indices
//        int EBO = GL15.glGenBuffers(); we dont need it for now

        // VAO → stores ALL configuration (VBO + attribute setup + EBO)
        int VAO = GL30.glGenVertexArrays();

        // bind VAO → everything after this gets saved into it
        GL30.glBindVertexArray(VAO);

        // bind VBO to ARRAY_BUFFER target
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VBO);

        // allocate native memory for vertices (JVM → native)
        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip(); // put data + prepare for reading

        // send vertex data to GPU
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

//        // allocate memory for indices
//        IntBuffer indexBuffer = MemoryUtil.memAllocInt(indices.length);
//        indexBuffer.put(indices).flip();
//
//        // bind EBO (VERY IMPORTANT: must be while VAO is bound) // dont need this as well
//        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, EBO);
//
//        // send indices to GPU
//        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);

        // tell OpenGL how to interpret vertex data
        int stride = 5 * Float.BYTES;

        // position
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, 0);
        GL20.glEnableVertexAttribArray(0);

//        // color
//        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 3 * Float.BYTES);
//        GL20.glEnableVertexAttribArray(1); // we aint got no color data no more cuz of texture

        // texture coords
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 3 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);

        // unbind VAO (optional safety)
        GL30.glBindVertexArray(0);

        // free CPU-side memory (GPU already has copy)
        MemoryUtil.memFree(vertexBuffer);
//        MemoryUtil.memFree(indexBuffer);

        // SHADER
        Shader shader = new Shader(
                "/shaders/shader.vs",
                "/shaders/shader.fs"
        );

        // =============== LOADING TEXTURES ==============

        int texture1 = loadTexture("bricks.png");
        int texture2 = loadTexture("awesomeFace.png");

        FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(16);

        // tell opengl for each sampler to which texture unit it belongs to (only has to be done once)
        // -------------------------------------------------------------------------------------------
        shader.use();
        shader.setInt("texture1", 0);
        shader.setInt("texture2", 1);

        GL11.glEnable(GL11.GL_DEPTH_TEST);


        Vector3f[] cubePositions = {
                new Vector3f(0.0f, 0.0f, 0.0f),
                new Vector3f(2.0f, 5.0f, -15.0f),
                new Vector3f(-1.5f, -2.2f, -2.5f),
                new Vector3f(-3.8f, -2.0f, -12.3f),
                new Vector3f(2.4f, -0.4f, -3.5f),
                new Vector3f(-1.7f, 3.0f, -7.5f),
                new Vector3f(1.3f, -2.0f, -2.5f),
                new Vector3f(1.5f, 2.0f, -2.5f),
                new Vector3f(1.5f, 0.2f, -1.5f),
                new Vector3f(-1.3f, 1.0f, -1.5f)
        };

        // ======================= RENDER LOOP =======================
        while (!GLFW.glfwWindowShouldClose(window)) {

            float currentFrame = (float) GLFW.glfwGetTime();
            deltaTime = currentFrame - lastFrame;
            lastFrame = currentFrame;

            // handle input
            processInput(window);

            // clear screen (background color)
            GL11.glClearColor(0.2f, 0.1f, 0.3f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // bind textures on corresponding texture units
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL13.glBindTexture(GL11.GL_TEXTURE_2D, texture1);
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL13.glBindTexture(GL11.GL_TEXTURE_2D, texture2);

            // activate shader
            shader.use();

            Matrix4f view = camera.getViewMatrix();

            Matrix4f projection = new Matrix4f()
                    .perspective((float) Math.toRadians(camera.zoom),
                            800.0f/600.0f,
                            0.1f,
                            100.0f);

            int modelLoc = GL20.glGetUniformLocation(shader.ID, "model");
            int viewLoc = GL20.glGetUniformLocation(shader.ID, "view");
            int projectionLoc = GL20.glGetUniformLocation(shader.ID, "projection");


            // VIEW
            matrixBuffer.clear();
            view.get(matrixBuffer);
            GL20.glUniformMatrix4fv(viewLoc, false, matrixBuffer);

            // PROJECTION
            matrixBuffer.clear();
            projection.get(matrixBuffer);
            GL20.glUniformMatrix4fv(projectionLoc, false, matrixBuffer);


            // bind VAO (restores all state)
            GL30.glBindVertexArray(VAO);


            // draw without using indicies cuz we already defined it in long form ( idk ig?)

            for (int i = 0; i < 10; i++) {

                Matrix4f model = new Matrix4f()
                        .translate(cubePositions[i]);

                float angle = (float) Math.toRadians(20.0f * i);
                model.rotate(angle, 1.0f, 0.3f, 0.5f);

                matrixBuffer.clear();
                model.get(matrixBuffer);

                GL20.glUniformMatrix4fv(modelLoc, false, matrixBuffer);

                GL11.glDrawArrays(
                        GL11.GL_TRIANGLES,
                        0,
                        36
                );
            }

            // swap buffers (display result)
            GLFW.glfwSwapBuffers(window);

            // process events (keyboard, mouse)
            GLFW.glfwPollEvents();
        }

        // cleanup
        GLFW.glfwTerminate();
    }

    public static void processInput(long window) {
        // if ESC pressed → close window
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }
        float cameraSpeed = 2.5f * deltaTime; // adjust according to ur wish
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS)
            camera.processKeyboard("FORWARD", deltaTime);

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS)
            camera.processKeyboard("BACKWARD", deltaTime);

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS)
            camera.processKeyboard("LEFT", deltaTime);

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS)
            camera.processKeyboard("RIGHT", deltaTime);


    }

    private static int loadTexture(String fileName) {

        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);


        // wrapping
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        // filtering
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        // load image
        try (MemoryStack stack = MemoryStack.stackPush()) {

            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            // flip image like LearnOpenGL expects
            STBImage.stbi_set_flip_vertically_on_load(true);

            ByteBuffer data = STBImage.stbi_load(fileName, width, height, channels, 0);

            if (data != null) {

                int format;

                if (channels.get(0) == 3)
                    format = GL11.GL_RGB;
                else if (channels.get(0) == 4)
                    format = GL11.GL_RGBA;
                else
                    throw new RuntimeException("Unsupported format");

                GL11.glTexImage2D(
                        GL11.GL_TEXTURE_2D,
                        0,
                        format,
                        width.get(0),
                        height.get(0),
                        0,
                        format,
                        GL11.GL_UNSIGNED_BYTE,
                        data
                );

                GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            }
            STBImage.stbi_image_free(data);
        }
        return texture;
    }

    public static void mouseCallback(long window, double xpos, double ypos) {

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

        camera.processMouseMovement(xoffset, yoffset);
    }
    // glfw: whenever the mouse scroll wheel scrolls, this callback is called
    // ----------------------------------------------------------------------
    public static void scroll_callback(long window, double xoffset, double yoffset) {
        camera.processMouseScroll((float) yoffset);
    }
}