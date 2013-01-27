#version 400 core

const float far = 100.0f;

uniform vec4 l_pos; // world space light position
in vec4 WS_pos_from_GS;

void main(void) {
  	// calculate distance
	float WS_dist = distance(WS_pos_from_GS, l_pos);

  	// map value to [0;1] by dividing by far plane distance
	float WS_dist_normalized = WS_dist / far;

  	// write modified depth
	gl_FragDepth = WS_dist_normalized;

	// when using depth-only FBO, do NOT write to color!!!
}