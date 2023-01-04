package com.example.noisealerterpro;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Button {
    private int x, y, width, height;
    private String text;
    private RectF rect;
    private boolean isHome;
    private Paint paint;

    //button constructor
    public Button(int x, int y, int screenWidth, int screenHeight, String text) {
        this.x = x;
        this.y = y;
        this.width = (int) (screenWidth/5);
        this.height = screenHeight/33;
        this.text = text;
        rect = new RectF(x, y, x + width, y + height);
        isHome = false;
        this.paint = new Paint();
        paint.setARGB(255, 183, 201, 226);
    }

    //checks if the button is clicked
    public boolean isClicked (float px, float py) {
        return px > x && px < x+width && py > y && py < y +height;
    }

    //draws the button
    public void draw(Canvas canvas, int paddingX, Paint outlinePaint, Paint textPaint) {
        canvas.drawRect(rect, paint);
        canvas.drawRect(rect, outlinePaint);
        canvas.drawText(text, x + paddingX, y + height/2 + 18, textPaint);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

