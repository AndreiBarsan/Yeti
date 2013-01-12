#version 400 core

uniform samplerCube cubeMap;
uniform sampler2D colorMap;

smooth in vec3 vVaryingTexCoords;
smooth in vec2 texCoords2d;

out vec4 vFragColor;

void main(void) {
	vFragColor = mix(texture(cubeMap, vVaryingTexCoords), 
		texture(colorMap, texCoords2d), 0.5f);

	vFragColor -= vFragColor * 0.99f;
	vFragColor += texture(colorMap, texCoords2d);
}
