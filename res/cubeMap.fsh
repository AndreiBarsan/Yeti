#version 400 core

uniform bool 		samplingCube;
uniform samplerCube cubeMap;
uniform sampler2D 	derpMap;

smooth in vec3 vVaryingTexCoords;

out vec4 vFragColor;

void main(void) {
	
	if(samplingCube) {
		vFragColor = texture(cubeMap, vVaryingTexCoords);
	}
	else {
		vFragColor = texture(derpMap, vVaryingTexCoords.xy);
	}
}
