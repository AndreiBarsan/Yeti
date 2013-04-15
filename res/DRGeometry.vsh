#version 400 core

uniform mat4 mvpMatrix;
uniform mat4 mvMatrix;
uniform mat4 mMatrix;
//uniform mat4 vMatrix;

//uniform mat3 vMatrix3x3;

uniform mat3 normalMatrix;
uniform bool useTexture;
uniform bool useBump;

in vec4 vVertex;
in vec3 vNormal;
in vec2 vTexCoord;
in vec3 vTang;
in vec3 vBinorm;

out vec3 WorldPos;
out vec2 TexCoord;
out vec3 Normal; 
out mat3 mNTB; 


void main() {
	Normal = (mMatrix * vec4(vNormal, 0.0f)).xyz;

	if(useBump) {
		mNTB[0] = vTang;
		mNTB[1] = vBinorm;
		mNTB[2] = normalize(vNormal);
		mNTB = mat3(mMatrix) * mNTB;
	}
	
	WorldPos = (mMatrix * vVertex).xyz; 	
	TexCoord = vTexCoord;
	
	gl_Position = mvpMatrix * vVertex;	
}