package com.dreggcake.src.shaders;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;

public class Shader {

    public int ID; // same as unsigned int ID

    public Shader(String vertexPath, String fragmentPath) {

        // 1. READ FILES (equivalent to ifstream + stringstream)
        String vertexCode = "";
        String fragmentCode = "";

            vertexCode = loadResource(vertexPath);
            fragmentCode = loadResource(fragmentPath);

        // 2. COMPILE SHADERS
        int vertex, fragment;
        int success;

        // Vertex Shader
        vertex = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertex, vertexCode);
        GL20.glCompileShader(vertex);

        success = GL20.glGetShaderi(vertex, GL20.GL_COMPILE_STATUS);
        if (success == GL20.GL_FALSE) {
            String infoLog = GL20.glGetShaderInfoLog(vertex);
            System.out.println("ERROR::SHADER::VERTEX::COMPILATION_FAILED\n" + infoLog);
        }

        // Fragment Shader
        fragment = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragment, fragmentCode);
        GL20.glCompileShader(fragment);

        success = GL20.glGetShaderi(fragment, GL20.GL_COMPILE_STATUS);
        if (success == GL20.GL_FALSE) {
            String infoLog = GL20.glGetShaderInfoLog(fragment);
            System.out.println("ERROR::SHADER::FRAGMENT::COMPILATION_FAILED\n" + infoLog);
        }

        // SHADER PROGRAM
        ID = GL20.glCreateProgram();
        GL20.glAttachShader(ID, vertex);
        GL20.glAttachShader(ID, fragment);
        GL20.glLinkProgram(ID);

        success = GL20.glGetProgrami(ID, GL20.GL_LINK_STATUS);
        if (success == GL20.GL_FALSE) {
            String infoLog = GL20.glGetProgramInfoLog(ID);
            System.out.println("ERROR::SHADER::PROGRAM::LINKING_FAILED\n" + infoLog);
        }

        // DELETE SHADERS
        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);
    }

    private static String loadResource(String path) {
        try (InputStream is = Shader.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + path);
            }
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + path, e);
        }
    }

    // use()
    public void use() {
        GL20.glUseProgram(ID);
    }

    // setBool
    public void setBool(String name, boolean value) {
        GL20.glUniform1i(GL20.glGetUniformLocation(ID, name), value ? 1 : 0);
    }

    // setInt
    public void setInt(String name, int value) {
        GL20.glUniform1i(GL20.glGetUniformLocation(ID, name), value);
    }

    // setFloat
    public void setFloat(String name, float value) {
        GL20.glUniform1f(GL20.glGetUniformLocation(ID, name), value);
    }

    public void setVec3(String name, Vector3f value){
        GL20.glUniform3f(GL20.glGetUniformLocation(ID, name),
                value.x,
                value.y,
                value.z);
    }
}