#version 400 core

uniform mat4 	lightMVP;

layout(location = 0) in vec4 vVertex;

void main(void) {
	gl_Position =  lightMVP * vVertex;
}
