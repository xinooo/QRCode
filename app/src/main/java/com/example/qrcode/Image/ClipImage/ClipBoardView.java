package com.example.qrcode.Image.ClipImage;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.example.qrcode.R;

/**
 * @author jett
 * @since 2018-03-05.
 */
public class ClipBoardView extends View {

    private int clipPadding;
    private Rect clipRect = new Rect();
    private Rect strokeRect = new Rect();
    private Paint mPaint = new Paint();

    private int fillColor;
    private int strokeWidth;
    private int strokeColor;

    public ClipBoardView(Context context) {
        super(context);
        init();
    }

    public ClipBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClipBoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ClipBoardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        clipPadding = dp2px(40);
        fillColor = 0x33000000;
        strokeColor = getResources().getColor(R.color.white);
        strokeWidth = dp2px(1);
    }

    private int dp2px(float dp) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (measuredWidth > 0 && measuredHeight > 0) {
            int clipWidth = measuredWidth - 2 * clipPadding;
            int clipHeight = measuredHeight - 2 * clipPadding;
            int side = Math.min(clipWidth, clipHeight);

            int left = (measuredWidth - side) / 2;
            int top = (measuredHeight - side) / 2;
            clipRect.set(left, top, left + side, top + side);

            int d = strokeWidth / 2;
            strokeRect.set(clipRect.left + d, clipRect.top + d, clipRect.right - d, clipRect.bottom - d);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(fillColor);
        canvas.drawRect(0, 0, clipRect.left, getMeasuredHeight(), mPaint);
        canvas.drawRect(clipRect.left, 0, clipRect.right, clipRect.top, mPaint);
        canvas.drawRect(clipRect.right, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
        canvas.drawRect(clipRect.left, clipRect.bottom, clipRect.right, getMeasuredHeight(), mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(strokeColor);
        mPaint.setStrokeWidth(strokeWidth);
        canvas.drawRect(strokeRect.left, strokeRect.top, strokeRect.right, strokeRect.bottom, mPaint);
    }

}
