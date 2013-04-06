#version 400 core

uniform bool sampleTexture;
uniform sampler2D colorMap;

out vec4 vFragColor;

void main(void) {
	vFragColor = vec4(1.0);
}
