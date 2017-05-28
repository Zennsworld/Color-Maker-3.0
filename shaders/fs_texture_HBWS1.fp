#version 400

in vec2 positionF;
out vec4 outputColorF;
uniform float sat;
uniform float alpha;

vec3 RGB;

void main() {
    RGB = vec3(positionF.x*6, positionF.x*6+4, positionF.x*6+2);
	RGB = max(min(abs(3-mod(RGB, 6))-1,1),0);
	outputColorF = vec4 ((1-(1-RGB)*sat)*positionF.y, 1);
}