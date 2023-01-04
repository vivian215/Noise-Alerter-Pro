package com.example.noisealerterpro;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class FFTPlotter extends BitmapPlotter{

    private Canvas canvas;
    private Paint paint;

    FFTPlotter(int w, int h) {
        super(w, h);
        plotHeight = 256;
        bmp.eraseColor(Color.argb(255, 0, 0, 0));
        canvas = new Canvas(bmp);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    void updateBitmap() {
        int color = Color.GREEN;
        if (AppData.get().detectCount > 0)
            color = Color.RED;

        bmp.eraseColor(Color.argb(255, 0, 0, 0));

        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(2);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);

          for (int i = 0; i < AppData.get().fftAmpl.length - 1; i ++) {
            float y1 = -(float)AppData.get().fftAmpl[i] * 10 + height / 2;
            float y2 = -(float)AppData.get().fftAmpl[i+1] * 10 + height / 2;
            canvas.drawLine(i, y1, i + 1, y2, paint);
        }
    }
}
