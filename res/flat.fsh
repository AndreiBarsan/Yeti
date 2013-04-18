#version 330

uniform vec4 	matColor;
uniform bool 	fogEnabled;
uniform vec4	fogColor;

in float fogFactor;

out vec4 vFragColor;

void main(void) {
	vFragColor = matColor * 0.33f;

	if(fogEnabled) {
		vFragColor = mix(vFragColor, fogColor, fogFactor);
	}
}
