#version 330

uniform vec4 matColor;

// Texture stuff
uniform bool useTexture;
uniform sampler2D colorMap;

// Fog
uniform bool 	fogEnabled;
uniform vec4	fogColor;

in vec4 vVaryingColor;
in vec2 vVaryingTexCoords;
in float fogFactor;

out vec4 vFragColor;

void main() {

	vFragColor = vVaryingColor * matColor;

	if(fogEnabled) {
		vFragColor = mix(vFragColor, fogColor, fogFactor);
	}
}