#version 400 core

layout(triangles) in;

layout(triangle_strip, max_vertices=18) out;

uniform mat4 cm_mat[6];

out vec4 WS_pos_from_GS;

void main(void) {
  	//iterate over the 6 cubemap faces
  	// gl_Layer = 0 <=> X+ ... gl_Layer = 5 <=> Z- 
	for(gl_Layer=0; gl_Layer<6; ++gl_Layer) {
    	for(int tri_vert=0; tri_vert<3; ++tri_vert) {
			WS_pos_from_GS = gl_in[tri_vert].gl_Position;
			gl_Position = cm_mat[gl_Layer] * WS_pos_from_GS;
			EmitVertex();
    	}
    	
	EndPrimitive();
  }
}