#version 400 core

const float factor = 12.0f;

uniform sampler2D colorMap;

in vec2 UV;

// Ouput data
layout(location = 0) out vec4 color;

void main(){
	color = 1.0f - (1.0f - texture(colorMap, UV)) * factor;
	color.a = 1.0f;
}