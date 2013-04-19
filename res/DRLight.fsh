#version 400 core

// Structure heavily inspired by Etay Meiri's examples.
// Valve-tier GLSL code cleanliness, man!
// If you're somehow reading this, thank you a million times for all the tutorials!

uniform vec2 screenSize;
uniform sampler2D positionMap;
uniform sampler2D colorMap;
uniform sampler2D normalMap;

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

uniform vec3 		eyeWorldPos;
uniform int 		lightType;


out vec4 vFragColor;

vec2 CalcTexCoord() {
   return gl_FragCoord.xy / screenSize;
}

vec4 CalcLightInternal(BaseLight Light,
					   vec3 LightDirection,
					   vec3 WorldPos,
					   vec3 Normal,
					   float SpecularIntensity, float SpecularPower)
{
    vec4 AmbientColor = vec4(Light.Color, 1.0f) * Light.AmbientIntensity;
    float DiffuseFactor = dot(Normal, -LightDirection);

    vec4 dColor  = vec4(0, 0, 0, 0);
    vec4 sColor = vec4(0, 0, 0, 0);

    if (DiffuseFactor > 0) {
        dColor = vec4(Light.Color, 1.0f) * Light.DiffuseIntensity * DiffuseFactor;

		vec3 vReflection = normalize(reflect(LightDirection, Normal));
		float spec = max(0.0, dot(Normal, vReflection));
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
	
   	vec3 WorldPos = pdata.xyz;
   	vec3 Color = cdata.xyz;
   	vec3 Normal = normalize(ndata.xyz);

	float MSI = cdata.a;
	float SP = ndata.a;
	if(lightType == 0) {		// Directional
		vFragColor = vec4(Color, 1.0f) * calcDirLight(WorldPos, Normal, MSI, SP);
	}
	else if(lightType == 1) {	// Point
   		vFragColor = vec4(Color, 1.0) * calcPointLight(pointLight, WorldPos, Normal, MSI, SP);
	} 
	else {						// Spot
		vFragColor = vec4(Color, 1.0) * calcSpotLight(WorldPos, Normal, MSI, SP);
	}
}
