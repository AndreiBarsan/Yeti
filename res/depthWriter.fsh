#version 400 core

layout(location = 0) out float fragmentDepth;

void main(void) {
	fragmentDepth = gl_FragCoord.z;
}
