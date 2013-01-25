#version 400 core

// Useful to improve depth buffer visualzation
uniform float factor;

uniform samplerCube colorMap;

in vec2 UV;

// Ouput data
layout(location = 0) out vec4 color;

void main(){
	// color = texture(colorMap, vec3(1.0f, UV.y, UV.x));
	  color = texture(colorMap, vec3(-1.0f, UV.y, -UV.x));
	//color = texture(colorMap, vec3(UV.x, UV.y, 1.0f));
	color = 1.0f - (1.0f - color) * factor;
}