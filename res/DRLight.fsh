#version 400 core
#define L_DIRECTIONAL 	0
#define L_POINT 		1
#define L_SPOT			2

#define UNLIT			0.1f
#define PDSAMPLES		16

// Structure heavily inspired by Etay Meiri's examples (http://ogldev.atspace.co.uk/index.html)
// Extending for multiple light type support, normal mapping and shadow mapping 
// by Andrei Barsan

// Valve-tier GLSL code cleanliness, man!
// If you're somehow reading this, thank you a million times for all the tutorials!

struct BaseLight
{
    vec3 Color;
    float AmbientIntensity;
    float DiffuseIntensity;
};

struct DirectionalLight
{
    BaseLight Base;
    vec3 Direction;
};

struct Attenuation
{
    float Constant;
    float Linear;
    float Quadratic;
};

struct PointLight
{
    BaseLight Base;
    vec3 Position;
    Attenuation Atten;
};

struct SpotLight
{
    PointLight Base;
    vec3 Direction;
    float CosInner;
	float CosOuter;
	float Exponent;
};

uniform PointLight 			pointLight;
uniform SpotLight			spotLight;
uniform DirectionalLight	dirLight;

uniform vec2 screenSize;

uniform sampler2D 	positionMap;
uniform sampler2D 	colorMap;
uniform sampler2D 	normalMap;

uniform sampler2D 	shadowMap;
uniform samplerCube cubeShadowMap;

uniform vec3 		eyeWorldPos;
uniform int 		lightType;

uniform bool 		useShadows;
uniform mat4 		biasMatrix;
uniform mat4 		vpMatrixShadows;
uniform int 		shadowQuality;

uniform float 		far;

const float 		bias = 0.01f;
const float 		cubeBias = 0.24f;
const float 		pFac = 250.0f;

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

out vec4 vFragColor;

float NL;
vec4 vertPos_dmc;
vec3 WorldPos;
vec3 Color;
vec3 Normal;

vec2 CalcTexCoord() {
   return gl_FragCoord.xy / screenSize;
}

float rand(in vec3 seed3, in int index) {
	vec4 seed4 = vec4(seed3, index);
	float dot_product = dot(seed4, vec4(12.9898,78.233,45.164,94.673));
    return fract(sin(dot_product) * 43758.5453);
}


float computeVisibilityCube() {
	float visibility = 1.0f;
	
	vec3 cm_lookup_vec = WorldPos - pointLight.Position;
	float d_l_closest_occluder = texture(cubeShadowMap, cm_lookup_vec ).z;
	float d_l_current_fragment = length(cm_lookup_vec);
	
	d_l_closest_occluder *= far;
	
	float t_bias = cubeBias;
	if(shadowQuality > 1) {
		t_bias *= tan(acos(NL));	
		t_bias  = clamp(t_bias, 0.00f, cubeBias);
	}

	if( d_l_closest_occluder  + t_bias < d_l_current_fragment ) {
		visibility = UNLIT;
	}
	
	return visibility;
}

// Compute a point's visiblity based on a shadow map, in relation to either a
// directional light, or a spotlight
float computeVisibilityFlat() {

	float visibility = 1.0f;

	// This line should technically only be needed when dealing with spot lights
	vec4 sc4 = vertPos_dmc / vertPos_dmc.w;
	vec2 sc  = sc4.xy;		
	 
	float t_bias = bias;
	if(shadowQuality > 1) {
		//t_bias *= tan(acos(NL));	
		//t_bias  = clamp(t_bias, 0.00f, bias);
	}
	
	if( vertPos_dmc.w <= 0 ) {
		visibility = 1.0f;
	} else if(sc.x <= 0.0 || sc.x >= 1.0f || sc.y <= 0.0 || sc.y >= 1.0f) {
		visibility = 1.0f;
	} else {
		if(shadowQuality <= 2) {
			if(texture(shadowMap, sc).z < (vertPos_dmc.z - t_bias) /  vertPos_dmc.w ) {
				visibility = UNLIT;
			}
		}
		else {
			for (int i = 0; i < PDSAMPLES; i++) {
				vec2 coord = sc + pD[i] / pFac;
				float unlitStep = (1 - UNLIT);
				unlitStep /= PDSAMPLES;
				if(texture(shadowMap, coord).z < (vertPos_dmc.z - t_bias) / vertPos_dmc.w) {
    				visibility -= unlitStep;
  				}
			}
		}
	}

	return visibility;
}

float computeVisibility() {
	if( ! useShadows) {
		return 1.0f;
	}
	
	if(lightType == L_POINT) {
		return computeVisibilityCube();
	}
	
	return computeVisibilityFlat();
}

