//#define SAMPLINGCUBE
#version 400 core

const float bias = 0.01f;
const float pFac = 2500.0f;

const vec2 pD[16] = vec2[]( 
   vec2( -0.94201624, -0.39906216 ), 
   vec2( 0.94558609, -0.76890725 ), 
   vec2( -0.094184101, -0.92938870 ), 
   vec2( 0.34495938, 0.29387760 ), 
   vec2( -0.91588581, 0.45771432 ), 
   vec2( -0.81544232, -0.87912464 ), 
   vec2( -0.38277543, 0.27676845 ), 
   vec2( 0.97484398, 0.75648379 ), 
   vec2( 0.44323325, -0.97511554 ), 
   vec2( 0.53742981, -0.47373420 ), 
   vec2( -0.26496911, -0.41893023 ), 
   vec2( 0.79197514, 0.19090188 ), 
   vec2( -0.24188840, 0.99706507 ), 
   vec2( -0.81409955, 0.91437590 ), 
   vec2( 0.19984126, 0.78641367 ), 
   vec2( 0.14383161, -0.14100790 ) 
);

uniform vec4 globalAmbient;

// ADS shading model
uniform vec4 	lightDiffuse;
uniform vec4 	lightSpecular;
uniform float 	lightTheta;
uniform float 	lightPhi;
uniform float 	lightExponent;
uniform vec3 	lightPosition;
uniform vec3    lightDirection;
uniform int 	lightType;

uniform mat4 	mMatrix;
uniform mat4 	biasMatrix;

uniform int shininess;
uniform vec4 matAmbient;
uniform vec4 matDiffuse;
uniform vec4 matSpecular;

uniform vec3 eyeWorldPos;

// Cubic attenuation parameters
uniform float constantAt;
uniform float linearAt;
uniform float quadraticAt;

// Texture stuff
uniform bool useTexture;
uniform sampler2D diffuseMap;

// Normal mapping
uniform bool useBump;
uniform sampler2D normalMap;

// Shadow mapping
uniform bool 		useShadows;
uniform int 		shadowQuality;
uniform bool 		samplingCube;
uniform samplerCube cubeShadowMap;		// Point lights
uniform sampler2D 	shadowMap;			// Spot & Directional lights

uniform float 		far;

// Gamma correction
uniform bool 	useGammaCorrection;
uniform float 	invGamma;

// Fog
uniform bool 	fogEnabled;
uniform vec4 	fogColor;

in vec3 	normal_wc;
in vec2 	texCoords;
in float 	fogFactor;

in vec3 	vertPos_wc;

in vec4 	vertPos_dmc;	// Used in shadow mapping
in mat3 	mNTB;			// Used in normal mapping

out vec4 vFragColor;

float rand(in vec3 seed3, in int index) {
	vec4 seed4 = vec4(seed3, index);
	float dot_product = dot(seed4, vec4(12.9898,78.233,45.164,94.673));
    return fract(sin(dot_product) * 43758.5453);
}

// Cubic attenuation function
float att(float d) {
	float den = constantAt + d * linearAt + d * d * quadraticAt;
	if(den == 0.0f) {
		return 1.0f;
	}
	return min(1.0f, 1.0f / den);
}


float computeIntensity(in vec3 nNormal, in vec3 nLightDir, in float NL) {	
	float intensity = max(0.0f, NL);

	// If we are a spot light
	if( lightType == 2 ) {
		float cos_outer_cone = lightTheta;
		float cos_inner_cone = lightPhi;
		float cos_inner_minus_outer = cos_inner_cone - cos_outer_cone;
		float cos_cur = dot(normalize(lightDirection), -nLightDir);
		// d3d style smooth edge
		float spotEffect = clamp((cos_cur - cos_outer_cone) / 
	      						cos_inner_minus_outer, 0.0, 1.0);
		spotEffect = pow(spotEffect, lightExponent);
		intensity *= spotEffect;
	}
	
	float attenuation = att( length(lightPosition - vertPos_wc) );
	intensity *= attenuation;

	return intensity;
}

float computeVisibilityCube(in float NL) {
	float visibility = 1.0f;
	// calculate vector from surface point to light position
	// (both positions are given in world space - since that's how we thought up
	// the cube map)
	// We cannot pre-pack the view matrix in that calculation since we're just 
	// too awesome for that: since we're using a geometry shader to dispatch
	// to 6 'views' at once, there's no single 'eye space' to tell the second
	// pass about, there's six of them!
	// Vielen Dank, TU Wien!
	vec3 cm_lookup_vec = vertPos_wc - lightPosition;
	float d_l_closest_occluder = texture(cubeShadowMap, cm_lookup_vec ).z;
	float d_l_current_fragment = length(cm_lookup_vec);
	
	d_l_closest_occluder *= far;
	
	float t_bias = bias;
	if(shadowQuality > 1) {
		t_bias *= tan(acos(NL));	
		t_bias  = clamp(t_bias, 0.00f, bias);
	}

	if( d_l_closest_occluder  + t_bias < d_l_current_fragment ) {
		visibility = 0.3f;
	}
	
	return visibility;
}

