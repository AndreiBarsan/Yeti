#version 400 core

in vec4 vVertex;

out vec2 UV;

void main(){
	gl_Position =  vVertex;
	UV = vVertex.xy;		// we're no longer mapping to a 0..1 texture, but
							// to a -1..1 cube!
}

