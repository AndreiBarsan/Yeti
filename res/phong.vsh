#version 330

uniform mat4 mvpMatrix;
uniform mat4 mvMatrix;
uniform mat3 mvMatrix3;	// TODO: remove me if needed
uniform mat4 vMatrix;


uniform mat3 normalMatrix;
uniform vec3 vLightPosition;
uniform vec3 spotDirection;
uniform bool useTexture;

uniform bool useBump;

uniform bool	fogEnabled;
uniform float 	minFogDistance;
uniform float 	maxFogDistance;

layout (location = 0) in vec4 vVertex;
layout (location = 1) in vec3 vNormal;
layout (location = 2) in vec2 vTexCoord;
layout (location = 3) in vec3 vTangent;
layout (location = 4) in vec3 vBitangent;

smooth out vec3 	vVaryingNormal;
smooth out vec3 	vVaryingLightDir;
smooth out vec2 	vVaryingTexCoords;
smooth out float 	fogFactor;

smooth out vec4 	vertPos_ec;
smooth out vec4 	vertPos_wc;
smooth out vec4 	lightPos_ec;
smooth out vec3 	spotDirection_ec;

smooth out mat3 	mNTB;

void main() {
	// Surface normal in eye coords
	vVaryingNormal = normalMatrix * vNormal;

	vec4 vPosition4 = mvMatrix * vVertex;
	vec3 vPosition3 = vPosition4.xyz / vPosition4.w;
	
	vec4 tLightPos4 = vMatrix * vec4(vLightPosition, 1.0);
	vec3 tLightPos  = tLightPos4.xyz / tLightPos4.w;

	// Diffuse light
	// Vector to light source (do NOT normalize this!)
	vVaryingLightDir = tLightPos - vPosition3;

	if(useTexture) {
		vVaryingTexCoords = vTexCoord;
	}
	
	if(useBump) {
		vec3 vNormal = normalize(vNormal);
		vec3 vTang = normalize(vec3(-vNormal.z,0,vNormal.x));
		if( vNormal.z == vNormal.x) vTang = vec3 (1.0,0.0,0.0);
		vec3 vBinorm = normalize(cross(vTang,vNormal));
		mNTB[0]=vTang; mNTB[1]=vBinorm; mNTB[2]=vNormal;
		mNTB = normalMatrix * mNTB;
	}
	
	lightPos_ec = vec4(tLightPos, 1.0f);
	vertPos_ec = vec4(vPosition3, 1.0f);
	
	// Transform the light direction (for spotlights) 
	vec4 spotDirection_ec4 = vec4(spotDirection, 1.0f);
	spotDirection_ec = spotDirection_ec4.xyz / spotDirection_ec4.w; 
	// Do not use the vMatrix here - it's a direction not a position!
	spotDirection_ec = normalMatrix * spotDirection;	// Important! 
	
	// Projected vertex
	gl_Position = mvpMatrix * vVertex;
	
	// Fog factor
	if(fogEnabled) {
		float len = length(gl_Position);
		fogFactor = (len - minFogDistance) / (maxFogDistance - minFogDistance);
		fogFactor = clamp(fogFactor, 0, 1);
	}	
}