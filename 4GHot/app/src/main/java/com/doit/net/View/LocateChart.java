package com.doit.net.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.doit.net.Utils.UtilBaseLog;
import com.doit.net.ucsi.R;

/**
 * Created by Zxc on 2020/1/14.
 */

public class LocateChart extends View {
    int chartWidth;
    int chartHeight;
    private int cylinderSpace = 0;      //每一柱之间的间隔
    private int pointSpaceInCylinder = 0;   //每一点上下的间隔
    private int bottomCompst = 10;    //底部补偿高度
    private int startCompst = 25;    //左边边补偿宽度
    private float pointLength = 0;
    private float pointHeight = 0;

    private int maxPointCntInClder = 25;     //每一柱有效点的个数
    private int cylinderCount = 15;  //横坐标个数
    protected Paint mPaint = null;
    protected int[] mChartDatas = new int[cylinderCount];

    public LocateChart(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public void initChart(int chartWidth, int chartHeight) {
        pointLength = (3*(chartWidth))/(4* cylinderCount);
        pointHeight = (3*(chartHeight))/(4* maxPointCntInClder);
        cylinderSpace = (int) pointLength /3;
        pointSpaceInCylinder = (int) pointHeight / 3;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        //mPaint.setColor(getResources().getColor(R.color.darkorange));
        mPaint.setStrokeJoin(Paint.Join.BEVEL);
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
        mPaint.setStrokeWidth(pointHeight);
    }

    public void setMaxPointCntInClder(int count){
        maxPointCntInClder = count;
    }

    public void setCylinderCount(int count){
        cylinderCount = count;
    }

    protected void drawCylinder(Canvas canvas, float x, int value) {
        if (value < 0)
            value = 0;

        if (value > maxPointCntInClder)
            value = maxPointCntInClder;

        mPaint.setColor(getResources().getColor(R.color.darkorange));
        for (int i = 0; i < value; i++) {
            float y = chartHeight - i * (pointSpaceInCylinder + pointHeight)- bottomCompst;
            canvas.drawLine(x, y,  x+ pointLength - pointHeight, y, mPaint);  //系统会自动将长度加上宽度，所以这里将其减掉
        }

        mPaint.setColor(getResources().getColor(R.color.darkdarkorange));
        for (int i = value; i < maxPointCntInClder; i++) {
            float y = chartHeight - i * (pointSpaceInCylinder + pointHeight)- bottomCompst;
            canvas.drawLine(x, y,  x+ pointLength - pointHeight, y, mPaint);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int i = 0; i < cylinderCount; i ++) {
            drawCylinder(canvas,i * (pointLength +cylinderSpace)+ startCompst, mChartDatas[i]);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        UtilBaseLog.printLog("onSizeChanged");
        chartWidth = w;
        chartHeight = h;
        initChart(w,h);
    }

    public void updateChart(int[] chartDate) {
        mChartDatas = chartDate;
        postInvalidate();
    }
}
