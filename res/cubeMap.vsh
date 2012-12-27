#version 330

in vec4 vVertex;

uniform mat4 mvpMatrix;

out vec3 vVaryingTexCoords;

void main(void) {
	vVaryingTexCoords = normalize(vVertex.xyz);
	gl_Position =  mvpMatrix * vVertex;
}
