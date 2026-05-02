#version 330 core

layout (location = 0) in vec3 aPos;   // position attribute 0
layout (location = 1) in vec3 aColor; // color attribute 1
layout (location = 2) in vec2 aTexCoord; // texture attribute 2

out vec3 ourColor;
out vec2 TexCoord;

uniform mat4 transform;

void main()
{
    gl_Position = transform * vec4(aPos, 1.0);
    ourColor = aColor;
    TexCoord = aTexCoord;

}