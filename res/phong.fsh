#version 330

uniform vec4 globalAmbient;
uniform vec4 lightDiffuse;
uniform vec4 lightSpecular;

uniform int shininess;
uniform vec4 matDiffuse;
// TODO: uniform vec4 matSpecular;

// Cubic attenuation parameters
//                         1
// att = -------------------------------------
//       k0 + k1 * d + k2 * d ^ 2 + k3 * d ^ 3
uniform float constantAt;
uniform float linearAt;
uniform float quadraticAt;
uniform float cubicAt;

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
//smooth in float attenuation;

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
		cf += attenuation * vec3(fSpec) * lightSpecular.rgb;
	}
	
	// Color modulation
	vFragColor = vec4(ct * cf, at * af);
	
	// Add the fog to the mix
	if(fogEnabled) {
		vFragColor = mix(vFragColor, fogColor, fogFactor);
	}
	
	//vFragColor -= vFragColor;
	//vFragColor += vec4(attenuation, attenuation, attenuation, 1.0f);
	//vFragColor += vec4(vVaryingLightDir, 1.0f);
	//vFragColor += vec4(intensity, intensity, intensity, 1.0f);
}