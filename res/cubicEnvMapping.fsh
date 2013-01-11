#version 330

uniform samplerCube cubeMap;

smooth in vec3 vVaryingTexCoords;

out vec4 vFragColor;

void main(void) {
	vFragColor = texture(cubeMap, vVaryingTexCoords);
}
