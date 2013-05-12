#version 400 core

uniform float far;
uniform vec4 lightPos_wc;

in vec4 WS_pos_from_GS;

void main(void) {
  	// calculate distance
	float WS_dist = distance(WS_pos_from_GS, lightPos_wc);

  	// map value to [0;1] by dividing by far plane distance
	float WS_dist_normalized = WS_dist / far;

  	// write modified depth
	gl_FragDepth = WS_dist_normalized;

	// when using depth-only FBO, do NOT write to color!!!
}