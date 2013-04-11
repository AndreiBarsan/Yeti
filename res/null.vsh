#version 400 core

uniform mat4 mvpMatrix;

layout(location = 0) in vec4 vVertex;

void main(void) {
	gl_Position =  mvpMatrix * vVertex;
}
