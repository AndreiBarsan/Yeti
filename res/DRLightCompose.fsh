#version 400

uniform sampler2D diffuseMap;
uniform sampler2D lightMap;

uniform sampler2D normalMap;
uniform sampler2D positionMap;
uniform sampler2D randomNormalMap;

uniform vec2 		screenSize;
uniform float 		randomSize;

uniform float 		aoScale;
uniform float 		aoBias;
uniform float 		aoSampleRad;
uniform float 		aoIntensity;

in vec2 vVaryingTexCoords;

out vec4 composedColor;

vec2 nuv;

/** SSAO as explained by José María Méndez */

vec2 getRandom(in vec2 uv) {
	return normalize(texture(randomNormalMap, screenSize * uv / randomSize).xy * 2.0f - 1.0f);
}

vec3 getPosition(in vec2 uv) {
	return texture(positionMap, uv).xyz;
}

vec3 getNormal(in vec2 uv) {
	return normalize(texture(normalMap, uv).xyz);
}

float computeAOSample(in vec2 tcoord, in vec2 uv, in vec3 p, in vec3 cnorm) {
	vec3 diff = getPosition(tcoord + uv) - p;
	vec3 v = normalize(diff);
	float d = length(diff) * aoScale;
	return max(0.0, dot(cnorm, v) - aoBias) * ( 1.0f / (1.0f + d) ) * aoIntensity;
}

float computeAO(in vec2 uv) {
	vec3 p = getPosition(uv);
	vec3 n = getNormal(uv);
	vec2 rand = getRandom(uv);

	vec2 dirs[4];
	dirs[0] = vec2( 1,  0);
	dirs[1] = vec2(-1,  0);
	dirs[2] = vec2( 0,  1);
	dirs[3] = vec2( 0, -1);

	float ao = 0.0f;
	float rad = aoSampleRad / p.z;

	int iterations = 4;
	for(int j = 0; j < iterations; ++j) {
		vec2 coord1 = reflect(dirs[j], rand) * rad;
		vec2 coord2 = vec2(	coord1.x * 0.707 - coord1.y * 0.707,
							coord1.x * 0.707 + coord1.y * 0.707 );

		ao += computeAOSample(uv, coord1 * 0.25, p, n);
		ao += computeAOSample(uv, coord1 * 0.5, p, n);
		ao += computeAOSample(uv, coord1 * 0.75, p, n);
		ao += computeAOSample(uv, coord2, p, n);
	} 
	
	ao /= float(iterations) * 4.0f;
	return ao;
}

void main(void) {
	nuv = vVaryingTexCoords;

	// Compose the albedo and the lighting
	vec4 light = texture(lightMap, nuv);

	float ao = computeAO(nuv);
	//*
	light = vec4(
		max(0.0f, light.r - ao),
		max(0.0f, light.g - ao),
		max(0.0f, light.b - ao),
		light.a
		);
	//*/
	composedColor = texture(diffuseMap, nuv) *  light; 
	

	// composedColor = vec4(vec3(1 - ao), 1);
}