vec4 CalcLightInternal(BaseLight Light,
					   vec3 LightDirection,
					   vec3 WorldPos,
					   vec3 Normal,
					   float SpecularIntensity, float SpecularPower)
{
    vec4 AmbientColor = vec4(Light.Color, 1.0f) * Light.AmbientIntensity;
    float DiffuseFactor = dot(Normal, -LightDirection);

	NL = max(0.0f, DiffuseFactor);

    vec4 dColor  = vec4(0, 0, 0, 0);
    vec4 sColor = vec4(0, 0, 0, 0);

    if (DiffuseFactor > 0) {
        dColor = vec4(Light.Color, 1.0f) * Light.DiffuseIntensity * DiffuseFactor;

		vec3 vReflection = normalize(reflect(LightDirection, Normal));
		vec3 vertexToEye = normalize(eyeWorldPos - WorldPos);
		float spec = max(0.0, dot(vertexToEye, vReflection));
        float SpecularFactor = pow(spec, SpecularPower);
        if (SpecularFactor > 0) {
            sColor = vec4(Light.Color, 1.0f) * SpecularIntensity * SpecularFactor;
        }
    }

    return (AmbientColor + dColor + sColor);
}

vec4 calcPointLight(PointLight pl, vec3 WorldPos, vec3 Normal, float MSI, float SP) {	
	vec3 LightDirection = WorldPos - pl.Position;
    float Distance = length(LightDirection);
    LightDirection = normalize(LightDirection);

    vec4 Color = CalcLightInternal(pl.Base, LightDirection, 
									WorldPos, Normal,
									MSI, SP);

    float Attenuation =  pl.Atten.Constant +
                         pl.Atten.Linear * Distance +
                         pl.Atten.Quadratic * Distance * Distance;

    Attenuation = max(1.0, Attenuation);

    return Color / Attenuation;
}

vec4 calcSpotLight(vec3 WorldPos, vec3 Normal, float MSI, float SP) {

		vec4 Color = calcPointLight(spotLight.Base, WorldPos, Normal, MSI, SP);
		vec3 LightDirection = WorldPos - spotLight.Base.Position;
		vec3 nLightDir = normalize(LightDirection);

		float cos_outer_cone = spotLight.CosOuter;
		float cos_inner_cone = spotLight.CosInner;
		float cos_diff = cos_inner_cone - cos_outer_cone;
		float cos_cur = dot(normalize(spotLight.Direction), nLightDir);
		float spotEffect = clamp((cos_cur - cos_outer_cone) / cos_diff, 0.0, 1.0);
		spotEffect = pow(spotEffect, spotLight.Exponent);

		return Color * spotEffect;
}

vec4 calcDirLight(vec3 WorldPos, vec3 Normal, float MSI, float SP) {
	return CalcLightInternal(dirLight.Base, dirLight.Direction, 
								WorldPos, Normal, MSI, SP);
} 

void main(void) {
	vec2 TexCoord = CalcTexCoord();

	vec4 pdata = texture(positionMap, TexCoord);
	vec4 cdata = texture(colorMap, TexCoord);
	vec4 ndata = texture(normalMap, TexCoord);
	
   	WorldPos = pdata.xyz;
   	Color = cdata.xyz;
   	Normal = normalize(ndata.xyz);

	// Unpack misc data
	float MSI = cdata.a;
	float SP = ndata.a;

	if(lightType == L_DIRECTIONAL) {		// Directional
		vFragColor = vec4(Color, 1.0f) * calcDirLight(WorldPos, Normal, MSI, SP);
	}
	else if(lightType == L_POINT) {			// Point
   		vFragColor = vec4(Color, 1.0f) * calcPointLight(pointLight, WorldPos, Normal, MSI, SP);
	} 
	else {									// Spot
		vFragColor = vec4(Color, 1.0f) * calcSpotLight(WorldPos, Normal, MSI, SP);
	}

	if(useShadows) {
		vertPos_dmc = biasMatrix * vpMatrixShadows * vec4( vec4(WorldPos, 1.0f) );
		vFragColor *= computeVisibility();

		vec4 sc4 = vertPos_dmc / vertPos_dmc.w;
		vec2 sc  = sc4.xy;	

		//vFragColor -= 0.99f * vFragColor;
		float v = (vertPos_dmc.z - bias) /  vertPos_dmc.w ;
		float v2 = texture(shadowMap, sc).z; 

		if(v2 < v) {
			//vFragColor += vec4(1.0f);
		}
	 
	}
	
	//vFragColor.r = pow(vFragColor.r, 1 / 2.2f);
	//vFragColor.g = pow(vFragColor.g, 1 / 2.2f);
	//vFragColor.b = pow(vFragColor.b, 1 / 2.2f);
}
