#version 400

in vec2 positionF;
out vec4 outputColorF;
uniform vec4 hueRGB;

void main() {
	outputColorF = vec4 ((1-((1-hueRGB.rgb)*positionF.x))*positionF.y,hueRGB.a);
}