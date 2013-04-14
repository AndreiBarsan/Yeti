#version 400 core

uniform mat4 mvpMatrix;
uniform mat4 mvMatrix;
uniform mat3 normalMatrix;

in vec4 vVertex;
in vec3 vNormal;
in vec2 vTexCoord;

out vec3 vVaryingTexCoords; 
out vec2 texCoords2d;

void main(void) {
	
	vec3 n = normalize(normalMatrix * vNormal);
	vec4 pos = mvMatrix * vVertex;
	vec3 d = reflect(pos.xyz, n);
	mat3 m = normalMatrix;

	vVaryingTexCoords.x = dot(m[0], d);
	vVaryingTexCoords.y = dot(m[1], d);
	vVaryingTexCoords.z = dot(m[2], d);
	
	texCoords2d = vTexCoord;
	gl_Position = mvpMatrix * vVertex;
}
