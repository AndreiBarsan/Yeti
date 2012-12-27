#version 330

uniform mat4 mvpMatrix;

out vec2 vVaryingTexCoords;

in vec4 vVertex;
in vec2 vTexCoord;

void main(void) {
	vVaryingTexCoords = vTexCoord;
	gl_Position =  mvpMatrix * vVertex;
}
