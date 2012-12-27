#version 330


uniform vec4 ambientColor;
uniform vec4 diffuseColor;
uniform vec4 specularColor;

uniform int shininess;
uniform vec4 matColor;

// Texture stuff
uniform bool useTexture;
uniform bool useHeight;
uniform sampler2D colorMap;

// Fog
uniform bool 	fogEnabled;
uniform vec4	fogColor;

smooth in vec3 	vVaryingNormal;
smooth in vec3 	vVaryingLightDir;
smooth in vec2 	vVaryingTexCoords;
smooth in float vVaryingHeightBlend;
smooth in float fogFactor;

out vec4 vFragColor;

void main() {
	// Compute instensity
	float intensity = max(0.0f, dot(
		normalize(vVaryingNormal),
		normalize(vVaryingLightDir)
	));

	float lt = vVaryingHeightBlend - 0.55f;
	float factor = min(max(0, lt) * 10, 1.0);

	vFragColor 	= intensity * diffuseColor;
	
	// Ambient light
	vFragColor += ambientColor;
	
	if(useHeight) {
		vFragColor *= mix(texture(colorMap, vVaryingTexCoords),
						vec4(1.0, 1.0f, 1.0f, 1.0f), 
						pow(factor, 0.3f));
	} else {
		vFragColor *= texture(colorMap, vVaryingTexCoords);
	}
	
	vFragColor *= matColor;
	
	if(fogEnabled) {
		vFragColor = mix(vFragColor, fogColor, fogFactor);
	}
}