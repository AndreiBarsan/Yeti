#version 400 core

layout(location = 0) out float fragmentDepth;

void main(void) {
	// TODO: check if it works without the following line; it should!
	fragmentDepth = gl_FragCoord.z;
}
