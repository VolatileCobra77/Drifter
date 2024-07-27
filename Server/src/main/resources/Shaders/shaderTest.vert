precision highp float;
in vec3 inPosition;
uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;
uniform float m_matTime;
uniform float m_sineNum;
uniform vec2 m_offset;

in vec2 inTexCoord;

float sinWaveFunc(float time, float amplitude, float coordinate, float wavelength, float speed) {
    float freq = 2/wavelength;
    float phase = speed * freq;
    float result = amplitude * sin(coordinate * freq + time * phase);
    return result;
}




float sumOfSines(float time, float coordinate, float sineNum) {
    float sum = 0.0;
    for (int i = 0; i < sineNum; i++) {
        float amplitude = 1.0 / (i + 1); // Example amplitude calculation
        float wavelength = 1.0 + float(i) * 0.1; // Example wavelength calculation
        float speed = 1.0 + float(i) * 0.05;// Example speed calculation
        float randomVal = 0.2;
        sum += sinWaveFunc(time, amplitude + randomVal, coordinate* randomVal, wavelength* randomVal, speed * randomVal);
    }

    return sum;
}

out vec3 worldPos;

out vec2 fragTexCoord;
out float fragWaveHeight;

void main() {
    float globalScalar = 0.3;

    float yCoord = (sumOfSines(m_matTime, inPosition.x, m_sineNum) + sumOfSines(m_matTime, inPosition.z, m_sineNum)) * globalScalar;

    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition.x, yCoord, inPosition.z, 1.0);

    vec3 tempWorldPos = (g_WorldMatrix * vec4(inPosition.x, yCoord, inPosition.z, 1.0)).xyz;

    fragTexCoord = inTexCoord;
    fragWaveHeight = yCoord;

    worldPos =  tempWorldPos;
}