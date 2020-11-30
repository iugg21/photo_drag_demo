package com.ctrun.photo.drag.demo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ctrun.photo.drag.demo.R;

/**
 * @author Lenovo on 2015/8/22.
 * 用点表示当前 ViewPager 是第几页
 */
public class IndicatorView extends FrameLayout {
    private static final int SELECTED_NONE = -1;

    private int pointResourceId;

    private LinearLayout layout;
    private int mSelect = SELECTED_NONE;

    public IndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.common_indicator_view, this);
        layout = findViewById(R.id.layout);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IndicatorView);
        int count = typedArray.getInt(R.styleable.IndicatorView_pointCount, 0);
        pointResourceId = typedArray.getResourceId(R.styleable.IndicatorView_pointView, R.layout.common_image_point);

        for (int i = 0; i < count; ++i) {
            View.inflate(context, pointResourceId, layout);
        }

        if (count > 0) {
            layout.getChildAt(0).setSelected(true);
        }
    }

    public void setCount(int count, int selectedPos) {
        if (count < 0 || count <= selectedPos) {
            return;
        }

        if(count == 0) {
            mSelect = SELECTED_NONE;
            layout.removeAllViews();
            return;
        }

        if (layout.getChildCount() != count) {
            mSelect = SELECTED_NONE;
            layout.removeAllViews();
            for (int i = 0; i < count; ++i) {
                View.inflate(getContext(), pointResourceId, layout);
            }
        }
        setSelect(selectedPos);
    }

    public void setSelect(int pos) {
        if (pos == mSelect) {
            return;
        }

        int count = layout.getChildCount();
        if (pos >= count) {
            return;
        }

        if (0 <= mSelect && mSelect < count) {
            layout.getChildAt(mSelect).setSelected(false);
        }

        layout.getChildAt(pos).setSelected(true);

        mSelect = pos;
    }

}
