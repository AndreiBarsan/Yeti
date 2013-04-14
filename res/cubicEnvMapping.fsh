#version 400 core

uniform samplerCube cubeMap;
uniform sampler2D colorMap;

in vec3 vVaryingTexCoords;
in vec2 texCoords2d;

out vec4 vFragColor;

void main(void) {
	vFragColor = mix(texture(cubeMap, vVaryingTexCoords), 
		texture(colorMap, texCoords2d), 0.0f);
}
