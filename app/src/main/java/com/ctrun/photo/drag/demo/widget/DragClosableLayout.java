package com.ctrun.photo.drag.demo.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import com.ctrun.photo.drag.demo.R;

import java.util.ArrayList;
import java.util.List;

import static androidx.customview.widget.ViewDragHelper.STATE_IDLE;


/**
 * @author ctrun on 2020/11/26
 * 带下拉关闭功能的布局
 */
@SuppressWarnings("unused")
public class DragClosableLayout extends FrameLayout {
    private static final String TAG = DragClosableLayout.class.getSimpleName();
    private ViewDragHelper mDragHelper;
    private List<DragCallback> mCallbacks;
    private HandleCallback mHandleCallback;

    private Activity mActivity;
    private View mDecorChildView;
    private ViewGroup mContentLayout;
    private View mContentView;

    private float mOriginalTop = 0;
    private float mDragThreshold;

    private boolean mEnable = true;

    public DragClosableLayout(Context context) {
        this(context, null);
    }

    public DragClosableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mDragThreshold = 0.2f;
        mDragHelper = ViewDragHelper.create(this, new ViewDragCallback());
    }

    public void attachToActivity(Activity activity) {
        mActivity = activity;

        mDecorChildView = mActivity.findViewById(Window.ID_ANDROID_CONTENT);
        mDecorChildView.setBackground(new ColorDrawable(Color.BLACK));

        addDragCallback(new SimpleDragCallback() {
            @Override
            public void onReleasedToClose() {
                if (mActivity != null && !mActivity.isFinishing()) {
                    mActivity.finish();
                    mActivity.overridePendingTransition(0, 0);
                }
            }
        });
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        boolean mFlagClose = false;
        boolean mFlagResume = false;
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            if (mFlagClose || mFlagResume) {
                return false;
            }

            if (mDragHelper.getViewDragState() == STATE_IDLE) {
                if (mCallbacks != null && !mCallbacks.isEmpty()) {
                    for (DragCallback callback : mCallbacks) {
                        callback.onStartDrag();
                    }
                }
                return true;
            }

            return false;
        }

        @Override
        public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {
            mOriginalTop = capturedChild.getTop();
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            Log.d(TAG, "top=" + top + ",left=" + left);

            final float yDiff = Math.abs(top - mOriginalTop);
            final int halfHeight = getHeight() >> 1;
            if (mFlagClose) {
                if (mContentView != null) {
                    final float scale = Math.min(mContentView.getScaleX(), yDiff / halfHeight);
                    mContentView.setScaleX(scale);
                    mContentView.setScaleY(scale);
                }

                if (top <= 10) {
                    if (mCallbacks != null && !mCallbacks.isEmpty()) {
                        for (DragCallback callback : mCallbacks) {
                            callback.onReleasedToClose();
                        }
                    }
                }

                return;
            }

            if (mFlagResume) {
                mFlagResume = false;
                reset();

                if (mCallbacks != null && !mCallbacks.isEmpty()) {
                    for (DragCallback callback : mCallbacks) {
                        callback.onReleasedToResume();
                    }
                }

                return;
            }

            if (top - mOriginalTop > 0) {
                final int alpha = (int) ((1 - Math.min(1, yDiff / halfHeight)) * 0xff);
                mDecorChildView.getBackground().setAlpha(alpha);

                final float scale = Math.max(0.4f, 1 - yDiff / halfHeight);
                if (mContentView != null) {
                    mContentView.setScaleX(scale);
                    mContentView.setScaleY(scale);
                }

                Log.d(TAG, "scale=" + scale + ",alpha=" + alpha);
            } else {
                reset();
            }
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return 1;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            if (mDragHelper.getViewDragState() == STATE_IDLE && dy < 0) {
                return 0;
            }

            return top;
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            return left;
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            Log.d(TAG, "xvel=" + xvel + ",yvel=" + yvel);

            if ((releasedChild.getTop() - mOriginalTop) / (float) getWidth() >= mDragThreshold) {
                mFlagClose = true;
                mFlagResume = false;
                mDecorChildView.getBackground().setAlpha(0);

                if (mCallbacks != null && !mCallbacks.isEmpty()) {
                    for (DragCallback callback : mCallbacks) {
                        callback.onReleasedToPendingClose();
                    }
                }

                mDragHelper.settleCapturedViewAt(0, (int) mOriginalTop);
                postInvalidateOnAnimation();
            } else {
                mFlagClose = false;
                mFlagResume = true;
                if (mCallbacks != null && !mCallbacks.isEmpty()) {
                    for (DragCallback callback : mCallbacks) {
                        callback.onReleasedToPendingResume();
                    }
                }

                ViewCompat.offsetLeftAndRight(releasedChild, -releasedChild.getLeft());
                ViewCompat.offsetTopAndBottom(releasedChild, -releasedChild.getTop());
                onViewPositionChanged(releasedChild, 0, 0, -releasedChild.getLeft(), -releasedChild.getTop());

                //mDragHelper.settleCapturedViewAt(0, (int) mOriginalTop);
                //abortAnimation = false;
                //postInvalidateOnAnimation();
            }

        }
    }

    private void reset() {
        mDecorChildView.getBackground().setAlpha(0xff);
        if (mContentView != null) {
            mContentView.setScaleX(1);
            mContentView.setScaleY(1);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentLayout = findViewById(R.id.fl_content);
    }

    public void setContentView(View contentView) {
        this.mContentView = contentView;
        mContentLayout.removeAllViews();
        mContentLayout.addView(contentView);
    }

    public void setDragCloseEnable(boolean enable) {
        mEnable = enable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mEnable) {
            return false;
        }

        if (ev.getPointerCount() > 1) {
            return false;
        }

        if (mHandleCallback != null && !mHandleCallback.shouldHandle()) {
            return false;
        }

        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnable) {
            return false;
        }

        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            postInvalidateOnAnimation();
        }
    }

    public void addDragCallback(DragCallback callback) {
        if (mCallbacks == null) {
            mCallbacks = new ArrayList<>();
        }

        mCallbacks.add(callback);
    }

    public void removeDragCallback(DragCallback callback) {
        if (mCallbacks == null) {
            return;
        }
        mCallbacks.remove(callback);
    }

    public static class SimpleDragCallback implements DragCallback {

        @Override
        public void onStartDrag() {

        }

        @Override
        public void onReleasedToPendingClose() {

        }

        @Override
        public void onReleasedToPendingResume() {

        }

        @Override
        public void onReleasedToClose() {

        }

        @Override
        public void onReleasedToResume() {

        }
    }

    public interface DragCallback {

        /**
         * 开始拖动（调用一次）
         */
        void onStartDrag();

        /**
         * 释放时刻（调用一次，将会触发关闭）
         */
        void onReleasedToPendingClose();

        /**
         * 关闭
         */
        void onReleasedToClose();

        /**
         * 释放时刻（调用一次，将会触发恢复）
         */
        void onReleasedToPendingResume();

        /**
         * 恢复
         */
        void onReleasedToResume();
    }

    public void setHandleCallback(HandleCallback callback) {
        mHandleCallback = callback;
    }

    public interface HandleCallback {
        /**
         * 用户自定义规则是否需要进行拦截
         * @return true 拦截，false 不拦截
         */
        boolean shouldHandle();

    }
}