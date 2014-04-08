#version 400 core

uniform mat4 mvpMatrix;
uniform mat4 mMatrix;

uniform mat4 biasMatrix;

uniform vec4 	lightPosition;
uniform vec3 	spotDirection;

uniform bool 	useTexture;
uniform bool 	useBump;

uniform bool 	fogEnabled;
uniform float 	minFogDistance;
uniform float 	maxFogDistance;

uniform bool 	useShadows;
uniform bool 	samplingCube;
uniform mat4 	mvpMatrixShadows;

in vec4 vVertex;
in vec3 vNormal;
in vec2 vTexCoord;
in vec3 vTang;
in vec3 vBinorm;

out vec3 	normal_wc;

out vec2 	texCoords;
// TODO: PER PIXEL MORON
out float 	fogFactor;

out vec3 	vertPos_wc;

out mat3 	mNTB;			// Used in normal mapping
out vec4 	vertPos_dmc;	// Used in shadow mapping

void main() {
	normal_wc = (mMatrix * vec4(vNormal, 0.0f)).xyz;

	vec4 vPosition4 = mMatrix * vVertex;

	if(useTexture) {
		texCoords = vTexCoord;
	}
	
	if(useBump) {
		mNTB[0] = vTang;
		mNTB[1] = vBinorm;
		mNTB[2] = normalize(vNormal);
		mNTB = mat3(mMatrix) * mNTB;
	}
	
	if(useShadows) {
		// Convert the vertex to shadowmap coordinates
		vertPos_dmc = mvpMatrixShadows * vVertex;
	}
	
	vertPos_wc = (mMatrix * vVertex).xyz;
	
	// Do not use the vMatrix here - it's a direction not a position!
	// Do not use the normalMatrix here. It might seem it works, but once
	// you try to light a rotated object it blows up in your face. It's a
	// direction, not a normal. (not mathematically different, but still 
	// different in our case)
	
	// See? See what happens when you get confused and think you *have* to compute
	// everything in eye coordinates? Just use world coords, and just don't do
	// anything in the VS.
	
	// Projected vertex
	gl_Position = mvpMatrix * vVertex;
	
	// Fog factor
	if(fogEnabled) {
		float len = length(gl_Position);
		fogFactor = (len - minFogDistance) / (maxFogDistance - minFogDistance);
		fogFactor = clamp(fogFactor, 0, 1);
	}	
}