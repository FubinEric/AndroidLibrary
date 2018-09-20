package com.eric.camera.recognition.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.eric.camera.R;
import com.eric.camera.recognition.utils.RecognitionUtils;

/**
 * 摄像头预览框View
 */
public class RecognitionScanBoxView extends View {

    private Rect mFramingRect;
    private Paint mPaint;


    private int mRectWidth;
    private int mRectHeight;
    private int mTopOffset;
    private int mMaskColor;// 除边框外阴影颜色
    private int mBorderHeight;// 边框线宽度
    private int mBorderColor;// 边框线颜色

    private int mCornerHeigjt; // 边角线宽度
    private int mCornerColor; // 边角线颜色
    private int mCornerLength;
    private float mHalfCornerWidth = 0; // 一半的边角线宽度

    private int screenMaxWidth = 0;  // 屏幕宽度(dp)
    private int screenMaxHeight = 0;  // 屏幕高度(dp)

    private boolean isQrCodeStyle = false;
    private boolean mIsScanLineShow = true;

    public RecognitionScanBoxView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
        initCustomAttrs(context, attrs);
        afterInitCustomAttrs();
    }

    private void init(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        getAndroiodScreenProperty(context);
        mTopOffset = RecognitionUtils.dp2px(context, 200);
        mRectWidth = screenMaxWidth;
        mRectHeight = RecognitionUtils.dp2px(context, 140);
        mMaskColor = Color.parseColor("#88000000");
        mBorderHeight = RecognitionUtils.dp2px(context, 1);
        mBorderColor = Color.WHITE;

        mCornerHeigjt = RecognitionUtils.dp2px(context, 3);
        mCornerColor = Color.RED;
        mCornerLength = RecognitionUtils.dp2px(context, 20);
        isQrCodeStyle = false;
    }

    private void initCustomAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecognitionScanBoxView);
        final int count = typedArray.getIndexCount();
        for (int i = 0; i < count; i++) {
            initCustomAttr(typedArray.getIndex(i), typedArray);
        }
        typedArray.recycle();

        afterInitCustomAttrs();
    }

    private void initCustomAttr(int attr, TypedArray typedArray) {
        if (attr == R.styleable.RecognitionScanBoxView_topOffset) {
            mTopOffset = typedArray.getDimensionPixelSize(attr, mTopOffset);
        } else if (attr == R.styleable.RecognitionScanBoxView_rectWidth) {
            mRectWidth = typedArray.getDimensionPixelSize(attr, mRectWidth);
        } else if (attr == R.styleable.RecognitionScanBoxView_rectHeight) {
            mRectHeight = typedArray.getDimensionPixelSize(attr, mRectHeight);
        } else if (attr == R.styleable.RecognitionScanBoxView_maskColor) {
            mMaskColor = typedArray.getColor(attr, mRectHeight);
        } else if (attr == R.styleable.RecognitionScanBoxView_borderHeight) {
            mBorderHeight = typedArray.getDimensionPixelSize(attr, mBorderHeight);
        } else if (attr == R.styleable.RecognitionScanBoxView_borderColor) {
            mBorderColor = typedArray.getColor(attr, mBorderColor);
        } else if (attr == R.styleable.RecognitionScanBoxView_cornerHeight) {
            mCornerHeigjt = typedArray.getDimensionPixelSize(attr, mCornerHeigjt);
        } else if (attr == R.styleable.RecognitionScanBoxView_cornerColor) {
            mCornerColor = typedArray.getColor(attr, mCornerColor);
        } else if (attr == R.styleable.RecognitionScanBoxView_cornerLength) {
            mCornerLength = typedArray.getDimensionPixelSize(attr, mCornerLength);
        } else if (attr == R.styleable.RecognitionScanBoxView_isQrCodeStyle) {
            isQrCodeStyle = typedArray.getBoolean(attr, isQrCodeStyle);
        }

    }

    public void afterInitCustomAttrs() {
        if (screenMaxWidth > 0 && mRectWidth >= screenMaxWidth) {
            mRectWidth = screenMaxWidth;
        }

        mHalfCornerWidth = 1.0f * mCornerHeigjt / 2;

        calFramingRect();
        postInvalidate();
    }

    public Rect getPreviewRect() {
        return mFramingRect;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mFramingRect == null) {
            return;
        }

        // 画遮罩层
        drawMask(canvas);

        // 画边框线
        drawBorderLine(canvas);

        // 画四个直角的线
        drawCornerLine(canvas);

        // 画扫描线
        drawScanLine(canvas);

        // 移动扫描线的位置
        moveScanLine();

    }

    /**
     * 画遮罩层
     *
     * @param canvas
     */
    private void drawMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if (mMaskColor != Color.TRANSPARENT) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mMaskColor);
            canvas.drawRect(0, 0, width, mFramingRect.top, mPaint);
            canvas.drawRect(0, mFramingRect.top, mFramingRect.left, mFramingRect.bottom + 1, mPaint);
            canvas.drawRect(mFramingRect.right + 1, mFramingRect.top, width, mFramingRect.bottom + 1, mPaint);
            canvas.drawRect(0, mFramingRect.bottom + 1, width, height, mPaint);
        }
    }

    /**
     * 画边框线
     *
     * @param canvas
     */
    private void drawBorderLine(Canvas canvas) {
        if (mBorderHeight > 0) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(mBorderColor);
            mPaint.setStrokeWidth(mBorderHeight);
            canvas.drawRect(mFramingRect, mPaint);
        }
    }

    /**
     * 画四个直角的线
     *
     * @param canvas
     */
    private void drawCornerLine(Canvas canvas) {
        if (mHalfCornerWidth > 0) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(mCornerColor);
            mPaint.setStrokeWidth(mCornerHeigjt);
            canvas.drawLine(mFramingRect.left - mHalfCornerWidth, mFramingRect.top, mFramingRect.left - mHalfCornerWidth + mCornerLength, mFramingRect.top, mPaint);
            canvas.drawLine(mFramingRect.left, mFramingRect.top - mHalfCornerWidth, mFramingRect.left, mFramingRect.top - mHalfCornerWidth + mCornerLength, mPaint);
            canvas.drawLine(mFramingRect.right + mHalfCornerWidth, mFramingRect.top, mFramingRect.right + mHalfCornerWidth - mCornerLength, mFramingRect.top, mPaint);
            canvas.drawLine(mFramingRect.right, mFramingRect.top - mHalfCornerWidth, mFramingRect.right, mFramingRect.top - mHalfCornerWidth + mCornerLength, mPaint);

            canvas.drawLine(mFramingRect.left - mHalfCornerWidth, mFramingRect.bottom, mFramingRect.left - mHalfCornerWidth + mCornerLength, mFramingRect.bottom, mPaint);
            canvas.drawLine(mFramingRect.left, mFramingRect.bottom + mHalfCornerWidth, mFramingRect.left, mFramingRect.bottom + mHalfCornerWidth - mCornerLength, mPaint);
            canvas.drawLine(mFramingRect.right + mHalfCornerWidth, mFramingRect.bottom, mFramingRect.right + mHalfCornerWidth - mCornerLength, mFramingRect.bottom, mPaint);
            canvas.drawLine(mFramingRect.right, mFramingRect.bottom + mHalfCornerWidth, mFramingRect.right, mFramingRect.bottom + mHalfCornerWidth - mCornerLength, mPaint);
        }
    }

    /**
     * 画扫描线
     *
     * @param canvas
     */
    private void drawScanLine(Canvas canvas) {
//        if (!mIsScanLineShow) return;
//        if (mIsBarcode) {
//            if (mIsScanLineVerticalForBarcode) {
//                mPaint.setStyle(Paint.Style.FILL);
//                mPaint.setColor(mScanLineColor);
//                float left = mFramingRect.left;
//                float top = (mFramingRect.bottom - mFramingRect.top) / 2 + mFramingRect.top;
//                float right = mFramingRect.right;
//                float bottom = top + mScanLineSize;
//                canvas.drawRect(left, top, right, bottom, mPaint);
//            } else {
//                mPaint.setStyle(Paint.Style.FILL);
//                mPaint.setColor(mScanLineColor);
//                canvas.drawRect(mScanLineLeft, mFramingRect.top + mHalfCornerWidth + mScanLineMargin, mScanLineLeft + mScanLineSize, mFramingRect.bottom - mHalfCornerWidth - mScanLineMargin, mPaint);
//            }
//        } else {
//
//            mPaint.setStyle(Paint.Style.FILL);
//            mPaint.setColor(mScanLineColor);
//            canvas.drawRect(mFramingRect.left + mHalfCornerWidth + mScanLineMargin, mScanLineTop, mFramingRect.right - mHalfCornerWidth - mScanLineMargin, mScanLineTop + mScanLineSize, mPaint);
//        }
    }

    /**
     * 移动扫描线的位置
     */
    private void moveScanLine() {
//        if (!mIsScanLineShow) return;
//        if (mIsBarcode) {
//            if (mGridScanLineBitmap == null) {
//                // 处理非网格扫描图片的情况
//                mScanLineLeft += mMoveStepDistance;
//                int scanLineSize = mScanLineSize;
//                if (mScanLineBitmap != null) {
//                    scanLineSize = mScanLineBitmap.getWidth();
//                }
//
//                if (mIsScanLineReverse) {
//                    if (mScanLineLeft + scanLineSize > mFramingRect.right - mHalfCornerWidth || mScanLineLeft < mFramingRect.left + mHalfCornerWidth) {
//                        mMoveStepDistance = -mMoveStepDistance;
//                    }
//                } else {
//                    if (mScanLineLeft + scanLineSize > mFramingRect.right - mHalfCornerWidth) {
//                        mScanLineLeft = mFramingRect.left + mHalfCornerWidth + 0.5f;
//                    }
//                }
//            } else {
//                // 处理网格扫描图片的情况
//                mGridScanLineRight += mMoveStepDistance;
//                if (mGridScanLineRight > mFramingRect.right - mHalfCornerWidth) {
//                    mGridScanLineRight = mFramingRect.left + mHalfCornerWidth + 0.5f;
//                }
//            }
//        } else {
//            if (mGridScanLineBitmap == null) {
//                // 处理非网格扫描图片的情况
//                mScanLineTop += mMoveStepDistance;
//                int scanLineSize = mScanLineSize;
//                if (mScanLineBitmap != null) {
//                    scanLineSize = mScanLineBitmap.getHeight();
//                }
//
//                if (mIsScanLineReverse) {
//                    if (mScanLineTop + scanLineSize > mFramingRect.bottom - mHalfCornerWidth || mScanLineTop < mFramingRect.top + mHalfCornerWidth) {
//                        mMoveStepDistance = -mMoveStepDistance;
//                    }
//                } else {
//                    if (mScanLineTop + scanLineSize > mFramingRect.bottom - mHalfCornerWidth) {
//                        mScanLineTop = mFramingRect.top + mHalfCornerWidth + 0.5f;
//                    }
//                }
//            } else {
//                // 处理网格扫描图片的情况
//                mGridScanLineBottom += mMoveStepDistance;
//                if (mGridScanLineBottom > mFramingRect.bottom - mHalfCornerWidth) {
//                    mGridScanLineBottom = mFramingRect.top + mHalfCornerWidth + 0.5f;
//                }
//            }
//
//        }
//        postInvalidateDelayed(mAnimDelayTime, mFramingRect.left, mFramingRect.top, mFramingRect.right, mFramingRect.bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calFramingRect();
    }

    private void calFramingRect() {
        int leftOffset = (getWidth() - mRectWidth) / 2;
        mFramingRect = new Rect(leftOffset, mTopOffset, leftOffset + mRectWidth, mTopOffset + mRectHeight);

    }

    /**
     * 判断是否是二维码扫描模式
     *
     * @return
     */
    public boolean isQrCodeStyle() {
        return isQrCodeStyle;
    }

    /**
     * 设置二维码扫描模式
     *
     * @param isQrCodeStyle
     */
    public void setQrCodeStyle(boolean isQrCodeStyle) {
        this.isQrCodeStyle = isQrCodeStyle;
    }

    private void getAndroiodScreenProperty(Context context) {

        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            if (wm == null) {
                return;
            }
            wm.getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;         // 屏幕宽度（像素）
            int height = dm.heightPixels;       // 屏幕高度（像素）
            float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
            int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
            // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
            int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
            int screenHeight = (int) (height / density);// 屏幕高度(dp)
            screenMaxWidth = RecognitionUtils.dp2px(context, screenWidth);
            screenMaxHeight = RecognitionUtils.dp2px(context, screenHeight);
        } catch (Exception e) {
            e.printStackTrace();
            screenMaxHeight = 0;
            screenMaxWidth = 0;
        }
    }
}