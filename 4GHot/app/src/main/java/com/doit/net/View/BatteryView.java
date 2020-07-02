package com.doit.net.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.doit.net.ucsi.R;

/**
 * Author：Libin on 2020/6/30 11:20
 * Email：1993911441@qq.com
 * Describe：
 */

public class BatteryView extends View {

    private int mPower = 100;
    private Paint strokePaint;
    private Paint valuePaint;
    private Paint headerPaint;

    int batteryWidth = 40;
    int batteryHeight = 20;

    int batteryHeadWidth = 3;
    int batteryHeadHeight = 6;

    int batteryPadding = 3;

    public BatteryView(Context context) {
        super(context);
    }

    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //先画外框
        strokePaint = new Paint();
        strokePaint.setColor(Color.WHITE);
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);


        valuePaint = new Paint();
        valuePaint.setStyle(Paint.Style.FILL);
        strokePaint.setAntiAlias(true);



        headerPaint = new Paint();
        headerPaint.setStyle(Paint.Style.FILL);
        strokePaint.setAntiAlias(true);
        headerPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        batteryWidth = MeasureSpec.getSize(widthMeasureSpec)-batteryHeadWidth;
        batteryHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //画边框
        canvas.drawRoundRect(0, 0, batteryWidth,batteryHeight,3,3, strokePaint);

        //画电量
        if (mPower <=20){
            valuePaint.setColor(Color.RED);
        }else if (mPower <=40){
            valuePaint.setColor(getResources().getColor(R.color.darkorange));
        }else {
            valuePaint.setColor(Color.WHITE);
        }
        float powerPercent = mPower / 100.0f;
        canvas.drawRect(batteryPadding, batteryPadding,
                (int) (batteryPadding+(batteryWidth- 2*batteryPadding)*powerPercent),
                batteryHeight-batteryPadding,
                valuePaint);

        //画电池头
        canvas.drawRoundRect(batteryWidth, (batteryHeight - batteryHeadHeight)/2,
                batteryWidth+batteryHeadWidth,(batteryHeight + batteryHeadHeight)/2,
                1.5f,1.5f,headerPaint);
    }

    public void setPower(int power) {
        mPower = power;
        if(mPower < 0) {
            mPower = 0;
        }
        if (mPower >100){
            mPower = 100;
        }
        invalidate();
    }

}
