#version 150
uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float HeatFraction;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    const float PI = 3.141592653589793;
    // GUI UVs point downwards: begin at six o'clock and reveal clockwise.
    float angle = mod(atan(texCoord.y - 0.5, texCoord.x - 0.5) - PI * 0.5 + PI * 2.0, PI * 2.0);
    if (angle > clamp(HeatFraction, 0.0, 1.0) * PI * 2.0) discard;
    fragColor = texture(Sampler0, texCoord) * vertexColor * ColorModulator;
    if (fragColor.a < 0.1) discard;
}
