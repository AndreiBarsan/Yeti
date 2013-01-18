#version 330 core

layout(location = 0) in vec4 vVertex;

out vec2 UV;

void main(){
	gl_Position =  vVertex;
	UV = ( vVertex.xy + vec2(1, 1) ) / 2.0f;
}

