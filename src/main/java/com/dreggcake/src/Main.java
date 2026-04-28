package com.dreggcake.src;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

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

        // define viewport (rendering area)
        GL11.glViewport(0, 0, 800, 600);

        // update viewport when window resizes
        GLFW.glfwSetFramebufferSizeCallback(window, (win, width, height) -> {
            GL11.glViewport(0, 0, width, height);
        });

        // ======================= ALL OPENGL SETUP GOES BEFORE RENDER LOOP =======================

        // rectangle vertices (4 points)
        float[] vertices = {
                0.5f,  0.5f, 0.0f,   // top right
                0.5f, -0.5f, 0.0f,   // bottom right
                -0.5f, -0.5f, 0.0f,   // bottom left
                -0.5f,  0.5f, 0.0f    // top left
        };

        // indices → tells GPU how to form triangles using above vertices
        int[] indices = {
                0, 1, 3,  // triangle 1
                1, 2, 3   // triangle 2
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
        GL20.glVertexAttribPointer(
                0,                  // attribute location in shader (layout = 0)
                3,                  // 3 floats per vertex (x,y,z)
                GL11.GL_FLOAT,      // data type
                false,              // normalize? no
                3 * Float.BYTES,    // stride (size of one vertex)
                0                   // offset
        );

        // enable attribute 0 (disabled by default)
        GL20.glEnableVertexAttribArray(0);

        // unbind VAO (optional safety)
        GL30.glBindVertexArray(0);

        // free CPU-side memory (GPU already has copy)
        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(indexBuffer);

        // ======================= SHADERS =======================

        // vertex shader (runs per vertex)
        String vertexShaderSource =
                """
                        #version 330 core
                        layout (location = 0) in vec3 aPos; // input from VAO

                        void main()
                        {
                            gl_Position = vec4(aPos, 1.0); // final position
                        }
                        """;

        // create + compile vertex shader
        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexShaderSource);
        GL20.glCompileShader(vertexShader);

        // check compile errors
        if (GL20.glGetShaderi(vertexShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.out.println(GL20.glGetShaderInfoLog(vertexShader));
        }

        // fragment shader (runs per pixel)
        String fragmentShaderSource =
                """
                        #version 330 core
                        out vec4 FragColor; // output color

                        void main()
                        {
                            FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f); // orange
                        }
                        """;

        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentShaderSource);
        GL20.glCompileShader(fragmentShader);

        if (GL20.glGetShaderi(fragmentShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.out.println(GL20.glGetShaderInfoLog(fragmentShader));
        }

        // create shader program (links vertex + fragment)
        int shaderProgram = GL20.glCreateProgram();
        GL20.glAttachShader(shaderProgram, vertexShader);
        GL20.glAttachShader(shaderProgram, fragmentShader);
        GL20.glLinkProgram(shaderProgram);

        if (GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            System.out.println(GL20.glGetProgramInfoLog(shaderProgram));
        }

        // shaders no longer needed after linking
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);

        // ======================= RENDER LOOP =======================
        while (!GLFW.glfwWindowShouldClose(window)) {

            // handle input
            processInput(window);

            // clear screen (background color)
            GL11.glClearColor(0.2f, 0.1f, 0.3f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            // activate shader program
            GL20.glUseProgram(shaderProgram);

            // bind VAO (restores all state)
            GL30.glBindVertexArray(VAO);

            // draw using indices (6 indices → 2 triangles)
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
    }
}