float computeVisibility(in float NL) {

	float visibility = 1.0f;

	// This line should technically only be needed when dealing with spot lights
	vec4 sc4 = vertPos_dmc / vertPos_dmc.w;
	vec2 sc  = sc4.xy;		
	
	float t_bias = bias;
	if(shadowQuality > 1) {
		t_bias *= tan(acos(NL));	
		t_bias  = clamp(t_bias, 0.00f, bias);
	}
	
	if( vertPos_dmc.w <= 0 ) {
		visibility = 1.0f;
	} else if(sc.x <= 0.0 || sc.x >= 1.0f || sc.y <= 0.0 || sc.y >= 1.0f) {
		visibility = 1.0f;
	} else {
		if(shadowQuality <= 2) {
			if(texture(shadowMap, sc).z < (vertPos_dmc.z - t_bias) /  vertPos_dmc.w ) {
				visibility = 0.2f;
			}
		}
		else if(shadowQuality == 3) {
			for (int i = 0; i < 16; i++) {
				vec2 coord = sc + pD[i] / pFac;
				if(texture(shadowMap, coord).z < (vertPos_dmc.z - t_bias) / vertPos_dmc.w) {
    				visibility -= 0.05;
  				}
			}
		} else if(shadowQuality >= 4) {
			for (int i = 0; i < 4; i++) {
				int index = int(mod(16.0 * rand(gl_FragCoord.xyy, i), 16));
				vec2 coord = sc + pD[index] / pFac;
				if(texture(shadowMap, coord).z < (vertPos_dmc.z - t_bias) / vertPos_dmc.w) {
    				visibility -= 0.2;
  				}
			}
  		}
	}

	return visibility;
}

/**
 *	Phong per-pixel lighting shading model.
 */
void main() {		
	vec3 ct, cf;
	vec4 texel = vec4(1.0f);
	float at, af;

	if(useTexture) {
		texel = texture(diffuseMap, texCoords); 
	} 
	
	ct = texel.rgb;
	at = texel.a;

	vec3 nNormal = normalize(normal_wc);
	vec3 nLightDir = vec3(0.0f);
	if(lightType == 0) {			// Directional
		nLightDir = normalize(lightDirection);
	} else {		// Point / spot
		nLightDir = normalize(lightPosition - vertPos_wc);
	}
	
	
	if(useBump) {
		vec3 vBump = 2.0f * texture(normalMap, texCoords).rgb - 1.0f;
		vBump = normalize(mNTB * vBump);
		nNormal = vBump;
	}
	
	float NL = dot(nNormal, nLightDir);
	float visibility = 1.0f;
	
	if(useShadows) {
		if(samplingCube) {
			visibility = computeVisibilityCube(NL);
		} else { 			
			visibility = computeVisibility(NL);
		}
	}
	
	float intensity = computeIntensity(nNormal, nLightDir, NL);
	intensity *= visibility;
	
	cf = matAmbient.rgb * globalAmbient.rgb + intensity * lightDiffuse.rgb * matDiffuse.rgb;	
	af = matAmbient.a * globalAmbient.a + lightDiffuse.a * matDiffuse.a;
	
	if(intensity > 0.0f && shininess > 0.0f) {
		// Specular light
		vec3 vReflection = normalize(reflect(-nLightDir, nNormal));
		vec3 vertexToEye = normalize(eyeWorldPos - vertPos_wc);
		float spec = max(0.0, dot(vertexToEye, vReflection));
		float fSpec = pow(spec, shininess) * lightSpecular.a;
		cf += intensity * vec3(fSpec) * lightSpecular.rgb * matSpecular.rgb;
	}
	
	// Color modulation
	vFragColor = vec4(ct * cf, at * af);
	
	// Add the fog to the mix
	if(fogEnabled) {
		vFragColor = mix(vFragColor, fogColor, fogFactor);
	}
	
	if(useGammaCorrection) {
		vFragColor = pow(vFragColor, vec4(invGamma) );
	}
	
	//vFragColor -= vFragColor;
	
	//vFragColor += vec4(nNormal, 1.0f);
	//vFragColor += vec4(vec3(length(lightDir) / 100), 1.0f);
	//vFragColor += texture(cubeShadowMap, vec3(1.0f, 2.3f, 1.0f));
	//vFragColor += vec4(vertexTangent_cameraspace, 1.0f);
	//vFragColor += vec4(intensity, intensity, intensity, 1.0f);
	// vFragColor += vec4(texture(shadowMap, vertPos_dmc.xy ).z) * 0.88f;	
	//vFragColor += vec4(vertPos_dmc.z) * 0.5;
	//vFragColor += vec4(d_l_current_fragment  / (far - near)); // works
	//vFragColor += vec4(d_l_closest_occluder);
	//float diff = texture( shadowMap, vertPos_dmc.xy ).z - vertPos_dmc.z;
	//vFragColor += vec4(length(vertPos_wc - lightPos_wc) / (far - near));
	//vFragColor += vec4(val);
	//vFragColor.a = 1.0f;
} 	