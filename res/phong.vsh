#version 330

uniform mat4 mvpMatrix;
uniform mat4 mvMatrix;
uniform mat4 vMatrix;

uniform mat3 vMatrix3x3;

uniform mat3 normalMatrix;
uniform vec4 vLightPosition;
uniform vec3 spotDirection;
uniform bool useTexture;

uniform bool useBump;

uniform bool 	fogEnabled;
uniform float 	minFogDistance;
uniform float 	maxFogDistance;

uniform bool 	useShadows;
uniform mat4 	mvpMatrixShadows;

layout(location = 0) in vec4 vVertex;
layout(location = 1) in vec3 vNormal;
layout(location = 2) in vec2 vTexCoord;

smooth out vec3 	vVaryingNormal;
smooth out vec3 	vVaryingLightDir;
smooth out vec2 	vVaryingTexCoords;
smooth out float 	fogFactor;

smooth out vec4 	vertPos_ec;
smooth out vec4 	vertPos_wc;
smooth out vec4 	lightPos_ec;
smooth out vec3 	spotDirection_ec;

smooth out mat3 	mNTB;

smooth out vec4 	vertPos_dmc;	// Used in shadow mapping

void main() {
	// Surface normal in eye coords
	vVaryingNormal = normalMatrix * vNormal;

	vec4 vPosition4 = mvMatrix * vVertex;
	vec3 vPosition3 = vPosition4.xyz / vPosition4.w;
	
	vec4 tLightPos4 = vMatrix * vLightPosition;
	vec3 tLightPos  = tLightPos4.xyz / tLightPos4.w;

	if(vLightPosition.w == 0.0f) {
		// Directional light
		vVaryingLightDir = tLightPos4.xyz;
	} else {
		// Point light
		// Vector to light source (do NOT normalize this!)
		vVaryingLightDir = tLightPos - vPosition3;
	}
	
	if(useTexture) {
		vVaryingTexCoords = vTexCoord;
	}
	
	if(useBump) {
		vec3 vNormal = normalize(vNormal);
		vec3 vTang = normalize(vec3(-vNormal.z, 0, vNormal.x));
		if( vNormal.z == vNormal.x) vTang = vec3 (1.0, 0.0, 0.0);
		vec3 vBinorm = normalize(cross(vTang, vNormal));
		mNTB[0]=vTang; mNTB[1]=vBinorm; mNTB[2]=vNormal;
		mNTB = normalMatrix * mNTB;
	}
	
	if(useShadows) {
		// Convert the vertex to shadowmap coordinates
		vertPos_dmc = mvpMatrixShadows * vVertex;
	}
	
	lightPos_ec = vec4(tLightPos, 1.0f);
	vertPos_ec = vec4(vPosition3, 1.0f);
	
	// Do not use the vMatrix here - it's a direction not a position!
	// Do not use the normalMatrix here. It might seem it works, but once
	// you try to light a rotated object it blows up in your face. It's a
	// direction, not a normal. (not mathematically different, but still 
	// different in our case)
	spotDirection_ec = vMatrix3x3 * spotDirection;	// Important! 
	
	// Projected vertex
	gl_Position = mvpMatrix * vVertex;
	
	// Fog factor
	if(fogEnabled) {
		float len = length(gl_Position);
		fogFactor = (len - minFogDistance) / (maxFogDistance - minFogDistance);
		fogFactor = clamp(fogFactor, 0, 1);
	}	
}