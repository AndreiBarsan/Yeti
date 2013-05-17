#version 400

uniform sampler2D diffuseMap;
uniform sampler2D lightMap;

in vec2 vVaryingTexCoords;

out vec4 composedColor;

void main(void) {
	// Compose the albedo and the lighting
	composedColor = texture(diffuseMap, vVaryingTexCoords) *  texture(lightMap, vVaryingTexCoords); 
}