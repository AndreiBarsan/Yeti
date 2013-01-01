#version 330

uniform vec4 globalAmbient;
uniform vec4 lightDiffuse;
uniform vec4 lightSpecular;

uniform int shininess;
uniform vec4 matDiffuse;
// TODO: uniform vec4 matSpecular;

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
smooth in float attenuation;

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

	vec3 ct, cf;
	vec4 texel;
	float at, af;
	
	cf = attenuation * intensity * matDiffuse.rgb;	
	af = matDiffuse.a;
	if(useTexture) {
		texel = texture2D(colorMap, vVaryingTexCoords); 
	} else {
		texel = vec4(1.0f);
	}
	
	ct = texel.rgb;
	at = texel.a;
	
	if(intensity > 0.0f) {
		// Specular light
		//  - 	added *after* the texture color is multiplied so that
		//		we get a truly shiny result
		vec3 vReflection = normalize(reflect(
			-normalize(vVaryingLightDir),
		 	 normalize(vVaryingNormal)));
	
		float spec = max(0.0, dot(normalize(vVaryingNormal), vReflection));
		float fSpec = pow(spec, shininess) * lightSpecular.a;
		//cf += attenuation * vec3(fSpec) * lightSpecular.rgb;
	}
	
	// Color modulation
	vFragColor = vec4(ct * cf, at * af);
	
	// Add the fog to the mix
	if(fogEnabled) {
		vFragColor = mix(vFragColor, fogColor, fogFactor);
	}
}