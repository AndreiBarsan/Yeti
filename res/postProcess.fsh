#version 330

uniform sampler2D colorMap;
uniform float blurH, blurV;

smooth in 	vec2 vVaryingTexCoords;

smooth out	vec4 vFragColor;
 
void main(void) {
	vFragColor  = texture(colorMap, vVaryingTexCoords);
	//*
	vec4 horizontal = vec4(0.0f); 

	horizontal += texture(colorMap, vec2(vVaryingTexCoords.x, vVaryingTexCoords.y - 4.0* blurH)) * 0.05;
	horizontal += texture(colorMap, vec2(vVaryingTexCoords.x, vVaryingTexCoords.y - 3.0*blurH)) * 0.09;
	horizontal += texture(colorMap, vec2(vVaryingTexCoords.x, vVaryingTexCoords.y - 2.0*blurH)) * 0.12;
	horizontal += texture(colorMap, vec2(vVaryingTexCoords.x, vVaryingTexCoords.y - blurH)) * 0.15;
	horizontal += texture(colorMap, vec2(vVaryingTexCoords.x, vVaryingTexCoords.y)) * 0.16;
	horizontal += texture(colorMap, vec2(vVaryingTexCoords.x, vVaryingTexCoords.y + blurH)) * 0.15;
	horizontal += texture(colorMap, vec2(vVaryingTexCoords.x, vVaryingTexCoords.y + 2.0*blurH)) * 0.12;
	horizontal += texture(colorMap, vec2(vVaryingTexCoords.x, vVaryingTexCoords.y + 3.0*blurH)) * 0.09;
	horizontal += texture(colorMap, vec2(vVaryingTexCoords.x, vVaryingTexCoords.y + 4.0*blurH)) * 0.05;
	  
	vec4 vertical = vec4(0.0f);
	vertical += texture(colorMap, vec2(vVaryingTexCoords.x - 4.0*blurV, vVaryingTexCoords.y)) * 0.05;
    vertical += texture(colorMap, vec2(vVaryingTexCoords.x - 3.0*blurV, vVaryingTexCoords.y)) * 0.09;
	vertical += texture(colorMap, vec2(vVaryingTexCoords.x - 2.0*blurV, vVaryingTexCoords.y)) * 0.12;
	vertical += texture(colorMap, vec2(vVaryingTexCoords.x - blurV, vVaryingTexCoords.y)) * 0.15;
	vertical += texture(colorMap, vec2(vVaryingTexCoords.x, vVaryingTexCoords.y)) * 0.16;
	vertical += texture(colorMap, vec2(vVaryingTexCoords.x + blurV, vVaryingTexCoords.y)) * 0.15;
	vertical += texture(colorMap, vec2(vVaryingTexCoords.x + 2.0*blurV, vVaryingTexCoords.y)) * 0.12;
	vertical += texture(colorMap, vec2(vVaryingTexCoords.x + 3.0*blurV, vVaryingTexCoords.y)) * 0.09;
	vertical += texture(colorMap, vec2(vVaryingTexCoords.x + 4.0*blurV, vVaryingTexCoords.y)) * 0.05;
	
   	// vFragColor = mix(vertical, horizontal, 0.5f);
	//*/
}	
