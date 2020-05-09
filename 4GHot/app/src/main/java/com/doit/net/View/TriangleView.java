package com.doit.net.View;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Author：Libin on 2020/5/9 09:43
 * Email：1993911441@qq.com
 * Describe：三角形
 */
public class TriangleView extends View {
    public TriangleView(Context context) {
        super(context);
    }

    public TriangleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        //实例化路径
        Path path = new Path();
        path.moveTo(0,0);
        path.lineTo(getMeasuredWidth(),0);
        path.lineTo(getMeasuredWidth(),getMeasuredHeight());
        path.close();

        canvas.drawPath(path,paint);
    }
}
