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
    float Theta;
	float Phi;
	float Exponent;
};

uniform PointLight 	pointLight;
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

vec4 calcPointLight(vec3 WorldPos, vec3 Normal, float MSI, float SP) {	
	vec3 LightDirection = WorldPos - pointLight.Position;
    float Distance = length(LightDirection);
    LightDirection = normalize(LightDirection);

    vec4 Color = CalcLightInternal(pointLight.Base, LightDirection, 
									WorldPos, Normal,
									MSI, SP);

    float Attenuation =  pointLight.Atten.Constant +
                         pointLight.Atten.Linear * Distance +
                         pointLight.Atten.Quadratic * Distance * Distance;

    Attenuation = max(1.0, Attenuation);

    return Color / Attenuation;
}

void main(void) {
	vec2 TexCoord = CalcTexCoord();
   	vec3 WorldPos = texture(positionMap, TexCoord).xyz;
   	vec3 Color = texture(colorMap, TexCoord).xyz;
   	vec3 Normal = texture(normalMap, TexCoord).xyz;
   	Normal = normalize(Normal);
	float MSI = texture(colorMap, TexCoord).a * 1000.0f;
	float SP = pow(2, texture(normalMap, TexCoord).a * 10.5f);
   	vFragColor = vec4(Color, 1.0) * calcPointLight(WorldPos, Normal, MSI, SP);
}
