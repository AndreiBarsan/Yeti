#version 400 core

const float bias = 0.0025f;
const vec2 poissonDisk[16] = vec2[]( 
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

float rand(in vec4 seed4) {
	float dot_product = dot(seed4, vec4(12.9898,78.233,45.164,94.673));
    return fract(sin(dot_product) * 43758.5453);
}

/**
 *	Phong per-pixel lighting shading model.
 */
void main() {		
	vec3 ct, cf;
	vec4 texel;
	float at, af;

	bool test = false;

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
	
	float intensity = computeIntensity(nNormal, nLightDir);
	
	float visibility = 1.0f;
	if(useShadows) {
		// This should technically only be needed when dealing with point
		// lights
		vec4 sc4 = vertPos_dmc / vertPos_dmc.w;
		vec2 sc  = sc4.xy;		

		// this dot prod. can be cached
		float t_bias = bias * tan(acos(dot(nNormal, nLightDir)));	
		t_bias = clamp(t_bias, 0.00f, 0.01f);
	
		if( vertPos_dmc.w == 0 ) {
			visibility = 1.0f;
		// TODO: fix the dark stripe bug
		} else if(sc.x <= 0.0 || sc.x >= 1.0f || sc.y <= 0.0 || sc.y >= 1.0f) {
			visibility = 1.0f;
		} else {
			for (int i=0; i < 4; i++) {
				int index = int(16.0 * rand(vec4(gl_FragCoord.xyy, i))) % 16;
 				if ( texture2D( shadowMap, sc + poissonDisk[index] / 1800.0f).z + t_bias < sc4.z) {
    				visibility -= 0.2;
  				}
			}
		}
	}

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
	//vFragColor += vec4(vertPos_dmc.z) * 0.5;
	//float diff = texture2D( shadowMap, vertPos_dmc.xy ).z - vertPos_dmc.z;
	//vFragColor += vec4();
	//vFragColor += vec4(visibility);
	//vFragColor.a = 1.0f;
}