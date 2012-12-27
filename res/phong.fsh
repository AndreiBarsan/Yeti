#version 330

// These will soon be
uniform vec4 ambientColor;
uniform vec4 diffuseColor;
uniform vec4 specularColor;
// ...arrays!

uniform int shininess;
uniform vec4 matColor;

// Texture stuff
uniform bool useTexture;
uniform sampler2D colorMap;

// Fog
uniform bool 	fogEnabled;
uniform vec4	fogColor;

smooth in vec3 	vVaryingNormal;
smooth in vec3 	vVaryingLightDir;
smooth in vec2 	vVaryingTexCoords;
smooth in float fogFactor;

out vec4 vFragColor;

/**
 *	Phong per-pixel lighting shading model.
 * 	Implements basic texture mapping and fog.
 */
void main() {
	// Compute instensity
	float intensity = max(0.0f, dot(
		normalize(vVaryingNormal),
		normalize(vVaryingLightDir)
	));

	vFragColor 	= intensity * diffuseColor;
	
	// Ambient light
	vFragColor += ambientColor;	
	
	// Apply ze texture
	if(useTexture) {
		vFragColor *= texture(colorMap, vVaryingTexCoords);
	}
	
	vFragColor *= matColor;

	// Specular light
	//  - 	added *after* the texture color is multiplied so that
	//		we get a truly shiny result
	vec3 vReflection = normalize(reflect(
		-normalize(vVaryingLightDir),
		 normalize(vVaryingNormal)));
	
	float spec = max(0.0, dot(normalize(vVaryingNormal), vReflection));
	if(intensity != 0) {
		float fSpec = pow(spec, shininess) * specularColor.a;
		vFragColor.rgb += vec3(fSpec, fSpec, fSpec) * specularColor.rgb;
	}
	
	if(fogEnabled) {
		vFragColor = mix(vFragColor, fogColor, fogFactor);
	}
}