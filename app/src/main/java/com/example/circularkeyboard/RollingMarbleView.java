package com.example.circularkeyboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

// Class representing ImageView that will be shown on top of the keyboard layout,
// resembling the rolling marble.
public class RollingMarbleView extends androidx.appcompat.widget.AppCompatImageView {

    private int startX;
    private int startY;
    private int marbleW;
    private int marbleH;
    private int marbleCenterW;
    private int marbleCenterH;
    private int w, h;

    Bitmap marbleBitmap;
    RectF mRect;
    Paint mPaint;

    // New variables for alphabet drawing
    private Paint textPaint;
    private int radiusMargin = 115; // Adjust as needed for your design
    private int textRadius; // Radius where the text will be drawn

    private int textSize = 60;

    // Constructor
    public RollingMarbleView(Context context, int diameter, int w, int h) {
        super(context);

        this.marbleW = diameter;
        this.marbleH = diameter;
        this.w = w;
        this.h = h;

        this.startX = this.w/2 - this.marbleW/2;
        this.startY = this.h/2 - this.marbleH/2;

        this.marbleCenterW = this.startX;
        this.marbleCenterH = this.startY;

        mPaint = new Paint();
        mRect = new RectF();
        // Rolling marble bitmap:
        marbleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.marble);

        // Initialize text paint and text radius
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK); // Set text color
        textPaint.setTextSize(textSize); // Set text size
        textPaint.setTextAlign(Paint.Align.CENTER);

        textRadius = (Math.min(w, h) / 2) - radiusMargin; // Adjust radius as needed
    }


    // onDraw -- just show the bitmap
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the marble
        mRect.set(startX, startY, startX + marbleW, startY + marbleH);
        canvas.drawBitmap(marbleBitmap, null, mRect, mPaint);

        // Define the alphabet as an array of strings
        String[] alphabet = {"a", "b", "c", "d", "e", "f", "g", "SP", "h", "i", "j", "k", "l", "m",
                "n", "o", "p", "q", "r", "s", "t", "DEL", "u", "v", "w", "x", "y", "z"};
        float angleStep = 360f / alphabet.length;

        for (int i = 0; i < alphabet.length; i++) {
            // Calculate angle for clockwise direction
            float angle = (float) Math.toRadians((i * angleStep) - 90); // Adjust for clockwise

            float x = getWidth() / 2 + textRadius * (float) Math.cos(angle);
            float y = getHeight() / 2 + textRadius * (float) Math.sin(angle) + textPaint.getTextSize() / 2;

            canvas.drawText(alphabet[i], x, y, textPaint);
        }

        // Force a view to draw again
        invalidate();
    }





    // Getters and setters
    public int startX(){
        return this.startX;
    }

    public int startY(){
        return this.startY;
    }

    public int getMarbleW(){
        return this.marbleW;
    }

    public int getMarbleH(){
        return this.marbleH;
    }

    public int getMarbleCenterW(){
        return this.marbleCenterW;
    }

    public int getMarbleCenterH(){
        return this.marbleCenterH;
    }

    public void setStartX(int value){
        this.startX = value;
    }

    public void setStartY(int value){
        this.startY = value;
    }

    public void setMarbleW(int value){
        this.marbleW = value;
    }

    public void setMarbleH(int value){
        this.marbleH = value;
    }
}

