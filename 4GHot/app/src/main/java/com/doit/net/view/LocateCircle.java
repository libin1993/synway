package com.doit.net.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.doit.net.ucsi.R;

/**
 * Created by Zxc on 2020/1/14.
 */
public class LocateCircle extends View {
    private Context mContext;
    //默认大小
    private int mDefaultSize;
    //是否开启抗锯齿
    private boolean antiAlias;
    //绘制提示
    private TextPaint mHintPaint;
    private CharSequence mHint;
    private int mHintColor;
    private float mHintSize;
    private float mHintOffset;

    //绘制下方文字
    private TextPaint mUnitPaint;
    private CharSequence mUnit;
    private int mUnitColor;
    private float mUnitSize;
    private float mUnitOffset;

    //绘制中间数值
    private TextPaint mValuePaint;
    private float mValue;
    private float mMaxValue;
    private float mValueOffset;
    private int mPrecision;
    private String mPrecisionFormat;
    private int mValueColor;
    private float mValueSize;

    //绘制中间背景
    private Paint mValueBkgPaint;
    private int mValueBkgColor;
    private RectF mRectValueBkg;

    //绘制圆弧
    private Paint mArcPaint;
    private float mArcWidth;
    private float mStartAngle, mSweepAngle;
    private RectF mRectF;
    //当前进度，[0.0f,1.0f]
    private float mPercent;
    //动画时间
    private long mAnimTime;
    //属性动画
    private ValueAnimator mAnimator;

    //绘制背景圆弧
    private Paint mBgArcPaint;
    private int mBgArcColor;
    private int mArcColor;
    private float mBgArcWidth;

    //圆心坐标，半径
    private Point mCenterPoint;
    private float mRadius;
    private float mTextOffsetPercentInRadius;

    // 刻度
    private Paint mPaintScale;
    private int mScaleColor;
    private float mScaleSize;

    private int mArcCenterX;
    // 内部虚线的外部半径
    private float mExternalDottedLineRadius;
    // 内部虚线的内部半径
    private float mInsideDottedLineRadius;

    // 线条数
    private int mDottedLineCount = 120;
    // 圆弧跟虚线之间的距离
    private int mLineDistance = 25;
    // 线条宽度
    private float mDottedLineLength = 40;
    //是否使用渐变
    protected boolean useGradient=true;
    //前景色起始颜色
    private int foreStartColor;
    //前景色结束颜色
    private int foreEndColcor;

