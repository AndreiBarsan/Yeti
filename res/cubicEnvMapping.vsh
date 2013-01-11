#version 330

uniform mat4 mvpMatrix;
uniform mat4 mvMatrix;
uniform mat4 mInverseCameraRot;

uniform vec3 cameraPosition;
uniform mat3 mvMatrix3x3;

uniform mat3 normalMatrix;

layout(location = 0) in vec4 vVertex;
layout(location = 1) in vec3 vNormal;

smooth out vec3 vVaryingTexCoords; 

void main(void) {
	vec3 eyeNormal = normalMatrix * vNormal;
	
	vec4 vert4 = mvMatrix * vVertex;
	vec3 eyeVertex = normalize(vert4.xyz / vert4.w);

	vec4 coords = vec4(reflect(eyeVertex, eyeNormal), 1.0);

	coords = mInverseCameraRot * coords;
	vVaryingTexCoords.xyz = normalize(coords.xyz);

	gl_Position = mvpMatrix * vVertex;
}
