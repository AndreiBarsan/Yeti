#version 330

uniform vec4 globalAmbient;
uniform vec4 lightDiffuse;
uniform vec4 lightSpecular;

uniform int shininess;
uniform vec4 matDiffuse;
// TODO: uniform vec4 matSpecular;

uniform float constantAt;
uniform float linearAt;
uniform float quadraticAt;
uniform float cubicAt;

// Texture stuff
uniform sampler2D colorMap;
uniform sampler2D colorMapB;

// Fog
uniform bool 	fogEnabled;
uniform vec4	fogColor;

smooth in vec3 	vVaryingNormal;
smooth in vec3 	vVaryingLightDir;
smooth in vec2 	vVaryingTexCoords;
smooth in float fogFactor;
smooth in float vVaryingHeightBlend;

smooth in vec4 	vertPos_ec;
smooth in vec4 	lightPos_ec;

out vec4 vFragColor;

// Cubic attenuation function
float att(float d) {
	float den = constantAt + d * linearAt + d * d * quadraticAt + d * d * d * cubicAt;

	if(den == 0.0f) {
		return 1.0f;
	}
	
	return min(1.0f, 1.0f / den);
}

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
	float attenuation = att( length(lightPos_ec - vertPos_ec) );
	
	cf = globalAmbient.rgb + attenuation * intensity * lightDiffuse.rgb * matDiffuse.rgb;	
	af = lightDiffuse.a * matDiffuse.a;

	float lt = vVaryingHeightBlend;
	float factor = min(max(0, lt) * 10, 1.0);
	texel = mix(texture2D(colorMap, 	vVaryingTexCoords),
				texture2D(colorMapB, 	vVaryingTexCoords),
				factor);
	
	ct = texel.rgb;
	at = texel.a;
	
	if(intensity > 0.0f && shininess > 0.0f) {
		// Specular light
		//  - 	added *after* the texture color is multiplied so that
		//		we get a truly shiny result
		vec3 vReflection = normalize(reflect(
			-normalize(vVaryingLightDir),
		 	 normalize(vVaryingNormal)));
	
		float spec = max(0.0, dot(normalize(vVaryingNormal), vReflection));
		float fSpec = pow(spec, shininess) * lightSpecular.a;
		cf += attenuation * vec3(fSpec) * lightSpecular.rgb;
	}
	
	// Color modulation
	vFragColor = vec4(ct * cf, at * af);
	
	// Add the fog to the mix
	if(fogEnabled) {
		vFragColor = mix(vFragColor, fogColor, fogFactor);
	}
}