#version 330

out vec4 vFragColor;

uniform samplerCube cubeMap;
in vec3 vVaryingTexCoords;

void main(void) {
	vFragColor = texture(cubeMap, vVaryingTexCoords);
}
