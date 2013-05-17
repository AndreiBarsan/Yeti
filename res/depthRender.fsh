#version 400 core

// Useful to improve depth buffer visualzation
uniform float factor;

uniform sampler2D colorMap;

in vec2 UV;

// Ouput data
out vec4 color;

void main(){
	vec4 data = texture(colorMap, UV);
	color = 1.0f - (1.0f - data) * factor;
	color.a = 1.0f;
}