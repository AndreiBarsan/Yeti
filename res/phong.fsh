#version 400 core

const float bias = 0.025f;

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

// Normal mapping
uniform bool useBump;
uniform sampler2D normalMap;

// Shadow mapping
uniform bool 	useShadows;
uniform sampler2D shadowMap;

// Fog
uniform bool 	fogEnabled;
uniform vec4 	fogColor;

smooth in vec3 	vVaryingNormal;
smooth in vec3 	vVaryingLightDir;
smooth in vec2 	vVaryingTexCoords;
smooth in float fogFactor;

smooth in vec4 	vertPos_wc;
smooth in vec4 	vertPos_ec;
smooth in vec4 	lightPos_ec;
smooth in vec3 	spotDirection_ec;

smooth in vec4 	vertPos_dmc;	// Used in shadow mapping

smooth in mat3 	mNTB;

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
 */
void main() {		
	vec3 ct, cf;
	vec4 texel;
	float at, af;

	if(useTexture) {
		texel = texture(colorMap, vVaryingTexCoords); 
	} else {
		texel = vec4(1.0f);
	}

	ct = texel.rgb;
	at = texel.a;

	vec3 nNormal = normalize(vVaryingNormal);
	vec3 nLightDir = normalize(vVaryingLightDir);
	
	// TODO: employ #ifdefs and perform shader generation instead
	vec3 mapNormal;
	if(useBump) {
		vec3 vBump = 2.0f * texture2D(normalMap, vVaryingTexCoords).rgb - 1.0f;
		vBump = normalize(mNTB * vBump);
		nNormal = vBump;
	}
	
	float visibility = 1.0f;
	
	if(useShadows) {
		vec4 sc4 = vertPos_dmc / vertPos_dmc.w;
		vec2 sc  = vertPos_dmc.xy;

		if( sc4.w == 0 ) {
			visibility = 1.0f;
		} else if(sc.x < 0 || sc.x > 1 || sc.y < 0 || sc.y > 1) {
			visibility = 1.0f;
		} else {
			float shadow = texture( shadowMap, sc ).z;
			if ( shadow + bias  <  sc4.z) {
    			visibility = 0.33f;
			}
		}
	}
	
	float intensity = computeIntensity(nNormal, nLightDir);
	intensity *= visibility;	
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
	
	//vFragColor -= vFragColor;
	//vFragColor += vec4(vertexTangent_cameraspace, 1.0f);
	//vFragColor += vec4(intensity, intensity, intensity, 1.0f);
	//vFragColor += vec4(texture(shadowMap, vertPos_dmc.xy ).z);
	//vFragColor += vec4(texture(shadowMap, vVaryingTexCoords));
	//vFragColor += vec4(vertPos_dmc.z);
	//vFragColor.a = 1.0f;
}