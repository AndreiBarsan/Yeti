#version 330

uniform mat4 mvpMatrix;
uniform mat4 mvMatrix;
uniform mat4 vMatrix;

uniform mat3 normalMatrix;
uniform vec3 vLightPosition;
//uniform vec3 cameraPosition;

uniform bool useTexture;

uniform bool	fogEnabled;
uniform float 	minFogDistance;
uniform float 	maxFogDistance;

in vec4 vVertex;
in vec3 vNormal;
in vec2 vTexCoord;

//smooth out vec3 	cameraSpaceVertex;
smooth out vec3 	vVaryingNormal;
smooth out vec3 	vVaryingLightDir;
smooth out vec2 	vVaryingTexCoords;
smooth out float 	fogFactor;
//smooth out float	attenuation;

smooth out vec4 	lightPos_ec;
smooth out vec4 	vertPos_ec;

void main() {
	// Surface normal in eye coords
	vVaryingNormal = normalMatrix * vNormal;

	vec4 vPosition4 = mvMatrix * vVertex;
	vec3 vPosition3 = vPosition4.xyz / vPosition4.w;
	
	vec3 tLightPos = (vMatrix * vec4(vLightPosition, 1.0)).xyz;

	// Diffuse light
	// Vector to light source
	vVaryingLightDir = tLightPos - vPosition3;

	if(useTexture) {
		vVaryingTexCoords = vTexCoord;
	}
	
	// Transformed vertex
	//cameraSpaceVertex = vPosition3;
	
	lightPos_ec = vec4(tLightPos, 1.0f);
	vertPos_ec = vec4(vPosition3, 1.0f);
	
	// Light attenuation factor
	
	
	// Projected vertex
	gl_Position = mvpMatrix * vVertex;
	
	// Fog factor
	if(fogEnabled) {
		float len = length(gl_Position);
		fogFactor = (len - minFogDistance) / (maxFogDistance - minFogDistance);
		fogFactor = clamp(fogFactor, 0, 1);
	}	
}