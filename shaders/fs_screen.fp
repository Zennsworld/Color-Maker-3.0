#version 410

in vec3 fragmentUV;
layout(location = 0) out vec4 outputColor;
uniform sampler2DArray textures;

void main() {
	outputColor = texture(textures, fragmentUV).rgba;
}