#version 400

in vec2 positionF;
out vec4 outputColorF;
uniform float bri;
uniform float hueOffset;
uniform float alpha;

vec3 RGB;

void main() {
    RGB = vec3(positionF.y*6, positionF.y*6+4, positionF.y*6+2) + hueOffset;
	RGB = max(min(abs(3-mod(RGB, 6))-1,1),0);
	outputColorF = vec4 ((1-(1-RGB)*positionF.x)*bri, alpha);
}