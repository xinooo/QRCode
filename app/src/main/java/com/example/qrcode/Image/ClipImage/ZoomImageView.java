package com.example.qrcode.Image.ClipImage;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatImageView;


/**
 * 瞎写，瞎写。。
 *
 * @author jett
 * @since 2018-03-06.
 */
public class ZoomImageView extends AppCompatImageView {

    private int clipPadding = -1;

    private int side;
    private int sideX, sideY;

    private ZoomMatrix mMatrix = new ZoomMatrix();
    private boolean smoothOutBounds = false;

    public ZoomImageView(Context context) {
        super(context);
        init();
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        clipPadding = dp2px(40);
    }

    public Bitmap getCropBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        Matrix matrix = new Matrix();
        // 控制图片大小为 500 * 500
        float scale = 500 * 1.0F / side;
        if (scale > 1) {
            scale = 1;
        }
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, sideX, sideY, side, side, matrix, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        adjust();
    }

    private void setClipPadding(int clipPadding) {
        this.clipPadding = clipPadding;
        adjust();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        adjust();
    }

    private void adjust() {
        Drawable d = getDrawable();
        if (d == null || clipPadding <= 0) {
            return;
        }
        int width = d.getIntrinsicWidth();
        int height = d.getIntrinsicHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (measuredWidth <= 0 || measuredHeight <= 0) {
            return;
        }
        int cropWidth = measuredWidth - clipPadding * 2;
        int cropHeight = measuredHeight - clipPadding * 2;
        side = Math.min(cropWidth, cropHeight);

        sideX = (measuredWidth - side) / 2;
        sideY = (measuredHeight - side) / 2;

        float widthScale = side * 1.0F / width;
        float heightScale = side * 1.0F / height;

        if (widthScale > heightScale) {
            float tranX = (measuredWidth - side) / 2;
            float tranY = (widthScale * height - measuredHeight) / 2;

            mMatrix.minScale = smoothOutBounds ? heightScale / 2 : widthScale;
            mMatrix.scale = widthScale;
            mMatrix.dx = tranX;
            mMatrix.dy = -tranY;
        } else {
            float tranX = (heightScale * width - measuredWidth) / 2;
            float tranY = (measuredHeight - side) / 2;

            mMatrix.minScale = smoothOutBounds ? widthScale / 2 : heightScale;
            mMatrix.scale = heightScale;
            mMatrix.dx = -tranX;
            mMatrix.dy = tranY;
        }

        mMatrix.width = width;
        mMatrix.height = height;
        setImageMatrix(mMatrix.build());
    }

    float lastDistance = -1F;
    float lastX = -1, lastY = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getActionIndex() == 1) {
                    float offsetX = event.getX(0) - event.getX(1);
                    float offsetY = event.getY(0) - event.getY(1);
                    lastDistance = (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int pointerCount = event.getPointerCount();
                if (pointerCount == 1) {
                    if (lastX == lastY && lastX == -1) {
                        lastX = event.getX(0);
                        lastY = event.getY(0);
                        return true;
                    }
                    float dx = event.getX(0) - lastX;
                    float dy = event.getY(0) - lastY;
                    handleTrans(dx, dy);
                } else if (pointerCount >= 2) {
                    float offsetX = event.getX(0) - event.getX(1);
                    float offsetY = event.getY(0) - event.getY(1);
                    float currentDistance = (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY);
                    handleScale(currentDistance - lastDistance);

                    lastDistance = currentDistance;
                }
                lastX = event.getX(0);
                lastY = event.getY(0);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                lastX = lastY = -1;
                break;
            case MotionEvent.ACTION_UP:
                lastX = lastY = -1;
                break;
            default:
                break;
        }
        return true;
    }

    private void handleTrans(float dx, float dy) {
        mMatrix.trans(dx, dy);
        setImageMatrix(mMatrix.build());
    }

    private void handleScale(float distance) {
        float rate = distance / side;
        mMatrix.zoom(rate * 2F);
        setImageMatrix(mMatrix.build());
    }

    private int dp2px(float dp) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private class ZoomMatrix {

        int width, height;
        float minScale, scale, dx, dy;

        void zoom(float rate) {
            float newScale = scale + rate;
            if (newScale < minScale) {
                rate = minScale - scale;
                scale = minScale;
            } else {
                scale = newScale;
            }
            dx -= rate * width / 2;
            dy -= rate * height / 2;
            checkBounds();
        }

        void trans(float dx, float dy) {
            this.dx += dx;
            this.dy += dy;
            checkBounds();
        }

        void checkBounds() {
            if (smoothOutBounds) {
                return;
            }
            // x 轴左边
            if (dx > sideX) {
                dx = sideX;
            }
            // x 轴右边
            float rightX = scale * width + dx;
            if (rightX < sideX + side) {
                float mx = sideX + side - rightX;
                dx += mx;
            }
            // y 轴上边
            if (dy > sideY) {
                dy = sideY;
            }
            // y 轴下边
            float bottomY = scale * height + dy;
            if (bottomY < sideY + side) {
                float mx = sideY + side - bottomY;
                dy += mx;
            }
        }

        Matrix build() {
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            matrix.postTranslate(dx, dy);
            return matrix;
        }

    }

}
