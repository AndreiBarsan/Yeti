#version 330

uniform mat4 mvpMatrix;
uniform mat4 mvMatrix;
uniform mat4 pMatrix;

uniform mat3 normalMatrix;
uniform vec3 cameraPosition;

uniform bool	fogEnabled;
uniform float 	minFogDistance;
uniform float 	maxFogDistance;

in vec4 vVertex;
in vec3 vNormal;

smooth out float 	fogFactor;

void main() {

	gl_Position = mvpMatrix * vVertex;	

	if(fogEnabled) {
		float len = length(gl_Position);
		fogFactor = (len - minFogDistance) / (maxFogDistance - minFogDistance);
		fogFactor = clamp(fogFactor, 0, 1);
	}
	
	vec3 eyeNormal = normalMatrix * vNormal;
	
	float distanceToCamera = min(length(gl_Position.xyz - cameraPosition), 20);
	float df = distanceToCamera * 0.00035;
	gl_Position += vec4(eyeNormal, 1.0) * df;
}