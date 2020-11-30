package com.ctrun.photo.drag.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * @author ctrun on 2016/11/18.
 */
public class MySubsamplingScaleImageView extends SubsamplingScaleImageView {

    private GestureDetector mDetector;

    public MySubsamplingScaleImageView(Context context, AttributeSet attr) {
        super(context, attr);

        setGestureDetector(context);
    }

    public MySubsamplingScaleImageView(Context context) {
        this(context, null);
    }

    private void setGestureDetector(final Context context) {
        this.mDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                performClick();
                return true;
            }

        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(getState() == null) {
            mDetector.onTouchEvent(event);
            return true;
        }

        return super.dispatchTouchEvent(event);
    }
}