    public LocateCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mDefaultSize = dipToPx(mContext, 150);
        mAnimator = new ValueAnimator();
        mRectF = new RectF();
        mRectValueBkg = new RectF();
        mCenterPoint = new Point();
        initAttrs(attrs);
        initPaint();
        setValue(mValue);
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);

        antiAlias = typedArray.getBoolean(R.styleable.CircleProgressBar_antiAlias, true);

        mHint = typedArray.getString(R.styleable.CircleProgressBar_hint);
        mHintColor = typedArray.getColor(R.styleable.CircleProgressBar_hintColor, Color.WHITE);
        mHintSize = typedArray.getDimension(R.styleable.CircleProgressBar_hintSize, 15);

        mValue = typedArray.getFloat(R.styleable.CircleProgressBar_value, 50);
        mMaxValue = typedArray.getFloat(R.styleable.CircleProgressBar_maxValue, 100);
        mValueColor = typedArray.getColor(R.styleable.CircleProgressBar_valueColor, Color.WHITE);
        mValueSize = typedArray.getDimension(R.styleable.CircleProgressBar_valueSize, 50);
        mPrecision = typedArray.getInt(R.styleable.CircleProgressBar_precision, 0);
        mPrecisionFormat = getPrecisionFormat(mPrecision);

        mValueBkgColor = typedArray.getColor(R.styleable.CircleProgressBar_valueBkgColor, Color.BLACK);

        mUnit = typedArray.getString(R.styleable.CircleProgressBar_unit);
        mUnitColor = typedArray.getColor(R.styleable.CircleProgressBar_unitColor, Color.WHITE);
        mUnitSize = typedArray.getDimension(R.styleable.CircleProgressBar_unitSize, 30);
        mTextOffsetPercentInRadius = typedArray.getFloat(R.styleable.CircleProgressBar_textOffsetPercentInRadius, 0.33f);

        mArcWidth = typedArray.getDimension(R.styleable.CircleProgressBar_arcWidth, 20);
        mArcColor = typedArray.getColor(R.styleable.CircleProgressBar_arcColors, Color.RED);
        foreStartColor = typedArray.getColor(R.styleable.CircleProgressBar_foreStartColor, Color.BLUE);
        foreEndColcor = typedArray.getColor(R.styleable.CircleProgressBar_foreEndColor, Color.BLUE);
        mStartAngle = typedArray.getFloat(R.styleable.CircleProgressBar_startAngle, 120);
        mSweepAngle = typedArray.getFloat(R.styleable.CircleProgressBar_sweepAngle, 300);
        mAnimTime = typedArray.getInt(R.styleable.CircleProgressBar_animTime, 1000);

        mBgArcColor = typedArray.getColor(R.styleable.CircleProgressBar_bgArcColor, Color.WHITE);
        mBgArcWidth = typedArray.getDimension(R.styleable.CircleProgressBar_bgArcWidth, 15);
        mDottedLineCount = typedArray.getInteger(R.styleable.CircleProgressBar_dottedLineCount, mDottedLineCount);
        mLineDistance = typedArray.getInteger(R.styleable.CircleProgressBar_lineDistance, mLineDistance);
        mDottedLineLength = typedArray.getDimension(R.styleable.CircleProgressBar_dottedLineLength, mDottedLineLength);

        mScaleColor = typedArray.getColor(R.styleable.CircleProgressBar_scaleColor, Color.WHITE);
        mScaleSize = typedArray.getDimension(R.styleable.CircleProgressBar_scaleSize, 14);

        typedArray.recycle();
    }

    private void initPaint() {
        mHintPaint = new TextPaint();
        // 设置抗锯齿,会消耗较大资源，绘制图形速度会变慢。
        mHintPaint.setAntiAlias(antiAlias);
        // 设置绘制文字大小
        mHintPaint.setTextSize(mHintSize);
        // 设置画笔颜色
        mHintPaint.setColor(mHintColor);
        // 从中间向两边绘制，不需要再次计算文字
        mHintPaint.setTextAlign(Paint.Align.CENTER);

        mValuePaint = new TextPaint();
        mValuePaint.setAntiAlias(antiAlias);
        mValuePaint.setTextSize(mValueSize);
        mValuePaint.setColor(mValueColor);
        // 设置Typeface对象，即字体风格，包括粗体，斜体以及衬线体，非衬线体等
        mValuePaint.setTypeface(Typeface.DEFAULT_BOLD);
        mValuePaint.setTextAlign(Paint.Align.CENTER);

        mUnitPaint = new TextPaint();
        mUnitPaint.setAntiAlias(antiAlias);
        mUnitPaint.setTextSize(mUnitSize);
        mUnitPaint.setColor(mUnitColor);
        mUnitPaint.setTextAlign(Paint.Align.CENTER);

        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(antiAlias);
        // 设置画笔的样式，为FILL，FILL_OR_STROKE，或STROKE
        //Paint.Style.FILL：填充内部
        //Paint.Style.FILL_AND_STROKE  ：填充内部和描边
        //Paint.Style.STROKE  ：描边
        mArcPaint.setStyle(Paint.Style.STROKE);
        //设置防抖（颜色）
        //mArcPaint.setDither(true);
        // 设置画笔粗细
        mArcPaint.setStrokeWidth(mArcWidth);
        // 当画笔样式为STROKE或FILL_OR_STROKE时，设置笔刷的图形样式，如圆形样式
        // Cap.ROUND(圆形样式)或Cap.SQUARE(方形样式)
        mArcPaint.setStrokeCap(Paint.Cap.SQUARE);

        mBgArcPaint = new Paint();
        mBgArcPaint.setAntiAlias(antiAlias);
        mBgArcPaint.setColor(mBgArcColor);
        mBgArcPaint.setStyle(Paint.Style.STROKE);
        mBgArcPaint.setStrokeWidth(mBgArcWidth);
        mBgArcPaint.setStrokeCap(Paint.Cap.ROUND);

        //设置刻度
        mPaintScale = new Paint();
        mPaintScale.setColor(mScaleColor);
        mPaintScale.setTextAlign(Paint.Align.CENTER);
        mPaintScale.setTextSize(mScaleSize);
        mPaintScale.setAntiAlias(antiAlias);

        //中间文字背景
        mValueBkgPaint = new Paint();
        mValueBkgPaint.setColor(mValueBkgColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureView(widthMeasureSpec, mDefaultSize),
                measureView(heightMeasureSpec, mDefaultSize));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mArcCenterX = (int) (w / 2.f);
        //Log.d(TAG, "onSizeChanged: w = " + w + "; h = " + h + "; oldw = " + oldw + "; oldh = " + oldh);
        //求圆弧和背景圆弧的最大宽度
        float maxArcWidth = Math.max(mArcWidth, mBgArcWidth);
        //求最小值作为实际值
        int minSize = Math.min(w - getPaddingLeft() - getPaddingRight() - 2 * (int) maxArcWidth,
                h - getPaddingTop() - getPaddingBottom() - 2 * (int) maxArcWidth);
        //减去圆弧的宽度，否则会造成部分圆弧绘制在外围
        mRadius = minSize / 2;
        //获取圆的相关参数
        mCenterPoint.x = w / 2;
        mCenterPoint.y = h / 2;
        //绘制圆弧的边界
        mRectF.left = mCenterPoint.x - mRadius - maxArcWidth / 2;
        mRectF.top = mCenterPoint.y - mRadius - maxArcWidth / 2;
        mRectF.right = mCenterPoint.x + mRadius + maxArcWidth / 2;
        mRectF.bottom = mCenterPoint.y + mRadius + maxArcWidth / 2;
        //计算文字绘制时的 baseline
        //由于文字的baseline、descent、ascent等属性只与textSize和typeface有关，所以此时可以直接计算
        //若value、hint、unit由同一个画笔绘制或者需要动态设置文字的大小，则需要在每次更新后再次计算
        mValueOffset = mCenterPoint.y + getBaselineOffsetFromY(mValuePaint);
        mHintOffset = mCenterPoint.y - mRadius * mTextOffsetPercentInRadius + getBaselineOffsetFromY(mHintPaint);
        mUnitOffset = mCenterPoint.y + mRadius * mTextOffsetPercentInRadius + getBaselineOffsetFromY(mUnitPaint);

        if (useGradient) {
            LinearGradient gradient = new LinearGradient(mRectF.left, mRectF.top,
                    mRectF.right, mRectF.bottom, foreEndColcor, foreStartColor, Shader.TileMode.CLAMP);
            //LinearGradient gradient = new LinearGradient(0, 0, w, h, foreEndColcor, foreStartColor, Shader.TileMode.CLAMP);
            mArcPaint.setShader(gradient);
        } else {
            mArcPaint.setColor(mArcColor);
        }

        mRectValueBkg.left = mCenterPoint.x - mRadius + 10;
        mRectValueBkg.top = mCenterPoint.y - mRadius + 10;
        mRectValueBkg.right = mCenterPoint.x + mRadius - 10;
        mRectValueBkg.bottom = mCenterPoint.y + mRadius - 10;

//        Log.d(TAG, "onSizeChanged: 控件大小 = " + "(" + w + ", " + h + ")"
//                + "圆心坐标 = " + mCenterPoint.toString()
//                + ";圆半径 = " + mRadius
//                + ";圆的外接矩形 = " + mRectF.toString());

        // 虚线的外部半径
        mExternalDottedLineRadius = (int) (mRectF.width() / 2)+mLineDistance;
        // 虚线的内部半径
        mInsideDottedLineRadius = mExternalDottedLineRadius - mDottedLineLength;
    }

    private float getBaselineOffsetFromY(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return ((Math.abs(fontMetrics.ascent) - fontMetrics.descent))/ 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawText(canvas);
        drawArc(canvas);
        drawScale(canvas);
    }

    //用做将刻度文字旋转，使之垂直显示
    void drawScaleText(Canvas canvas , String text , float x , float y, Paint paint , float angle){
        //一下针对旋转导致的刻度远离或接近圆心做出相应的距离调整
        if (Math.abs(angle) > 120 && Math.abs(angle) < 145){
            y = y - 8;
        }
        if (Math.abs(angle) > 150 && Math.abs(angle) < 230){
            y = y - 16;
        }
        if (Math.abs(angle) > 300){
            y = y + 12;
        }
        if(angle != 0){
            canvas.rotate(angle, x, y);
        }
        canvas.drawText(text, x, y, paint);

        if(angle != 0){
            canvas.rotate(-angle, x, y);   //恢复
        }
    }

    private void drawScale(Canvas canvas) {
        float radius = mCenterPoint.x + mRadius + mArcWidth + mScaleSize;  //刻度离中心的距离
        float everyDegree = (mSweepAngle-4) / 10; //多出来的环宽补偿和"ready"之前的抵消之后差4
        float startDegree = 170-mSweepAngle/2;  //理论为180，减掉之多来的部分

        canvas.save();// 保存当前画布

        canvas.rotate(startDegree, mCenterPoint.x, mCenterPoint.y);
//        canvas.drawText("0", mCenterPoint.x, radius, mPaintScale);
        drawScaleText(canvas, "0", mCenterPoint.x, radius, mPaintScale, 0-startDegree);

        canvas.rotate((float) 0.8*everyDegree, mCenterPoint.x, mCenterPoint.y);
        drawScaleText(canvas, "Ready", mCenterPoint.x, radius+18, mPaintScale, (float) (0-1.8*everyDegree));  //ready文字太长，故往外推一点

        for (int i = 1; i <= 10; i++) {
            canvas.rotate(everyDegree, mCenterPoint.x, mCenterPoint.y);
            //canvas.drawText(i * 10 + "", mCenterPoint.x, radius, mPaintScale);
            drawScaleText(canvas, i * 10 + "", mCenterPoint.x, radius, mPaintScale, (float) (0-1.8*everyDegree-everyDegree*i));
        }

        canvas.restore();//
    }

    /**
     * 绘制内容文字
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        canvas.save();
        canvas.rotate(0, mCenterPoint.x, mCenterPoint.y);
        canvas.drawArc(mRectValueBkg, 0, 360, true, mValueBkgPaint);

        if (mUnit != null){
            canvas.drawText(String.format(mPrecisionFormat, mValue), mCenterPoint.x, mValueOffset-mUnitSize/2, mValuePaint); //数字稍微靠上一点
            //canvas.drawText(String.valueOf((int)(mValue)), mCenterPoint.x, mValueOffset-mUnitSize/2, mValuePaint); //数字稍微靠上一点
        }else{
            canvas.drawText(String.format(mPrecisionFormat, mValue), mCenterPoint.x, mValueOffset, mValuePaint);
        }

        if (mHint != null) {
            canvas.drawText(mHint.toString(), mCenterPoint.x, mHintOffset, mHintPaint);
        }

        if (mUnit != null) {
            canvas.drawText(mUnit.toString(), mCenterPoint.x, mUnitOffset, mUnitPaint);
        }

        canvas.restore();
    }

    private int getDpValue(int w) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, w, getContext().getResources().getDisplayMetrics());
    }

    private void drawArc(Canvas canvas) {
        // 绘制背景圆弧
        // 从进度圆弧结束的地方开始重新绘制，优化性能


        //经过测试判断startDegress和endDegress12点钟方向为0度，顺时针递增
        // 360 * Math.PI / 180
        float evenryDegrees = (float) (2.0f * Math.PI / mDottedLineCount);
        //测试发现当实心环宽度加大时，会默认在头部加长，故这里也适当增多虚线
        float startDegress = (float) ((276-mStartAngle) * Math.PI / 180);
        float endDegress = (float) ((84+mStartAngle) * Math.PI / 180);
        for (int i = 0; i < mDottedLineCount; i++) {
            float degrees = i * evenryDegrees;
            // 过滤底部90度的弧长
            if (degrees > startDegress && degrees < endDegress) {
                continue;
            }

            float startX = mArcCenterX + (float) Math.sin(degrees) * mInsideDottedLineRadius;
            float startY = mArcCenterX - (float) Math.cos(degrees) * mInsideDottedLineRadius;

            float stopX = mArcCenterX + (float) Math.sin(degrees) * mExternalDottedLineRadius;
            float stopY = mArcCenterX - (float) Math.cos(degrees) * mExternalDottedLineRadius;

            canvas.drawLine(startX, startY, stopX, stopY, mBgArcPaint);
        }



        // 第一个参数 oval 为 RectF 类型，即圆弧显示区域
        // startAngle 和 sweepAngle  均为 float 类型，分别表示圆弧起始角度和圆弧度数
        // 3点钟方向为0度，顺时针递增
        // 如果 startAngle < 0 或者 > 360,则相当于 startAngle % 360
        // useCenter:如果为True时，在绘制圆弧时将圆心包括在内，通常用来绘制扇形

        float currentAngle = mSweepAngle * mPercent;
        canvas.drawArc(mRectF, mStartAngle, currentAngle, false, mArcPaint);
    }

    public boolean isAntiAlias() {
        return antiAlias;
    }

    public CharSequence getHint() {
        return mHint;
    }

    public void setHint(CharSequence hint) {
        mHint = hint;
    }

    public CharSequence getUnit() {
        return mUnit;
    }

    public void setUnit(CharSequence unit) {
        mUnit = unit;
    }

    public float getValue() {
        return mValue;
    }

    /**
     * 设置当前值
     *
     * @param value
     */
    public void setValue(float value) {
        if (value > mMaxValue) {
            value = mMaxValue;
        }
        float start = mPercent;
        float end = value / mMaxValue;
        startAnimator(start, end, mAnimTime);
    }

    private void startAnimator(float start, float end, long animTime) {
        mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.setDuration(animTime);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPercent = (float) animation.getAnimatedValue();
                mValue = mPercent * mMaxValue;
                invalidate();//触发onDraw
            }
        });
        mAnimator.start();
    }

    /**
     * 获取最大值
     *
     * @return
     */
    public float getMaxValue() {
        return mMaxValue;
    }

    /**
     * 设置最大值
     *
     * @param maxValue
     */
    public void setMaxValue(float maxValue) {
        mMaxValue = maxValue;
    }

    /**
     * 获取精度
     *
     * @return
     */
    public int getPrecision() {
        return mPrecision;
    }

    public void setPrecision(int precision) {
        mPrecision = precision;
        mPrecisionFormat = getPrecisionFormat(precision);
    }

    public long getAnimTime() {
        return mAnimTime;
    }

    public void setAnimTime(long animTime) {
        mAnimTime = animTime;
    }

    /**
     * 重置
     */
    public void reset() {
        startAnimator(mPercent, 0.0f, 1000L);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //释放资源
    }

    /**
     * 测量 View
     *
     * @param measureSpec
     * @param defaultSize View 的默认大小
     * @return
     */
    private static int measureView(int measureSpec, int defaultSize) {
        int result = defaultSize;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);

        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    /**
     * dip 转换成px
     *
     * @param dip
     * @return
     */
    public static int dipToPx(Context context, float dip) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

    /**
     * 获取数值精度格式化字符串
     *
     * @param precision
     * @return
     */
    public static String getPrecisionFormat(int precision) {
        return "%." + precision + "f";
    }
}