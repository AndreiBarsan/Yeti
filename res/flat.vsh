#version 330

uniform mat4 mvpMatrix;

uniform bool	fogEnabled;
uniform float 	minFogDistance;
uniform float 	maxFogDistance;

smooth in vec4 vVertex; 

smooth out float fogFactor;

void main(void) {

	// Even if it's flat, it should still be affected by fog	
	if(fogEnabled) {
		float len = length(gl_Position);
		fogFactor = (len - minFogDistance) / (maxFogDistance - minFogDistance);
		fogFactor = clamp(fogFactor, 0, 1);
	}

	gl_Position =  mvpMatrix * vVertex;
}
