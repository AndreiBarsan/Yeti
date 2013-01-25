#version 330

uniform samplerCube cubeMap;
// Gamma correction
uniform bool 	useGammaCorrection;
uniform float 	invGamma;

smooth in vec3 vVaryingTexCoords;

out vec4 vFragColor;

void main(void) {

	vFragColor = texture(cubeMap, vVaryingTexCoords);

	if(useGammaCorrection) {
		vFragColor = pow(vFragColor, vec4(invGamma) );
	}
}
