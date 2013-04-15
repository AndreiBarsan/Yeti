#version 400 core

uniform bool 		useTexture;
uniform sampler2D 	diffuseMap;

uniform bool 		useBump;
uniform sampler2D 	normalMap;
  
uniform vec4 		matDiffuse;

in vec3 WorldPos;
in vec2 TexCoord;
in vec3 Normal;
in mat3 mNTB;

out vec3 outWorldPos; 
out vec3 outDiffuse; 
out vec3 outNormal; 
out vec3 outTexCoord;

void main() {	
    outWorldPos = WorldPos;	

	vec3 nNormal = normalize(Normal);
	if(useBump) {
		vec3 vBump = 2.0f * texture(normalMap, TexCoord).rgb - 1.0f;
		vBump = normalize(mNTB * vBump);
		nNormal = vBump;
	}
	outNormal = nNormal;
	
	if(useTexture) {
    	outDiffuse = texture(diffuseMap, TexCoord).rgb * matDiffuse.rgb;
	}
	else {
		outDiffuse = matDiffuse.rgb;
	}	
	
    outTexCoord = vec3(TexCoord, 0.0);	
};