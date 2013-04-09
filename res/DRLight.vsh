#version 400 core

layout(location = 0) in vec4 vVertex;

uniform mat4 mvpMatrix;

void main(void) {
	gl_Position =  mvpMatrix * vVertex;
}
