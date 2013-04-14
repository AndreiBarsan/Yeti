#version 330

uniform sampler2D colorMap;

in 	vec2 vVaryingTexCoords;

layout(location = 0) out vec4 vFragColor;
 
void main(void) {
	vFragColor = texture(colorMap, vVaryingTexCoords);
}	
