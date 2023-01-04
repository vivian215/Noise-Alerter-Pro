package com.example.noisealerterpro;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.example.noisealerterpro.ThirdParty.MFCC;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/*Machine Learning Processor
uses neural network to classify sounds based on features of frequency domain
*/
public class MLProcessor implements ISampleProcessor {
    final static int INPUT_BUFFER_SIZE = 20480;
    final static int NNET_OUTPUT_NUMBER = 7; // previously it is 3 for 3 sound types
    final static int MAX_DETECT_COUNT = 5;
    final static float SMOKE_DETECTOR_FREQ_MIN = 2900.0f; //HZ
    final static float SMOKE_DETECTOR_FREQ_MAX = 4000.0f; //HZ
    Interpreter interpreter;
    Context context;
    double[] inputBuffer;

    int featureDim;
    short[] shortBuffer;
    int inputBufferIndex;
    FFTProcessor fftProcessor = null;
    DecibelProcessor decibelMeter = null;
    boolean detectStarted;
    int detectCount;
    Utils utils;
    FileOutputStream writer;

    MLProcessor(Context context) {
        inputBuffer = new double[INPUT_BUFFER_SIZE];
        shortBuffer = new short[INPUT_BUFFER_SIZE];
        inputBufferIndex = 0;

        this.context = context;

        try {
            interpreter = new Interpreter(loadModelFile(context));
        }
        catch (IOException e) {
            Log.e("Exception", "model loading failed: " + e.toString());
        }

        fftProcessor = new FFTProcessor();
        decibelMeter = new DecibelProcessor();

        decibelMeter.init();
        fftProcessor.init();

        detectStarted = false;

        featureDim = AppData.FEATURE_DIMENSION;

        AppData.get().detectCount = detectCount = 0;

        AppData.get().status = "initializing...";

        utils = new Utils(context);
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        int audioResourceId;

        if (AppData.USE_CNN)
            audioResourceId = R.raw.modelcnn62;
        else
            audioResourceId = R.raw.model;

        AssetFileDescriptor descriptor = context.getResources().openRawResourceFd(audioResourceId);
        FileInputStream fis = new FileInputStream(descriptor.getFileDescriptor());
        FileChannel channel = fis.getChannel();
        return channel.map(FileChannel.MapMode.READ_ONLY, descriptor.getStartOffset(), descriptor.getDeclaredLength());
    }

    // extracts features of audio and sends to neural network for classification
    private void processAudioSamples(short[] audioBuffer, int size) {
        for (int i = 0; i < size; i ++) {
            // waits until enough samples to extract features
            if (inputBufferIndex < INPUT_BUFFER_SIZE) {
                // normalizes raw audio data to -1 <--> 1
                inputBuffer[inputBufferIndex] = ((double) audioBuffer[i])/32768.0F;
                shortBuffer[inputBufferIndex] = (short) audioBuffer[i];
                inputBufferIndex++;
                if (inputBufferIndex >= INPUT_BUFFER_SIZE) {
                    float[] features = extractFeatures(inputBuffer);
                    if (detectStarted) {
                        if (AppData.USE_CNN)
                            predictCNN(AppData.get().mfcc);
                        else
                            predictANN(features);

                        // validate if this is the sound from a smoke detector using non-ml method
                        detectSmokeDetector();

                        detectCount++;
                        AppData.get().detectCount = detectCount;
                        AppData.get().idleCount = 0;
                    }
                    else
                        AppData.get().idleCount ++;

                    if (AppData.get().idleCount >= 36) {
                        AppData.get().clearNeuralNetResult();
                    }
                    AppData.get().bitmapPredict.updateBitmap();
                    inputBufferIndex = 0;
                }
            }
        }
    }

    private void detectSmokeDetector() {
        double[][] stft = AppData.get().stft;
        int numFFT = stft[0].length;
        for (int i = 0; i < numFFT; i ++) {
            int peakIndex = 0;
            for (int j = 0; j < stft.length; j ++) {
                if (stft[j][i] > stft[peakIndex][i]) {
                    peakIndex = j;
                }
            }
            AppData.get().addFrequencyPeakResult(peakIndex, stft[peakIndex][i], stft.length, numFFT);
        }
    }

    private float[] extractFeatures(double[] audioInput) {
        // MFCC java library
        MFCC mfcc = new MFCC();

        float[] features = mfcc.process(audioInput);

        // calculate weight for neural network prediction based on signal strength
        float predictWeight = 0.0f;
        for (int i = 0; i < INPUT_BUFFER_SIZE; i ++) {
            predictWeight += Math.abs(audioInput[i]);
        }

        predictWeight /= (float)INPUT_BUFFER_SIZE;

        AppData.get().addPredictWeight(predictWeight);

        AppData.get().melSpectroGram = mfcc.getMelSpectroGram();
        AppData.get().bitmapSpectrum.updateBitmap();

        AppData.get().mfcc = mfcc.getMfcc();
        AppData.get().bitmapMfcc.updateBitmap();

        AppData.get().stft = mfcc.getStft();

        return features;
    }

    // predicts sound classification based on features from audio signal
    private void predictANN(float[] features) {
        float[][] output = new float[1][NNET_OUTPUT_NUMBER];

        ByteBuffer neuralInput = ByteBuffer.allocateDirect(featureDim*4).order(ByteOrder.nativeOrder());
        for (int i = 0; i < featureDim; i++) {
            neuralInput.putFloat(features[i]);
        }
        interpreter.run(neuralInput, output);

        AppData.get().status = "Classifying... ";

        AppData.get().addNeuralNetResult(output[0]);
    }

    // predicts sound classification based on features from audio signal
    private void predictCNN(double[][] features) {
        float[][] output = new float[1][NNET_OUTPUT_NUMBER];

        ByteBuffer neuralInput = ByteBuffer.allocateDirect(featureDim*4*21).order(ByteOrder.nativeOrder());

        for (int i = 0; i < featureDim; i++) {
            for (int j = 0; j < 21; j++) {
                neuralInput.putFloat((float)features[i][j]);
            }
        }
        interpreter.run(neuralInput, output);

        AppData.get().status = "Classifying... ";

        AppData.get().addNeuralNetResult(output[0]);
    }

    @Override
    public void init() {
        detectCount = 0;
        inputBufferIndex = 0;
        detectStarted = false;
        writer = utils.openBinaryFile(context.getFilesDir(), "smp");
    }

    @Override
    public void close() {
        utils.closeBinaryFile(writer);
    }

    @Override
    public void run(short[] samples, int size) {
        decibelMeter.run(samples, size);
        fftProcessor.run(samples, size);

        // starts detecting if volume passes threshold
        if (!detectStarted) {
            if ((AppData.get().avgDecibel > 60.0f && Math.log10(AppData.get().zscore) > 2.5) || AppData.get().maxDecibel > 82.0f) {
                AppData.get().status = "start detecting...";
                detectStarted = true;
                AppData.get().detectCount = detectCount = 1;
                AppData.get().addEvents("Loud sound is detected!");
            }
        }

        // extracts features of audio and sends to neural network for classification
        processAudioSamples(samples, size);

        if (detectStarted && detectCount <= MAX_DETECT_COUNT) {
            // empty
        } else {
            if (detectCount >= MAX_DETECT_COUNT) {
                AppData.get().addEvents(AppData.get().detectedSound + " is detected!");
            }

            detectStarted = false;
            detectCount = 0;
            AppData.get().resetDetection();
        }
    }
}

