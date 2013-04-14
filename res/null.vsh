#version 400 core

uniform mat4 mvpMatrix;

in vec4 vVertex;

void main(void) {
	gl_Position =  mvpMatrix * vVertex;
}
