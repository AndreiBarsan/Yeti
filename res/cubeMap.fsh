#version 400 core

uniform samplerCube cubeMap;
uniform bool 		useGammaCorrection;
uniform float 		invGamma;

in vec3 		vVaryingTexCoords;

out vec4 			vFragColor;

void main(void) {	
	vFragColor = texture(cubeMap, vVaryingTexCoords);

	if(useGammaCorrection) {
		vFragColor = pow(vFragColor, vec4(invGamma) );
	}
}
