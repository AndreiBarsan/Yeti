#version 400 core

uniform mat4 mMatrix;

in vec4 vVertex; 

void main(void) {	
	gl_Position =  mMatrix * vVertex;
}
