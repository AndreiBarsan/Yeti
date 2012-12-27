#version 330

uniform vec4 	matColor;
uniform bool 	fogEnabled;
uniform vec4	fogColor;

smooth in float fogFactor;

out vec4 vFragColor;

void main(void) {
	vFragColor = matColor;
	
	if(fogEnabled) {
		vFragColor = mix(vFragColor, fogColor, fogFactor);
	}
}
