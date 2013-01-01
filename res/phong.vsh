#version 330

uniform mat4 mvpMatrix;
uniform mat4 mvMatrix;
uniform mat4 vMatrix;

uniform mat3 normalMatrix;
uniform vec3 vLightPosition;
//uniform vec3 cameraPosition;

// Cubic attenuation parameters
//                         1
// att = -------------------------------------
//       k0 + k1 * d + k2 * d ^ 2 + k3 * d ^ 3
uniform float constantAt;
uniform float linearAt;
uniform float quadraticAt;
uniform float cubicAt;

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
smooth out float	attenuation;

// Cubic attenuation function
float att(float d) {
	return min(1.0f, 1.0f /
 ( constantAt + d * linearAt + d * d * quadraticAt + d * d * d * cubicAt));
}

void main() {
	// Surface normal in eye coords
	vVaryingNormal = normalMatrix * vNormal;

	vec4 vPosition4 = mvMatrix * vVertex;
	vec3 vPosition3 = vPosition4.xyz / vPosition4.w;
	
	vec3 tLightPos = (vMatrix * vec4(vLightPosition, 1.0)).xyz;

	// Diffuse light
	// Vector to light source
	vVaryingLightDir = normalize( tLightPos - vPosition3 );

	if(useTexture) {
		vVaryingTexCoords = vTexCoord;
	}
	
	// Transformed vertex
	//cameraSpaceVertex = vPosition3;
	
	// Light attenuation factor
	attenuation = att( length(tLightPos - vPosition3) );
	
	// Projected vertex
	gl_Position = mvpMatrix * vVertex;
	
	// Fog factor
	if(fogEnabled) {
		float len = length(gl_Position);
		fogFactor = (len - minFogDistance) / (maxFogDistance - minFogDistance);
		fogFactor = clamp(fogFactor, 0, 1);
	}	
}