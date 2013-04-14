#version 330

uniform mat4 mvpMatrix;

uniform bool	fogEnabled;
uniform float 	minFogDistance;
uniform float 	maxFogDistance;

in vec4 vVertex; 

out float fogFactor;

void main(void) {
	gl_Position =  mvpMatrix * vVertex;

	// Even if it's flat, it should still be affected by fog	
	if(fogEnabled) {
		float len = length(gl_Position);
		fogFactor = (len - minFogDistance) / (maxFogDistance - minFogDistance);
		fogFactor = clamp(fogFactor, 0, 1);
	}
}
