#version 400 core

uniform bool 		useTexture;
uniform sampler2D 	diffuseMap;

uniform bool 		useBump;
uniform sampler2D 	normalMap;
  
uniform vec4 		matDiffuse;
uniform float 		matSpecularIntensity;
uniform float 		matSpecularPower;

in vec3 WorldPos;
in vec2 TexCoord;
in vec3 Normal;
in mat3 mNTB;

out vec4 outWorldPos; 
out vec4 outDiffuse; 
out vec4 outNormal; 
//out vec3 outTexCoord;

void main() {	
    outWorldPos.xyz = WorldPos;	

	vec3 nNormal = normalize(Normal);
	if(useBump) {
		vec3 vBump = 2.0f * texture(normalMap, TexCoord).rgb - 1.0f;
		vBump = normalize(mNTB * vBump);
		nNormal = vBump;
	}
	outNormal.xyz = nNormal;
	
	if(useTexture) {
    	outDiffuse.rgb = texture(diffuseMap, TexCoord).rgb * matDiffuse.rgb;
	}
	else {
		outDiffuse.rgb = matDiffuse.rgb;
	}	
	
	// Write specular intensity to out-diffuse-alpha
	outDiffuse.a = matSpecularIntensity;
	// Write specular exponent to out-normal-alpha
	outNormal.a  = matSpecularPower;
};