#version 330

uniform vec4 globalAmbient;

// ADS shading model
uniform vec4 lightDiffuse;
uniform vec4 lightSpecular;
uniform float lightTheta;
uniform float lightPhi;
uniform float lightExponent;

uniform int shininess;
uniform vec4 matAmbient;
uniform vec4 matDiffuse;
uniform vec4 matSpecular;

// Cubic attenuation parameters
uniform float constantAt;
uniform float linearAt;
uniform float quadraticAt;
uniform float cubicAt;

// Texture stuff
uniform bool useTexture;
uniform sampler2D colorMap;

uniform bool useBump;
uniform sampler2D normalMap;

// Fog
uniform bool 	fogEnabled;
uniform vec4	fogColor;

smooth in vec3 	vVaryingNormal;
smooth in vec3 	vVaryingLightDir;
smooth in vec2 	vVaryingTexCoords;
smooth in float fogFactor;
smooth in vec4 	vertPos_ec;
smooth in vec4 	lightPos_ec;
smooth in vec3 	spotDirection_ec;

out vec4 vFragColor;

// Cubic attenuation function
float att(float d) {
	float den = constantAt + d * linearAt + d * d * quadraticAt + d * d * d * cubicAt;

	if(den == 0.0f) {
		return 1.0f;
	}
	
	return min(1.0f, 1.0f / den);
}

float computeIntensity(in vec3 nNormal, in vec3 nLightDir) {	
	float intensity = max(0.0f, dot(nNormal, nLightDir));
	float cos_outer_cone = lightTheta;
	float cos_inner_cone = lightPhi;
	float cos_inner_minus_outer = cos_inner_cone - cos_outer_cone;

	// If we are a point light
	if(lightTheta > 0.0f) {
		float cos_cur = dot(normalize(spotDirection_ec), -nLightDir);
		// d3d style smooth edge
		float spotEffect = clamp((cos_cur - cos_outer_cone) / 
	      						cos_inner_minus_outer, 0.0, 1.0);
		spotEffect = pow(spotEffect, lightExponent);
		intensity *= spotEffect;
	}	
	
	float attenuation = att( length(lightPos_ec - vertPos_ec) );
	intensity *= attenuation;

	return intensity;
}

/**
 *	Phong per-pixel lighting shading model.
 * 	Implements basic texture mapping and fog.
 */
void main() {		
	vec3 ct, cf;
	vec4 texel;
	float at, af;

	if(useTexture) {
		texel = texture2D(colorMap, vVaryingTexCoords); 
	} else {
		texel = vec4(1.0f);
	}

	ct = texel.rgb;
	at = texel.a;

	vec3 nNormal = normalize(vVaryingNormal);
	vec3 nLightDir = normalize(vVaryingLightDir);
	
	// TODO: employ IFDEFS and perform shader generation instead
	if(useBump) {
		// make nNormal (I think) be affected by the normal map
	}

	float intensity = computeIntensity(nNormal, nLightDir);	
	cf = matAmbient.rgb * globalAmbient.rgb + intensity * lightDiffuse.rgb * matDiffuse.rgb;	
	af = matAmbient.a * globalAmbient.a + lightDiffuse.a * matDiffuse.a;
	
	if(intensity > 0.0f && shininess > 0.0f) {
		// Specular light
		//  - 	added *after* the texture color is multiplied so that
		//		we get a truly shiny result
		vec3 vReflection = normalize(reflect(-nLightDir, nNormal));
		float spec = max(0.0, dot(nNormal, vReflection));
		float fSpec = pow(spec, shininess) * lightSpecular.a;
		cf += intensity * vec3(fSpec) * lightSpecular.rgb * matSpecular.rgb;
	}
	
	// Color modulation
	vFragColor = vec4(ct * cf, at * af);
	
	// Add the fog to the mix
	if(fogEnabled) {
		vFragColor = mix(vFragColor, fogColor, fogFactor);
	}
}