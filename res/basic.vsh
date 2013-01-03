#version 330

uniform mat4 mvpMatrix;
uniform mat4 mvMatrix;
uniform mat3 normalMatrix;
uniform vec3 vLightPosition;

uniform vec4 ambientColor;
uniform vec4 diffuseColor;
uniform vec4 specularColor;

uniform int shininess;

uniform bool useTexture;

uniform bool	fogEnabled;
uniform float 	minFogDistance;
uniform float 	maxFogDistance;

in vec4 vVertex;
in vec3 vNormal;
in vec2 vTexCoord;

smooth out vec4 	vVaryingColor;
smooth out vec2 	vVaryingTexCoords;
smooth out float 	fogFactor;

/*
Gouraud shading model
*/
void main() {
	// Surface normal in eye coords
	// Why aren't we using the mvMatrix? ANSWERED IN OpenGL bible + that awesome tut
	vec3 vEyeNormal = normalMatrix * vNormal;

	vec4 vPosition4 = mvMatrix * vVertex;
	vec3 vPosition3 = vPosition4.xyz / vPosition4.w;
	
	// Diffuse light
	// Vector to light source
	vec3 vLightDir = normalize(vLightPosition - vPosition3);

	// Compute instensity
	float intensity = max(0.0f, dot(vEyeNormal, vLightDir));

	vVaryingColor 	= intensity * diffuseColor;
	
	// Ambient light
	vVaryingColor += ambientColor;	
	
	// Specular light
	vec3 vReflection = normalize(reflect(-vLightDir, vEyeNormal));
	float spec = max(0.0, dot(vEyeNormal, vReflection));
	if(intensity != 0) {
		float fSpec = pow(spec, shininess) * specularColor.a;
		vVaryingColor.rgb += vec3(fSpec, fSpec, fSpec) * specularColor.rgb;
	}
	
	if(useTexture) {
		vVaryingTexCoords = vTexCoord;
	}
	
	if(fogEnabled) {
		float len = length(gl_Position);
		fogFactor = (len - minFogDistance) / (maxFogDistance - minFogDistance);
		fogFactor = clamp(fogFactor, 0, 1);
	}

	gl_Position = mvpMatrix * vVertex;
}