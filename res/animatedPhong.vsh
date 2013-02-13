#version 400 core

uniform mat4 mvpMatrix;
uniform mat4 mvMatrix;
uniform mat4 mMatrix;
uniform mat4 vMatrix;

uniform mat3 vMatrix3x3;

uniform mat3 normalMatrix;
uniform vec4 lightPosition;
uniform vec3 spotDirection;
uniform bool useTexture;

uniform bool 	useBump;

uniform bool 	fogEnabled;
uniform float 	minFogDistance;
uniform float 	maxFogDistance;

uniform bool 	useShadows;
uniform bool 	samplingCube;
uniform mat4 	mvpMatrixShadows;

// 0 ... 1, where 0 is the start position and 1 the end position
uniform float animationIndex;  
in layout(location = 0) vec4 inPositionStart;
in layout(location = 1) vec3 inNormalStart;

in layout(location = 2) vec4 inPositionEnd;
in layout(location = 3) vec3 inNormalEnd;

in layout(location = 4) vec2 inTexCoords;


smooth out vec3 	normal_ec;
smooth out vec3 	lightDir;
smooth out vec2 	texCoords;
smooth out float 	fogFactor;

smooth out vec4 	vertPos_ec;
smooth out vec4 	vertPos_wc;
smooth out vec4 	lightPos_ec;
smooth out vec3 	lightPos_wc;

smooth out vec3 	spotDirection_ec;

smooth out mat3 	mNTB;
smooth out vec4 	vertPos_dmc;	// Used in shadow mapping

void main(void) {

	vec4 interpolatedPosition 	= mix(inPositionStart, inPositionEnd, animationIndex);
	vec3 interpolatedNormal		= mix(inNormalStart, inNormalEnd, animationIndex);

	normal_ec = normalMatrix * interpolatedNormal;

	vec4 vPosition4 = mvMatrix * interpolatedPosition;
	vec3 vPosition3 = vPosition4.xyz / vPosition4.w;

	vec4 tLightPos4 = vMatrix * lightPosition;
	vec3 tLightPos  = tLightPos4.xyz / tLightPos4.w;

	lightPos_ec = vec4(tLightPos, 1.0f);
	vertPos_ec = vec4(vPosition3, 1.0f);
	
	spotDirection_ec = vMatrix3x3 * spotDirection;	// Important! 

	if(lightPosition.w == 0.0f) {
		// Directional light
		lightDir = tLightPos4.xyz;
	} else {
		// Point light
		lightDir = tLightPos - vPosition3;
	}
	
	
	if(useTexture) {
		texCoords = inTexCoords;
	}
	
	if(useBump) {
		vec3 vNormal = normalize(interpolatedNormal);
		vec3 vTang = normalize(vec3(-vNormal.z, 0, vNormal.x));
		if( vNormal.z == vNormal.x) { 
			vTang = vec3 (1.0, 0.0, 0.0);
		}
		vec3 vBinorm = normalize(cross(vTang, vNormal));
		mNTB[0] = vTang;
		mNTB[1] = vBinorm;
		mNTB[2] = vNormal;
		mNTB = normalMatrix * mNTB;
	}
	
	if(useShadows) {
		// Convert the vertex to shadowmap coordinates
		vertPos_dmc = mvpMatrixShadows * interpolatedPosition;
	
		if(samplingCube) {
			lightPos_wc = lightPosition.xyz;
			vertPos_wc = mMatrix * interpolatedPosition;
		}	
	}
	
	gl_Position =  mvpMatrix * interpolatedPosition;
	
	if(fogEnabled) {
		float len = length(gl_Position);
		fogFactor = (len - minFogDistance) / (maxFogDistance - minFogDistance);
		fogFactor = clamp(fogFactor, 0, 1);
	}
}
