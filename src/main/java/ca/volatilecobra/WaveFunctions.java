package ca.volatilecobra;

public class WaveFunctions {

    public float sinWaveFunc(float time, float amplitude, float coordinate, float wavelength, float speed) {
        float freq = 2 / wavelength;
        float phase = speed * freq;
        float result = amplitude * (float) Math.sin(coordinate * freq + time * phase);
        return result;
    }

    public float sumOfSines(float time, float coordinate, float sineNum) {
        float sum = 0.0f;
        for (int i = 0; i < sineNum; i++) {
            float amplitude = 1.0f / (i + 1); // Example amplitude calculation
            float wavelength = 1.0f + i * 0.1f; // Example wavelength calculation
            float speed = 1.0f + i * 0.05f; // Example speed calculation
            float randomVal = 0.2f;
            sum += sinWaveFunc(time, amplitude + randomVal, coordinate * randomVal, wavelength * randomVal, speed * randomVal);
        }
        return sum;
    }
}