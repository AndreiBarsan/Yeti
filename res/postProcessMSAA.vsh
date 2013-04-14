#version 330

in vec4 vVertex;
in vec2 vTexCoord;

out vec2 vVaryingTexCoords;

void main(void) {
	// Just pass the data through
	vVaryingTexCoords = vTexCoord;
	gl_Position = vVertex;
}
