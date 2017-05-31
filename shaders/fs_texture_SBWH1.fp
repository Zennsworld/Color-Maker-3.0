#version 410

layout(location = 0) in vec2 positionF;
layout(location = 0) out vec4 outputColorF;
uniform vec4 hueRGB;

void main() {
	outputColorF = vec4 ((1-((1-hueRGB.rgb)*positionF.x))*positionF.y,hueRGB.a);
}