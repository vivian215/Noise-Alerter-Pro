package com.example.noisealerterpro;

import android.graphics.Color;

public class AudioSignalPlotter extends BitmapPlotter{

    AudioSignalPlotter(int w, int h) {
        super(w, h);
        bmp.eraseColor(Color.argb(255, 0, 0, 0));
    }

    void updateBitmap()
    {
        int y0, y1;

        int x = curX;
        int yyMax, yyMin;

        yyMax = (int) AppData.get().chunkAmpMax;
        yyMin = (int) AppData.get().chunkAmpMin;
        y0 = (yyMin + 32768) * height / 2 / 32768;
        y1 = (yyMax + 32768) * height / 2 / 32768;
        if (y1 == y0) y1 = y0 + 2;
        if (y0 < 0) y0 = 1;
        if (y1 > height) y1 = height - 1;
        int color = Color.GREEN;
        if (AppData.get().detectCount > 0)
            color = Color.RED;

        for (int y = 0; y < height; y++) {
            if (y >= y0 && y < y1)
                bmp.setPixel(x, y, color);
            else
                bmp.setPixel(x, y, Color.BLACK);
        }

        curX++;
        if (curX >= width) {
            curX = 0;
        }
    }
}
