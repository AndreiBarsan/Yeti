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

uniform bool useBump;

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

out vec3 	normal_ec;
out vec3 	normal_wc;
out vec3 	lightDir;
out vec2 	texCoords;
out float 	fogFactor;

out vec4 	vertPos_ec;
out vec4 	vertPos_wc;
out vec4 	lightPos_ec;
out vec4 	lightPos_wc;

out vec3 	spotDirection_wc;

out mat3 	mNTB;
out vec4 	vertPos_dmc;	// Used in shadow mapping

void main() {
	// Surface normal in eye coords
	//normal_ec = normalMatrix * vNormal;
	normal_wc = (mMatrix * vec4(vNormal, 0.0f)).xyz;

	vec4 vPosition4 = mMatrix * vVertex;
	vec3 vPosition3 = vPosition4.xyz / vPosition4.w;
	
	vec4 tLightPos4 = lightPosition;
	vec3 tLightPos  = tLightPos4.xyz / tLightPos4.w;

	if(lightPosition.w == 0.0f) {
		// Directional light
		lightDir = tLightPos4.xyz;
	} else {
		// Point / spot light
		// Vector to light source (do NOT normalize this!)
		lightDir = tLightPos - vPosition3;
	}
	
	if(useTexture) {
		texCoords = vTexCoord;
	}
	
	if(useBump) {
		mNTB[0] = vTang;
		mNTB[1] = vBinorm;
		mNTB[2] = normalize(vNormal);
		// seems to work without the normal matrix
		mNTB = mat3(mMatrix) * mNTB;
	}
	
	if(useShadows) {
		// Convert the vertex to shadowmap coordinates
		vertPos_dmc = mvpMatrixShadows * vVertex;
	}
	
	lightPos_wc = lightPosition;
	vertPos_wc = mMatrix * vVertex;
	
	lightPos_ec = vec4(tLightPos, 1.0f);
	vertPos_ec = vec4(vPosition3, 1.0f);
	
	// Do not use the vMatrix here - it's a direction not a position!
	// Do not use the normalMatrix here. It might seem it works, but once
	// you try to light a rotated object it blows up in your face. It's a
	// direction, not a normal. (not mathematically different, but still 
	// different in our case)
	spotDirection_wc = spotDirection;	// Important! 
	
	// Projected vertex
	gl_Position = mvpMatrix * vVertex;
	
	// Fog factor
	if(fogEnabled) {
		float len = length(gl_Position);
		fogFactor = (len - minFogDistance) / (maxFogDistance - minFogDistance);
		fogFactor = clamp(fogFactor, 0, 1);
	}	
}