/*
 * Fragment shader.
 */

#version 400

in vec3 fragmentUV;
out vec4 outputColor;
uniform sampler2DArray textures;

void main() {
	outputColor = texture(textures, fragmentUV).rgba;
}