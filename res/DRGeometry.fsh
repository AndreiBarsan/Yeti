#version 400 core

struct FSOutput
{ 
    vec3 WorldPos; 
    vec3 Diffuse; 
    vec3 Normal; 
    vec3 TexCoord; 
};

uniform bool useTexture;
uniform sampler2D colorMap; 

in vec3 WorldPos;
in vec2 TexCoord;
in vec3 Normal; 

out FSOutput FSout;

void main()
{	
    FSout.WorldPos = WorldPos;	
    FSout.Diffuse = texture(colorMap, TexCoord).xyz;	
    FSout.Normal = normalize(Normal);	
    FSout.TexCoord = vec3(TexCoord, 0.0);	
};