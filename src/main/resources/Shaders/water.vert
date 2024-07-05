#version 330

// Input vertex position from vertex array
attribute vec3 inPosition;

// Uniform variables for wave parameters
uniform float g_Time;    // Time variable for animation
uniform float amplitude1;
uniform float frequency1;
uniform float phase1;

uniform float amplitude2;
uniform float frequency2;
uniform float phase2;

uniform float amplitude3;
uniform float frequency3;
uniform float phase3;

// Output position for the fragment shader
varying vec3 fragPos;

void main()
{
    // Calculate the wave offsets for x and y axes
    float wave1 = amplitude1 * sin(inPosition.x * frequency1 + g_Time * phase1);
    float wave2 = amplitude2 * sin(inPosition.y * frequency2 + g_Time * phase2);
    float wave3 = amplitude3 * sin(inPosition.x * frequency3 + inPosition.y * frequency3 + g_Time * phase3);

    // Sum the wave offsets
    float wave = wave1 + wave2 + wave3;

    // Apply the wave offset to the z-coordinate of the vertex position
    vec3 pos = inPosition;
    pos.z += wave; // Assuming z is the up-axis in a 3D space

    // Set the final vertex position
    gl_Position = gl_ModelViewProjectionMatrix * vec4(pos, 1.0);

    // Pass the position to the fragment shader
    fragPos = pos;
}
