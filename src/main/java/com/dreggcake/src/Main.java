package com.dreggcake.src;

import com.dreggcake.src.shaders.Shader;
import org.joml.Matrix4f;
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

        // ======================= ALL OPENGL SETUP GOES BEFORE RENDER LOOP =======================

        float[] vertices = {
                // positions      // colors         // texture coords
                0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, // top right
                0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // bottom right
                -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, // bottom left
                -0.5f, 0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f // top left
        };

        // indices → tells GPU how to form triangles using above vertices
        int[] indices = {
                0, 1, 3,
                1, 2, 3
        };

        // VBO → stores vertex data in GPU memory
        int VBO = GL15.glGenBuffers();

        // EBO → stores indices
        int EBO = GL15.glGenBuffers();

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

        // allocate memory for indices
        IntBuffer indexBuffer = MemoryUtil.memAllocInt(indices.length);
        indexBuffer.put(indices).flip();

        // bind EBO (VERY IMPORTANT: must be while VAO is bound)
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, EBO);

        // send indices to GPU
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);

        // tell OpenGL how to interpret vertex data

        int stride = 8 * Float.BYTES;

        // position
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, 0);
        GL20.glEnableVertexAttribArray(0);

        // color
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, stride, 3 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);

        // texture coords
        GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, stride, 6 * Float.BYTES);
        GL20.glEnableVertexAttribArray(2);

        // unbind VAO (optional safety)
        GL30.glBindVertexArray(0);

        // free CPU-side memory (GPU already has copy)
        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(indexBuffer);

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

        // ======================= RENDER LOOP =======================
        while (!GLFW.glfwWindowShouldClose(window)) {
            // handle input
            processInput(window);

            // clear screen (background color)
            GL11.glClearColor(0.2f, 0.1f, 0.3f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            // bind textures on corresponding texture units
            GL30.glActiveTexture(GL13.GL_TEXTURE0);
            GL30.glBindTexture(GL11.GL_TEXTURE_2D, texture1);
            GL30.glActiveTexture(GL13.GL_TEXTURE1);
            GL30.glBindTexture(GL11.GL_TEXTURE_2D, texture2);

            // activate shader
            shader.use();

            Matrix4f model = new Matrix4f().rotate((float) Math.toRadians(-55.0f), 1.0f, 0.0f, 0.0f);

            Matrix4f view = new Matrix4f().translate(0.0f, 0.0f, -3.0f);

            Matrix4f projection = new Matrix4f()
                    .perspective((float)Math.toRadians(45.0f),
                            800.0f/600.0f,
                            0.1f,
                            100.0f
                    );

            int modelLoc = GL20.glGetUniformLocation(shader.ID, "model");
            int viewLoc = GL20.glGetUniformLocation(shader.ID, "view");
            int projectionLoc = GL20.glGetUniformLocation(shader.ID, "projection");

            // MODEL
            matrixBuffer.clear();
            model.get(matrixBuffer);
            GL20.glUniformMatrix4fv(modelLoc, false, matrixBuffer);

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


            // draw using indices ( not supposed to be here i believe according to book but idk how to fix it)
            GL11.glDrawElements(
                    GL11.GL_TRIANGLES,   // draw triangles
                    6,                   // number of indices
                    GL11.GL_UNSIGNED_INT,// type of indices
                    0                    // offset
            );

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

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }


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
                        GL11.GL_RGB,
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
}