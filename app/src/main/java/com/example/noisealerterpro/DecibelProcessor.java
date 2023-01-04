package com.example.noisealerterpro;

import android.util.Log;

public class DecibelProcessor implements ISampleProcessor {
    private static final int SAMPLE_CHUNK_SIZE = 512*2;

    private int chunkIndex;
    private int[] chunkBuffer;

    @Override
    public void init() {
        chunkBuffer = new int[SAMPLE_CHUNK_SIZE];
        chunkIndex = 0;
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


    private void processChunkData(int[] buffer, int size) {
        int maxVolume = 0;
        int averageVolume = 0;
        int maxAmp = 0;
        int minAmp = 0;
        double mean = 0.0;

        for (int i = 0; i < size; i ++)
        {
            // get max amplitude
            if (buffer[i] > maxAmp)
                maxAmp = buffer[i];

            // get min amplitude
            if (buffer[i] < minAmp)
                minAmp = buffer[i];

            // get average amplitude in a chunk of samples
            int absAmp = Math.abs(buffer[i]);
            averageVolume += absAmp;

            mean += (double)buffer[i];
        }

        mean /= (double)size;

        // standard deviation
        double averagePower = 0.0;
        for (int i = 0; i < size; i ++) {
            double power = (double)buffer[i] - (double)mean;
            averagePower += power * power;
        }

        // filter standard deviation
        if (AppData.get().stddev > 50)
            AppData.get().stddev = 50;
        AppData.get().stddev = AppData.get().stddev * (1 - 0.001f) + Math.sqrt(averagePower / size) * 0.001f;

        // zscore
        AppData.get().zscore = (minAmp - mean) / AppData.get().stddev;

        // find max magnitude
        if (-minAmp > maxAmp) {
            maxVolume = -minAmp;
            AppData.get().zscore = (mean - minAmp) / AppData.get().stddev;
        } else {
            maxVolume = maxAmp;
            AppData.get().zscore = (maxAmp - mean) / AppData.get().stddev;
        }

        // convert linear amplitude to logscale decibel
        float avgDecibel = (float)(Math.log10((double)averageVolume / (double)size) * 20.0);
        float maxDecibel = (float)(Math.log10((double)maxVolume) * 20.0);

        // send results to appdata
        AppData.get().addTimeDomainResult((float)minAmp, (float)maxAmp, avgDecibel, maxDecibel);
        AppData.get().bitmapAudioSignal.updateBitmap();
    }

    private void processAudioSamples(short[] audioBuffer, int size) {
        for (int i = 0; i < size; i ++) {
            if (chunkIndex < SAMPLE_CHUNK_SIZE) {
                // normalize raw audio data to -1 <--> 1
                chunkBuffer[chunkIndex] = (int) audioBuffer[i];
                chunkIndex ++;
                if (chunkIndex >= SAMPLE_CHUNK_SIZE) {
                    processChunkData(chunkBuffer, SAMPLE_CHUNK_SIZE);
                    chunkIndex = 0;
                }
            }
        }
    }
}
