package com.github.adnansm.timelytextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.github.adnansm.timelytextview.animation.TimelyEvaluator;
import com.github.adnansm.timelytextview.model.NumberUtils;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.util.Property;

public class TimelyView extends View {
    private static final float                           RATIO                   = 1f;
    private static final Property<TimelyView, float[][]> CONTROL_POINTS_PROPERTY = new Property<TimelyView, float[][]>(float[][].class, "controlPoints") {
        @Override
        public float[][] get(TimelyView object) {
            return object.getControlPoints();
        }

        @Override
        public void set(TimelyView object, float[][] value) {
            object.setControlPoints(value);
        }
    };
    private final int default_text_color = Color.BLACK;

    private              Paint                           mPaint                  = null;
    private              Path                            mPath                   = null;
    private              float[][]                       controlPoints           = null;

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
        if(mPaint != null)
            mPaint.setColor(textColor);
    }

    public int getTextColor() {
        return mTextColor;
    }

    private int mTextColor;

    public TimelyView(Context context) {
        this(context, null);
    }

    public TimelyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimelyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TimelyView, defStyleAttr, 0);
        initByAttributes(attributes);
        attributes.recycle();
        init();
    }

    protected void initByAttributes(TypedArray attributes) {
        mTextColor = attributes.getColor(R.styleable.TimelyView_timely_text_color, default_text_color);
    }

    public float[][] getControlPoints() {
        return controlPoints;
    }

    public void setControlPoints(float[][] controlPoints) {
        this.controlPoints = controlPoints;
        invalidate();
    }

    public ObjectAnimator animate(int start, int end) {
        float[][] startPoints = NumberUtils.getControlPointsFor(start);
        float[][] endPoints = NumberUtils.getControlPointsFor(end);

        return ObjectAnimator.ofObject(this, CONTROL_POINTS_PROPERTY, new TimelyEvaluator(), startPoints, endPoints);
    }

    public ObjectAnimator animate(int end) {
        float[][] startPoints = NumberUtils.getControlPointsFor(-1);
        float[][] endPoints = NumberUtils.getControlPointsFor(end);

        return ObjectAnimator.ofObject(this, CONTROL_POINTS_PROPERTY, new TimelyEvaluator(), startPoints, endPoints);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (controlPoints == null) return;

        int length = controlPoints.length;

        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        float minDimen = height > width ? width : height;

        mPath.reset();
        mPath.moveTo(minDimen * controlPoints[0][0], minDimen * controlPoints[0][1]);
        for (int i = 1; i < length; i += 3) {
            mPath.cubicTo(minDimen * controlPoints[i][0], minDimen * controlPoints[i][1],
                    minDimen * controlPoints[i + 1][0], minDimen * controlPoints[i + 1][1],
                    minDimen * controlPoints[i + 2][0], minDimen * controlPoints[i + 2][1]);
        }
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if(height == 0 && widthMode != MeasureSpec.UNSPECIFIED)
            height = width;
        else if(width == 0 && heightMode != MeasureSpec.UNSPECIFIED)
            width = height;

        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heigthWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        int maxWidth = (int) (heigthWithoutPadding * RATIO);
        int maxHeight = (int) (widthWithoutPadding / RATIO);

        if (widthWithoutPadding > maxWidth) {
            width = maxWidth + getPaddingLeft() + getPaddingRight();
        } else {
            height = maxHeight + getPaddingTop() + getPaddingBottom();
        }

        setMeasuredDimension(width, height);
    }

    private void init() {
        // A new paint with the style as stroke.
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mTextColor);
        mPaint.setStrokeWidth(5.0f);
        mPaint.setStyle(Paint.Style.STROKE);
        mPath = new Path();
    }
}
