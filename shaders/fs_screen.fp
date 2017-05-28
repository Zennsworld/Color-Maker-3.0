/*
 * Fragment shader.
 */

#version 400

in vec2 fragmentUV;
out vec4 outputColor;
uniform sampler2D textures;

void main() {
	outputColor = texture(textures, fragmentUV).rgba;
}