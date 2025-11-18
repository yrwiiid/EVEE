package com.example.evee;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class MoodBarChartView extends View {

    // Data 6 mood untuk grafik, bisa diganti sesuai input user
    private float[] values = {1, 4, 3, 6, 8, 5}; // skala 1-10
    private String[] labels = {"Marah","Sedih","Biasa","Senang","Bahagia","Excited"};
    private String[] emojis = {"😡","😞","😐","😊","😃","🤩"}; // Emoji sesuai label

    private Paint barPaint;
    private Paint textPaint;
    private Paint axisPaint;

    public MoodBarChartView(Context context) {
        super(context);
        init();
    }

    public MoodBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        barPaint = new Paint();
        barPaint.setColor(Color.parseColor("#8C183B"));
        barPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(36f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        axisPaint = new Paint();
        axisPaint.setColor(Color.BLACK);
        axisPaint.setStrokeWidth(4f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float paddingLeft = 120f;
        float paddingBottom = 160f; // ruang untuk emoji + label
        float paddingTop = 50f;

        // Sumbu Y
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height-paddingBottom, axisPaint);
        // Sumbu X
        canvas.drawLine(paddingLeft, height-paddingBottom, width-50f, height-paddingBottom, axisPaint);

        // Tentukan maxVal dari skala baru: 10
        float maxVal = 10;

        int count = values.length;
        float spacePerBar = (width - paddingLeft - 50f) / count;
        float barWidth = spacePerBar * 0.6f;

        // Batang, nilai, emoji, label
        for(int i=0;i<count;i++){
            float barHeight = (values[i]/maxVal)*(height-paddingBottom-paddingTop);
            float left = paddingLeft + i*spacePerBar + (spacePerBar-barWidth)/2;
            float top = height-paddingBottom - barHeight;
            float right = left + barWidth;
            float bottom = height-paddingBottom;

            // Batang
            canvas.drawRect(left, top, right, bottom, barPaint);

            // Nilai di atas batang (1-10)
            canvas.drawText(String.valueOf((int)values[i]), left+barWidth/2, top-10, textPaint);

            // Emoji + label di bawah batang
            float textY = bottom + 30;
            if(emojis != null && i < emojis.length){
                canvas.drawText(emojis[i], left + barWidth/2, textY, textPaint); // emoji utuh
                canvas.drawText(labels[i], left + barWidth/2, textY + 40, textPaint); // label
            } else {
                canvas.drawText(labels[i], left + barWidth/2, textY + 20, textPaint);
            }
        }

        // Nilai sumbu Y tiap 1 (1-10)
        textPaint.setTextAlign(Paint.Align.RIGHT);
        for(int i=1;i<=maxVal;i++){
            float y = height-paddingBottom - (i/maxVal)*(height-paddingBottom-paddingTop);
            canvas.drawText(""+i, paddingLeft-10, y+10, textPaint);
            canvas.drawLine(paddingLeft, y, width-50f, y, axisPaint);
        }

        textPaint.setTextAlign(Paint.Align.CENTER); // reset alignment
    }

    // Set data lengkap dengan emoji
    public void setData(float[] values, String[] labels, String[] emojis){
        this.values = values;
        this.labels = labels;
        this.emojis = emojis;
        invalidate();
    }

    // Set data tanpa emoji
    public void setData(float[] values, String[] labels){
        this.values = values;
        this.labels = labels;
        this.emojis = null;
        invalidate();
    }
}
