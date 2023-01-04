package com.example.noisealerterpro;

import android.graphics.Color;

public class SpectrogramPlotter extends BitmapPlotter{

    SpectrogramPlotter(int w, int h) {
        super(w, h);
        plotHeight *= 2;
        int red = 36;
        bmp.eraseColor(Color.argb(255, red, (255-red)*8/10, (255-red)));
    }

    void updateBitmap()
    {
        double[][] spectrum = AppData.get().melSpectroGram;
        int w = spectrum[0].length - 1;

        for (int x = 0; x < w; x ++) { //20
            if (x+curX >= width)
                break;
            for (int y = 0; y < spectrum.length; y++) { //128
                float red = (float) (spectrum[spectrum.length-y-1][x] + 80)/160;
                bmp.setPixel(x + curX, y, Color.rgb(red, 0.8f*(1.0f-red), 1.0f*(1.0f-red)));
            }
        }
        curX += w;
        if (curX >= width) {
            curX = 0;
        }
    }

}
