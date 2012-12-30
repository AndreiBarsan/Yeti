#version 330

uniform sampler2D colorMap;

smooth in 	vec2 vVaryingTexCoords;

layout(location = 0) smooth out	vec4 vFragColor;
 
void main(void) {
	vFragColor = texture(colorMap, vVaryingTexCoords);
}	
