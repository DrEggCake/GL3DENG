package com.dreggcake.src;

import com.dreggcake.src.shaders.Shader;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;

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

        // define viewport (rendering area)
        GL11.glViewport(0, 0, 800, 600);

        // update viewport when window resizes
        GLFW.glfwSetFramebufferSizeCallback(window, (win, width, height) -> {
            GL11.glViewport(0, 0, width, height);
        });

        // ======================= ALL OPENGL SETUP GOES BEFORE RENDER LOOP =======================

        float[] vertices = {
                // positions       // colors
                0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, // bottom right
                -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, // bottom left
                0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f // top
        };

        // indices → tells GPU how to form triangles using above vertices
        int[] indices = {
                0, 1, 2
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

        // position attribute (location 0)
        GL20.glVertexAttribPointer(
                0,
                3,
                GL11.GL_FLOAT,
                false,
                6 * Float.BYTES,
                0
        );
        GL20.glEnableVertexAttribArray(0);

        // color attribute (location 1)
        GL20.glVertexAttribPointer(
                1,
                3,
                GL11.GL_FLOAT,
                false,
                6 * Float.BYTES,
                3 * Float.BYTES
        );
        GL20.glEnableVertexAttribArray(1);

        // unbind VAO (optional safety)
        GL30.glBindVertexArray(0);

        // free CPU-side memory (GPU already has copy)
        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(indexBuffer);

        // SHADER
        Shader shader = new Shader(
                "shaders/shader.vs",
                "shaders/shader.fs"
        );


        // ======================= RENDER LOOP =======================
        while (!GLFW.glfwWindowShouldClose(window)) {

            // handle input
            processInput(window);

            // clear screen (background color)
            GL11.glClearColor(0.2f, 0.1f, 0.3f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            // activate shader program
            shader.use();
            shader.setFloat("someUniform", 1.0f);


            // bind VAO (restores all state)
            GL30.glBindVertexArray(VAO);

            // draw using indices ( not supposed to be here i believe according to book but idk how to fix it)
            GL11.glDrawElements(
                    GL11.GL_TRIANGLES,   // draw triangles
                    3,                   // number of indices
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
    }
}