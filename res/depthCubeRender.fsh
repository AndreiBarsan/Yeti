#version 400 core

// Useful to improve depth buffer visualzation
uniform float factor;

uniform samplerCube colorMap;

in vec2 UV;

// Ouput data
out vec4 color;

void main(){
	color = texture(colorMap, vec3(UV.x, -1.0f, UV.y));
	color = 1.0f - (1.0f - color) * factor;
}