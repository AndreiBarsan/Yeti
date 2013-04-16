#version 400 core

uniform mat4 mvpMatrix;
uniform mat4 mvMatrix;
uniform mat4 mMatrix;
uniform mat4 vMatrix;

uniform mat3 vMatrix3x3;

uniform mat3 normalMatrix;
uniform vec4 lightPosition;
uniform vec3 spotDirection;
uniform bool useTexture;

uniform bool 	useBump;

uniform bool 	fogEnabled;
uniform float 	minFogDistance;
uniform float 	maxFogDistance;

uniform bool 	useShadows;
uniform bool 	samplingCube;
uniform mat4 	mvpMatrixShadows;

// 0 ... 1, where 0 is the start position and 1 the end position
uniform float animationIndex;  
in vec4 inPositionStart;
in vec3 inNormalStart;
in vec3 inTangStart;
in vec3 inBinormStart;

in vec4 inPositionEnd;
in vec3 inNormalEnd;
in vec3 inTangEnd;
in vec3 inBinormEnd;

in vec2 inTexCoords;

out vec3 	normal_wc;
out vec2 	texCoords;
out float 	fogFactor;

out vec3 	vertPos_wc;

out mat3 	mNTB;
out vec4 	vertPos_dmc;	// Used in shadow mapping

void main(void) {

	vec4 interpolatedPosition 	= mix(inPositionStart, inPositionEnd, animationIndex);
	vec3 interpolatedNormal		= mix(inNormalStart, inNormalEnd, animationIndex);
	vec3 interpolatedTangent	= mix(inTangStart, inTangEnd, animationIndex);
	vec3 interpolatedBinormal	= mix(inBinormStart, inBinormEnd, animationIndex);

	normal_wc = (mMatrix * vec4(interpolatedNormal, 0.0f)).xyz;
	
	vec4 vPosition4 = mvMatrix * interpolatedPosition;

	if(useTexture) {
		texCoords = inTexCoords;
	}
	
	if(useBump) {
		mNTB[0] = interpolatedTangent;
		mNTB[1] = interpolatedBinormal;
		mNTB[2] = normalize(interpolatedNormal);
		mNTB = mat3(mMatrix) * mNTB;
	}
	
	if(useShadows) {
		// Convert the vertex to shadowmap coordinates
		vertPos_dmc = mvpMatrixShadows * interpolatedPosition;
	}
	
	vertPos_wc = (mMatrix * interpolatedPosition).xyz;	
	gl_Position =  mvpMatrix * interpolatedPosition;
	
	if(fogEnabled) {
		float len = length(gl_Position);
		fogFactor = (len - minFogDistance) / (maxFogDistance - minFogDistance);
		fogFactor = clamp(fogFactor, 0, 1);
	}
}
