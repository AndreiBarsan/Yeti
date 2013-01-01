#version 330

#ifdef GL_ES
precision highp float;
#endif

uniform vec4 ambientColor;
uniform vec4 diffuseColor;
uniform vec4 specularColor;

uniform int shininess;
uniform vec4 matColor;

// Texture stuff
uniform bool useTexture;
uniform sampler2D colorMap;

uniform bool useEdgeContour;
uniform vec3 cameraDirection;

// WARNING GLES Doesn't support 1D samplers
//uniform sampler1D toonMap;

// Fog
uniform bool 	fogEnabled;
uniform vec4	fogColor;

smooth in vec3 	vVaryingNormal;
smooth in vec3 	vVaryingLightDir;
smooth in vec2 	vVaryingTexCoords;
smooth in float fogFactor;

out vec4 vFragColor;

vec4 toonify(float intensity) {
	if(intensity < .2f) {
		return vec4(0.0f);
	} else if(intensity < .6f) {
		return vec4(0.5f, 0.5f, 0.5f, 1.0f);
	} else if(intensity < .9f) {
		return vec4(0.7f, 0.7f, 0.7f, 1.0f);
	} else {
		return vec4(0.9f, 0.9f, 0.9f, 1.0f);
	}
}

void main() {
	// Compute instensity
	float intensity = max(0.0f, dot(
		normalize(vVaryingNormal),
		normalize(vVaryingLightDir)
	));

	vFragColor 	= toonify(intensity) * diffuseColor;
	
	// Ambient light
	vFragColor += ambientColor;	
	
	// Apply ze texture
	if(useTexture) {
		vFragColor *= texture(colorMap, vVaryingTexCoords);
	}
		
	vFragColor *= matColor;

	if(useEdgeContour) {
		float edge = dot(normalize(vVaryingNormal), -normalize(cameraDirection));
		edge = clamp(edge, 0, 1);
		if(edge < 0.2) {
			vFragColor = mix(vec4(0.0f, 0.0f, 0.0f, 1.0f), vFragColor, edge);
		}
	}

	if(fogEnabled) {
		vFragColor = mix(vFragColor, fogColor, fogFactor);
	}
}