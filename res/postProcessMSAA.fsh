#version 330

uniform sampler2DMS colorMap;

// Number of MSAA samples
uniform int sampleCount;
uniform float kernel[];

smooth in 	vec2 vVaryingTexCoords;

smooth out	vec4 vFragColor;
 
void main(void) {

	// Calculate un-normalized texture coordinates
    vec2 tmp = floor (textureSize (colorMap) * vVaryingTexCoords);
	vFragColor = vec4(0.0f);
	for(int i = 0; i < sampleCount; i++) {
		vFragColor += texelFetch(colorMap, ivec2(tmp), i);
	}
	vFragColor /= sampleCount;
}	
