package com.example.noisealerterpro;

import android.util.Log;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class AppData {
    public static final boolean USE_WEIGHTED_AVERAGE = true;
    public static final int FEATURE_DIMENSION = 62; //62; //40;
    public static final int MAX_DETECT_COUNT = 5;

    public static final int SOUND_TYPE_SMOKE_DETECTOR = 0;
    public static final int SOUND_TYPE_KNOCKING = 1;
    public static final int SOUND_TYPE_FOOTSTEPS = 2;
    public static final int SOUND_TYPE_GLASS_BREAKING = 3;
    public static final int SOUND_TYPE_DOORBELL = 4;
    public static final int SOUND_TYPE_BARKING = 5;
    public static final int SOUND_TYPE_OTHER_NOISE = 6;
    public static final int SOUND_TYPE_NUMBER = 7;

    private final int MAX_EVENT_NUM = 8;

    public static final boolean USE_CNN = true;
    public static String soundTypes[] = {"Smoke Detector", "Knocking", "Footsteps", "Glass Breaking", "Doorbell", "Barking", "Other Noises"};

    // map local sound ID to neural network model
    public static int mapLocalSoundType[] = {5, 3, 6, 2, 1, 0, 4};

    float[] predictResult;
    float[][] predictResults;
    float[] averageResult;
    double stddev;
    double zscore;

    int idleCount;
    int detectCount;
    String status;

    float maxDecibel;
    float avgDecibel;

    float chunkAmpMax, chunkAmpMin;

    int detectedSoundType;

    String detectedSound;

    List<String> events;

    AudioSignalPlotter bitmapAudioSignal;
    SpectrogramPlotter bitmapSpectrum;
    MFCCPlotter bitmapMfcc;
    PredictionPlotter bitmapPredict;
    FFTPlotter bitmapFFT;

    double[][] melSpectroGram;
    double[][] mfcc;
    double[][] stft;
    double[]   fftAmpl;

    float[][] frequencyResults;
    boolean isSmokeDetector;
    int peakDetectCount;

    int decibelMeterCount;
    float predictWeight;
    float predictWeights[];

    static AppData appData = null;

    private AppData() {
        predictResult = new float[SOUND_TYPE_NUMBER];
        predictResults = new float[MAX_DETECT_COUNT][SOUND_TYPE_NUMBER];
        predictWeights = new float[MAX_DETECT_COUNT];

        averageResult = new float[SOUND_TYPE_NUMBER];

        frequencyResults = new float[2][160];

        events = new LinkedList<String>();

        bitmapAudioSignal = new AudioSignalPlotter(1000, 200);

        bitmapFFT = new FFTPlotter(1000, 200);
        bitmapSpectrum = new SpectrogramPlotter(1000, 128);
        bitmapMfcc = new MFCCPlotter(1000, AppData.FEATURE_DIMENSION);
        bitmapPredict = new PredictionPlotter(1000, 28 * 7);

        detectCount = 0;
        idleCount = 0;

        detectedSoundType = -1;

        detectedSound = "unknown noise";

        // default standard deviation
        stddev = 36.0f;

        resetDetection();

    }

    public static AppData get() {
        if (appData == null)
            appData = new AppData();

        return appData;
    }

    public void addEvents(String event) {
        String strTime = java.text.DateFormat.getDateTimeInstance().format(new Date());
        events.add(strTime + ": " + event);
        if (events.size() > MAX_EVENT_NUM) {
            events.remove(0);
        }
    }

    public void addTimeDomainResult(float minAmp, float maxAmp, float avgDecibelAmp, float maxDecibelAmp) {
        chunkAmpMin = minAmp;
        chunkAmpMax = maxAmp;
        avgDecibel = avgDecibelAmp;
        maxDecibel = maxDecibelAmp;
        decibelMeterCount ++;
    }

    // weight is based on audio signal volume
    public void addPredictWeight(float weight) {
        weight = (float)Math.log10(weight * 800);

        if (weight > 1.0f)
            weight = 1.0f;
        else if (weight < 0.01f)
            weight = 0.01f;

        predictWeight = weight;

        if (detectCount > 0)
            predictWeights[detectCount-1] = weight;
    }

    // result is probability of each category of sound
    public void addNeuralNetResult(float[] result) {
        for (int i = 0; i < SOUND_TYPE_NUMBER; i ++) {
            predictResult[i] = result[mapLocalSoundType[i]];
            predictResults[detectCount - 1][i] = predictResult[i];
        }

        if (USE_WEIGHTED_AVERAGE) {
            float totalWeight = 0.0f;

            // Get the current average score
            for (int i = 0; i < result.length; i++) {
                averageResult[i] = 0;
                totalWeight = 0.0f;
                for (int j = 0; j < detectCount; j++) {
                    averageResult[i] += predictResults[j][i] * predictWeights[j];
                    totalWeight += predictWeights[j];
                }
                averageResult[i] /= totalWeight;
            }
        }
        else {
            // get current average score
            for (int i = 0; i < result.length; i++) {
                averageResult[i] = 0;
                for (int j = 0; j < detectCount; j++)
                    averageResult[i] += predictResults[j][i];
                averageResult[i] /= detectCount;
            }
        }

        // get final classification result
        if (detectCount == MAX_DETECT_COUNT) {
            int type = 0;
            for (int i = 0; i < result.length; i ++) {
                if (averageResult[i] > averageResult[type]) {
                    type = i;
                }
            }

            // map neural model sound type to the local sound type
            detectedSoundType = type;
            detectedSound = soundTypes[type];
        }
    }

    public void clearNeuralNetResult() {
        detectedSoundType = -1;
        detectedSound = "";
        for (int i = 0; i < predictResult.length; i ++) {
            predictResult[i] = 0.0f;
            averageResult[i] = 0.0f;
            for (int j = 0; j < MAX_DETECT_COUNT; j++)
                predictResults[j][i] = 0;
        }
    }

    public void resetDetection() {
        detectCount = 0;
        peakDetectCount = 0;
        decibelMeterCount = 0;
        predictWeight = 1.0f;
        status = "idle...";
    }

    public void addFrequencyPeakResult(int peakIndex, double amplitude, int fftSize, int fftNum) {
        final float SMOKE_DETECTOR_FREQ_MIN = 2900.0f;
        final float SMOKE_DETECTOR_FREQ_MAX = 4000.0f;

        frequencyResults[0][peakDetectCount] = (float)peakIndex * 22050.0f / 2.0f * fftSize;
        frequencyResults[1][peakDetectCount] = (float)amplitude;

        peakDetectCount++;
        if (peakDetectCount >= MAX_DETECT_COUNT * fftNum) {
            int numSuccess = 0;
            for (int i = 0; i < peakDetectCount; i++) {
                if (frequencyResults[0][i] > SMOKE_DETECTOR_FREQ_MIN
                        && frequencyResults[0][i] < SMOKE_DETECTOR_FREQ_MAX
                        && frequencyResults[1][i] > 5.0f) {
                    numSuccess++;
                    if (numSuccess > 10)
                        break;
                } else
                    numSuccess = 0;
            }
            if (numSuccess > 10)
                isSmokeDetector = true;

            // validate using DSP to make sure this is a smoke detector alarm
            if (!isSmokeDetector && (detectedSoundType == SOUND_TYPE_SMOKE_DETECTOR)) {
                detectedSoundType = SOUND_TYPE_OTHER_NOISE;
                detectedSound = soundTypes[detectedSoundType];
            }
        }
    }
}
