package com.example.noisealerterpro;

import com.example.noisealerterpro.ThirdParty.FFTColumbia;

public class FFTProcessor implements ISampleProcessor {
    final static int FFT_BUFFER_SIZE = 2048;

    double[][] fftBuffer;
    double[] fftAmplBuffer;
    int sampleIndex;

    @Override
    public void init() {
        fftBuffer = new double[2][FFT_BUFFER_SIZE];
        fftAmplBuffer = new double[FFT_BUFFER_SIZE];
        sampleIndex = 0;
    }

    @Override
    public void close() {
    }

    @Override
    public void run(short[] samples, int size) {
        if (size <= 0)
            return;
        processAudioSamples(samples, size);
    }

    private void processAudioSamples(short[] audioBuffer, int size) {
        for (int i = 0; i < size; i++) {
            if (sampleIndex < FFT_BUFFER_SIZE) {
                // normalize raw audio data to -1 <--> 1
                fftBuffer[0][sampleIndex] = ((double) audioBuffer[i])/32768.0;
                fftBuffer[1][sampleIndex] = 0;
                sampleIndex++;
                if (sampleIndex >= FFT_BUFFER_SIZE) {
                    processFFTData(fftBuffer, FFT_BUFFER_SIZE);
                    sampleIndex = 0;
                }
            }
        }
    }

    private void processFFTData(double[][] buffer, int size) {
        FFTColumbia fftColumbia = new FFTColumbia(FFT_BUFFER_SIZE);

        if (size < FFT_BUFFER_SIZE)
            return;

        double[] re = buffer[0];
        double[] im = buffer[1];

        fftColumbia.fft(re, im);

        for (int i = 0; i < size; i ++) {
            fftAmplBuffer[i] = Math.sqrt(re[i] * re[i] + im[i] * im[i]);
        }

        AppData.get().fftAmpl = fftAmplBuffer;
        AppData.get().bitmapFFT.updateBitmap();
    }
}
