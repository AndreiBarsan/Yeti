#version 400 core

uniform mat4 mMatrix;

in layout(location = 0) vec4 vVertex; 

void main(void) {	
	gl_Position =  mMatrix * vVertex;
}
