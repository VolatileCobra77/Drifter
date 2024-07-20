precision highp float;
//out vec4 fragColor;
//in vec3 worldPos;
//in float debugVal;
//
//void main() {
//    // Use gl_FragCoord to get the position of the fragment
//    vec2 fragPos = gl_FragCoord.xy;
//
//    // Normalize the position to range [0, 1]
//    vec3 normalizedPos = worldPos / vec3(10,10,10); // Assuming a resolution of 800x600
//
//    // Determine the color based on the normalized position
//
//    vec3 color = vec3(abs(normalizedPos.y), abs(normalizedPos.y), 1); // Example: red based on x, green based on y, constant blue
//
//    fragColor = vec4(color, 0.75f);//old water texture vec4(0.3705882353, 0.4568627451, 0.4882352941, 0.78); // Set the color with full opacity
//}


uniform sampler2D m_waterTexture;
uniform vec3 m_lightDirection;
uniform float m_matTime;


out vec4 fragColor;
in vec2 fragTexCoord;
in float fragWaveHeight;

// Simple function to generate procedural noise
float noise(vec2 uv) {
    return fract(sin(dot(uv.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

// Function to generate a dynamic water texture
vec4 dynamicWaterTexture(vec2 uv, float waveHeight) {
    float noiseValue = noise(uv * 10.0 + waveHeight * 0.1);
    float wavePattern = sin(uv.x * 10.0 + waveHeight) * 0.5 + 0.5;
    float otherWavePattern = sin(uv.y * 10.0 + waveHeight * 0.5 + 0.5);
    vec3 color = mix(vec3(0.0, 0.0, 0.5), vec3(0.0, 0.5, 1.0), wavePattern + otherWavePattern + noiseValue * 0.5);
    return vec4(color, 1.0);
}

void main() {
    vec2 uv = fragTexCoord;

    // Generate the dynamic water texture
    vec4 textureColor = dynamicWaterTexture(uv, fragWaveHeight);

    // Simple lighting calculation
    float lightIntensity = max(dot(normalize(vec3(0.0, 1.0, 0.0)), normalize(m_lightDirection)), 0.0);
    vec4 lightColor = vec4(vec3(lightIntensity), 0.99);

    // Combine texture color with light
    fragColor = textureColor;
}