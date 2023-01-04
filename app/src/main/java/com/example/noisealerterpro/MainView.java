package com.example.noisealerterpro;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

public class MainView extends SurfaceView implements Runnable {
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int SAMPLE_RATE = 22050; // Hz
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int CHANNEL_MASK = AudioFormat.CHANNEL_IN_MONO;

    private static final int FREQ_PLOT_TYPE_SPECTROGRAM = 0;
    private static final int FREQ_PLOT_TYPE_MFCC = 1;
    private static final int FREQ_PLOT_TYPE_FFT = 2;

    private static final int NOISE_ALERTER_VERSION = 1;

    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_MASK, ENCODING);

    private static final int PROC_TYPE_FFT = 1;
    private static final int PROC_TYPE_LOGGER = 2;
    private static final int PROC_TYPE_ML = 3;

    // interval to update canvas
    private final int INTERVAL_IN_MILISECOND = 17;

    boolean running = false;

    Thread mainThread;
    private String soundTypes[];
    private boolean isRecording;

    private AudioRecord audioRecorder = null;
    private short[] audioBuffer;

    private ISampleProcessor processor = null;

    int processorType = PROC_TYPE_ML;

    // for painting
    private int screenHeight, screenWidth;

    private Paint fillPaint, textPaint, outlinePaint;

    Button plotToggleButton;
    int currFreqPlot;

    // constructor
    public MainView(Context context, Resources res, int screenHeight, int screenWidth) {
        super(context);
        isRecording = false;

        audioRecorder = null;
        audioBuffer = new short[BUFFER_SIZE];
        if (processorType == PROC_TYPE_ML)
            processor = new MLProcessor(getContext());
        else if (processorType == PROC_TYPE_FFT)
            processor = new FFTProcessor();
        else
            processor = new SampleLogger(getContext());

        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;

        soundTypes = AppData.soundTypes;

        currFreqPlot = FREQ_PLOT_TYPE_SPECTROGRAM;

        initPainting();
    }

    private void startRecording() {
        try {
            audioRecorder = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_MASK, ENCODING, BUFFER_SIZE);
            audioRecorder.startRecording();
        } catch (Exception ex) {
            Log.i("Info", "AudioRecord open failed!");
        }
    }

    private void stopRecording() {
        audioRecorder.stop();
        audioRecorder.release();
        audioRecorder = null;
    }

    private void sleep() {
        try {
            Thread.sleep(INTERVAL_IN_MILISECOND);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        running = false;
        processor.close();

        try {
            mainThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        processor.init();

        mainThread = new Thread(this);
        mainThread.start();
        running = true;
    }

    public Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    void initPainting() {
        outlinePaint = new Paint();
        outlinePaint.setColor(Color.BLACK);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(5);

        textPaint = new Paint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(50);
        fillPaint = new Paint();

        plotToggleButton = new Button(810, 459, screenWidth, screenHeight, "Toggle");
    }

    void drawDecibelMeter(int y, Canvas canvas, Paint paint) {
        int topMargin = y + 98;
        int leftMargin = 39;
        float decibelMax = AppData.get().maxDecibel;
        int num = (int)(decibelMax / 90.0 * 255.0);
        int width = 768, height = 80;

        drawSectionTitle("Decibel Meter & Audio Signal", 39, y, canvas, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        Utils.drawRecessedFrame(leftMargin, topMargin,  leftMargin + width, topMargin + height, canvas, paint);
        Utils.drawRecessedFrame(leftMargin + width + 20, topMargin,  leftMargin + width + 231, topMargin + height, canvas, paint);

        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < num; i ++) {
            paint.setColor(Color.rgb(i, 255-i, 0));
            canvas.drawRect(leftMargin+i*3, topMargin,  leftMargin + i*3 + 3, topMargin + height, paint);
        }

        paint.setTextSize(80);
        canvas.drawText(String.format("%.1f", decibelMax), leftMargin + 768 + 50, topMargin + 70, paint);
    }

    void drawAudioSignal(int y, Canvas canvas, Paint paint) {
        AppData.get().bitmapAudioSignal.draw(39, y, canvas, paint);
    }

    void drawFreqGraph(int y, Canvas canvas, Paint paint) {
        String caption;
        // plot spectrogram
        if (currFreqPlot == FREQ_PLOT_TYPE_SPECTROGRAM) {
            caption = "Spectrogram";
            AppData.get().bitmapSpectrum.draw(39, y + 98, canvas, paint);
        }
        else if (currFreqPlot == FREQ_PLOT_TYPE_MFCC) {
            caption = "MFCC (Input for Neural Network)";
            AppData.get().bitmapMfcc.draw(39, y + 98, canvas, paint);
        }
        else {
            caption = "FFT Graph";
            AppData.get().bitmapFFT.drawStatic(39, y + 98, canvas, paint);
        }
        drawSectionTitle(caption, 39, y, canvas, paint);
    }

    void drawClassificationResults(int y, Canvas canvas, Paint paint) {
        int x = 39;
        int top = y;
        int rowSpace = 56;
        int radius = 21;

        int colorResult[] = {0, 0, 0, 0, 0, 0, 0};

        float[] result = AppData.get().predictResult;

        drawSectionTitle("Machine Learning Detection Result", 39, y+2, canvas, paint);

        y += 100                                                                                                                                                                                                                           ;
        AppData.get().bitmapPredict.draw(39,  y + 2, canvas, paint);

        y += (rowSpace + 3);

        for (int i = 0; i < soundTypes.length; i++) {
            // clear bg on left of ml detection result
            paint.setColor(Color.WHITE);
            canvas.drawRect(x, y + (i-1) * rowSpace + 1, x + 339, y + i * rowSpace - 2,  paint);
            paint.setColor(Color.argb(255, 183, 201, 226));
            canvas.drawRect(x, y + (i-1) * rowSpace + 1,  x + radius * 2 + 6, y + i * rowSpace - 2,  paint);

            paint.setStrokeWidth(2);
            int color = (int)(result[i] * 255.0);
            if (AppData.get().detectCount > 0) {
                paint.setColor(Color.rgb(255 - color, color, 0));
            }
            else {
                if (i == AppData.get().detectedSoundType)
                    paint.setColor(Color.GREEN);
                else
                    paint.setColor(Color.RED);
            }
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(x + radius + 2, y + i * rowSpace - radius - 7, radius, paint);

            // show intermediate average results
            paint.setColor(Color.GREEN);
            float avgScore = AppData.get().averageResult[i];

            // show overall classification result
            int barWidth = (int)(avgScore * 200.0f);
            if (barWidth < 2)
                barWidth = 2;

            canvas.drawRect(x + radius * 2 + 26, y + (i-1) * rowSpace + 1, x + radius * 2 + 26 + barWidth, y + i * rowSpace - 2,  paint);

            // fill color to indicate the classification result
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            canvas.drawCircle(x + radius + 2, y + i * rowSpace - radius - 7, radius, paint);

            // show name of sound
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(2);
            paint.setTextSize(36);
            canvas.drawText(soundTypes[i], x + radius * 3 + 3, y + i * rowSpace - 16, paint);

            if (i == 0)
                canvas.drawLine(x, y  - rowSpace, x + 1000, y - rowSpace, paint);

            canvas.drawLine(x, y + i * rowSpace, x + 1000, y + i * rowSpace, paint);
        }

        Utils.drawRecessedFrame(x, top+102,  x + radius * 2 + 6, y+ (soundTypes.length - 1) * rowSpace, canvas, paint);
        Utils.drawRecessedFrame(x+radius * 2 + 25, top+102,  x + 319, y+ (soundTypes.length - 1) * rowSpace, canvas, paint);
        Utils.drawRecessedFrame(x+339, top+102,  x + 1000, y+ (soundTypes.length - 1) * rowSpace, canvas, paint);
    }

    void drawEvents(int y, Canvas canvas, Paint paint) {
        int x = 39;

        drawSectionTitle("Events (" + AppData.get().status + ")", 39, 1385, canvas, paint);

        Utils.drawRecessedFrame(x, y, 39 + 1000, y + 299, canvas, paint);

        // show event messages
        for (int i = 0; i < AppData.get().events.size(); i++) {
            String event = AppData.get().events.get(i);
            y += 36;
            if (i == AppData.get().events.size()-1) {
                paint.setColor(Color.RED);
            } else {
                paint.setColor(Color.BLACK);
            }
            canvas.drawText(event, x + 6, y, paint);
        }
    }

    void drawSectionTitle(String caption, int x, int y, Canvas canvas, Paint paint) {
        Rect rect = new Rect(x, y, 1000+x,y + 78);
        Utils.drawRecessedFrame(x, y, 1000+x, y+78, canvas, paint);
        paint.setColor(Color.BLUE);
        canvas.drawRect(rect, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(39);
        canvas.drawText(caption, x, y+50, paint);

    }

    void drawGUIComponents(Canvas canvas) {
        //draw home button
        textPaint.setColor(Color.BLACK);
        plotToggleButton.draw(canvas, 30, outlinePaint, textPaint);
    }

    public void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = this.getHolder().lockCanvas();
            canvas.drawARGB(255, 183, 201, 226);

            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(80);

            // draw decibel meter
            drawDecibelMeter(12, canvas, paint);

            // draw audio signal
            drawAudioSignal(210, canvas, paint);

            // draw spectrogram, FFT graph, MFCC
            drawFreqGraph(452, canvas, paint);

            // draw scrolling version of detection results
            drawClassificationResults(850, canvas, paint);

            // draw events
            drawEvents(1483, canvas, paint);

            drawGUIComponents(canvas);

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public void run() {
        while (running) {
            if (!isRecording) {
                Log.d("monitor", "Yessir1");
                if (audioRecorder != null) {
                    stopRecording();
                }
                startRecording();
                isRecording = true;
            }

            if (isRecording) {
                int size = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    size = audioRecorder.read(audioBuffer, 0, audioBuffer.length, AudioRecord.READ_NON_BLOCKING);
                    processor.run(audioBuffer, size);
                }
            }
            draw();
            sleep();
        }
    }

    public boolean onPressEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                if (plotToggleButton.isClicked(x, y)) {
                    currFreqPlot ++;
                    if (currFreqPlot > FREQ_PLOT_TYPE_FFT)
                        currFreqPlot = 0;
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        onPressEvent(event);
        return true;
    }
}
